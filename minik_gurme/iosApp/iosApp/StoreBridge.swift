import Foundation
import StoreKit
import Shared

/// Plus aboneliği: StoreKit 2. Durum mantığı Kotlin tarafında (`StoreEvents`); burada yalnızca mağaza çağrıları yapılır.
/// Ürün kimlikleri App Store Connect'teki aboneliklerle birebir aynı olmalı:
///   minikgurme_plus_monthly, minikgurme_plus_yearly
final class StoreBridge: NSObject, IosStoreBridge {
    private var ids: [String] = []
    private var updates: Task<Void, Never>?

    func start(productIds: [String]) {
        ids = productIds
        updates?.cancel()
        // Uygulama dışında olan değişiklikler (yenileme, iptal, iade, Aile Paylaşımı) burada yakalanır
        let watched = productIds
        updates = Task.detached {
            for await result in Transaction.updates {
                if case .verified(let transaction) = result { await transaction.finish() }
                _ = await StoreBridge.publishEntitlement(ids: watched)
            }
        }
        Task {
            _ = await StoreBridge.publishEntitlement(ids: watched)
            await StoreBridge.publishProducts(ids: watched)
        }
    }

    func purchase(productId: String) {
        let watched = ids
        Task {
            do {
                guard let product = try await Product.products(for: [productId]).first else {
                    StoreEvents.shared.onPurchaseResult(code: "failed"); return
                }
                switch try await product.purchase() {
                case .success(let verification):
                    if case .verified(let transaction) = verification {
                        await transaction.finish()
                        _ = await StoreBridge.publishEntitlement(ids: watched)
                        StoreEvents.shared.onPurchaseResult(code: "success")
                    } else {
                        StoreEvents.shared.onPurchaseResult(code: "failed")
                    }
                case .pending:
                    StoreEvents.shared.onPurchaseResult(code: "pending")
                case .userCancelled:
                    StoreEvents.shared.onPurchaseResult(code: "cancelled")
                @unknown default:
                    StoreEvents.shared.onPurchaseResult(code: "failed")
                }
            } catch {
                StoreEvents.shared.onPurchaseResult(code: "failed")
            }
        }
    }

    func restore() {
        let watched = ids
        Task {
            try? await AppStore.sync()
            let active = await StoreBridge.publishEntitlement(ids: watched)
            StoreEvents.shared.onRestoreResult(active: active, silent: false)
        }
    }

    // MARK: - Yardımcılar

    /// Etkin (süresi dolmamış, iade edilmemiş) Plus aboneliği var mı; sonucu Kotlin'e bildirir.
    @discardableResult
    static func publishEntitlement(ids: [String]) async -> Bool {
        var active = false
        for await result in Transaction.currentEntitlements {
            guard case .verified(let t) = result, ids.contains(t.productID), t.revocationDate == nil else { continue }
            if let expiry = t.expirationDate, expiry < Date() { continue }
            active = true
        }
        StoreEvents.shared.onEntitlement(active: active)
        return active
    }

    static func publishProducts(ids: [String]) async {
        do {
            let products = try await Product.products(for: ids)
            let list = products.map { StoreProduct(id: $0.id, price: $0.displayPrice, period: periodCode($0)) }
            StoreEvents.shared.onProducts(products: list)
        } catch {
            StoreEvents.shared.onProducts(products: [])
        }
    }

    /// ISO 8601 süre kodu (Android'deki billingPeriod ile aynı biçim): P1M, P1Y ...
    static func periodCode(_ product: Product) -> String {
        guard let period = product.subscription?.subscriptionPeriod else { return "" }
        switch period.unit {
        case .day: return "P\(period.value)D"
        case .week: return "P\(period.value)W"
        case .month: return "P\(period.value)M"
        case .year: return "P\(period.value)Y"
        @unknown default: return ""
        }
    }
}
