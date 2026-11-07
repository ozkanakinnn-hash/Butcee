plugins {
    id("com.android.application")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

// google-services.json depoya eklenmez (.gitignore). Dosya varsa Firebase eklentisi uygulanır;
// yoksa uygulama yine derlenir ve açılışta "Firebase yapılandırılmadı" ekranı gösterir.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
} else {
    logger.warn("UYARI: android/app/google-services.json bulunamadı — Firebase devre dışı.")
}

android {
    namespace = "com.haberakisi.haber_akisi"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // flutter_local_notifications için gerekli
        isCoreLibraryDesugaringEnabled = true
    }

    defaultConfig {
        // Uygulama kimliği Play Store'a ilk yüklemeden sonra DEĞİŞTİRİLEMEZ — yayından önce kesinleştirin.
        applicationId = "com.haberakisi.haber_akisi"
        // You can update the following values to match your application needs.
        // For more information, see: https://flutter.dev/to/review-gradle-config.
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        // Uses the version code from pubspec.yaml. When using split APKs, 1000 * ABI_VERSION
        // is added automatically by Flutter. (https://developer.android.com/studio/build/configure-apk-splits#configure-APK-versions)
        // You can force using the value of versionCode by specifying the `-P force-version-code-ignoring-abi=true`
        // flag during build.
        versionCode = flutter.versionCode
        versionName = flutter.versionName
        // AdMob uygulama kimliği: mağaza sürümünde ADMOB_APP_ID ortam değişkeni; yoksa Google TEST kimliği.
        manifestPlaceholders["admobAppId"] =
            System.getenv("ADMOB_APP_ID") ?: "ca-app-pub-3940256099942544~3347511713"
    }

    signingConfigs {
        // Test dağıtımı (CI APK'ları) için SABİT anahtar. Önceden her CI makinesi rastgele bir debug anahtarı
        // üretiyordu → her APK farklı imzalıydı ve telefondaki sürümün üzerine "Uygulama yüklenmedi" hatasıyla
        // kurulamıyordu. Bu anahtar gizli değildir; Play Store'a yüklemede KULLANILMAZ (aşağıdaki "upload").
        create("test") {
            storeFile = file("test-signing.keystore")
            storePassword = "haberakisi-test"
            keyAlias = "test"
            keyPassword = "haberakisi-test"
        }
        // Play Store yükleme anahtarı: GitHub Secrets'tan ortam değişkenleriyle (varsa).
        val uploadStore = System.getenv("UPLOAD_KEYSTORE_PATH")
        if (uploadStore != null && file(uploadStore).exists()) {
            create("upload") {
                storeFile = file(uploadStore)
                storePassword = System.getenv("UPLOAD_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("UPLOAD_KEY_ALIAS")
                keyPassword = System.getenv("UPLOAD_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("test")
        }
        release {
            signingConfig = signingConfigs.findByName("upload") ?: signingConfigs.getByName("test")
            // R8 koruma kuralları (WorkManager/Room, bildirimler) — yoksa release açılışta çöker
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

flutter {
    source = "../.."
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")
}
