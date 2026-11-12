package akinai.minikgurme.platform

import akinai.minikgurme.util.DoctorReportContent
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSAttributedString
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToURL
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsPDFRenderer
import platform.UIKit.UIViewController
import platform.UIKit.boundingRectWithSize
import platform.UIKit.drawInRect
import platform.UIKit.NSStringDrawingUsesLineFragmentOrigin
import platform.UniformTypeIdentifiers.UTTypeJSON
import platform.UniformTypeIdentifiers.UTTypePlainText
import platform.darwin.NSObject

/** iOS dosya işleri: Dosyalar'a kaydet/aç (UIDocumentPicker), paylaşım menüsü, PDF çizimi. */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
object IosFiles {
    private fun topController(): UIViewController? {
        var c = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (c?.presentedViewController != null) c = c.presentedViewController
        return c
    }

    fun tempFile(name: String): NSURL = NSURL.fileURLWithPath(NSTemporaryDirectory() + name)

    fun writeText(url: NSURL, text: String): Boolean =
        (NSString.create(string = text)).writeToURL(url, atomically = true, encoding = NSUTF8StringEncoding, error = null)

    fun readText(url: NSURL): String? {
        val scoped = url.startAccessingSecurityScopedResource()
        try {
            val data = NSData.dataWithContentsOfURL(url) ?: return null
            return NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
        } finally { if (scoped) url.stopAccessingSecurityScopedResource() }
    }

    /** Paylaşım menüsü (Dosyalar'a kaydet, Mail, WhatsApp, AirDrop...). */
    fun share(url: NSURL) {
        val vc = UIActivityViewController(activityItems = listOf(url), applicationActivities = null)
        topController()?.presentViewController(vc, animated = true, completion = null)
    }

    /** Dosyalar uygulamasına kaydetme penceresi. */
    fun exportToFiles(url: NSURL, onDone: (Boolean) -> Unit) {
        val picker = UIDocumentPickerViewController(forExportingURLs = listOf(url), asCopy = true)
        val delegate = PickerDelegate(onPicked = { onDone(true) }, onCancel = { onDone(false) })
        retain(delegate); picker.delegate = delegate
        topController()?.presentViewController(picker, animated = true, completion = null)
    }

    /** Dosyalar'dan JSON yedek seçme penceresi. */
    fun pickBackup(onPicked: (NSURL) -> Unit) {
        val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeJSON, UTTypePlainText), asCopy = true)
        val delegate = PickerDelegate(onPicked = onPicked, onCancel = {})
        retain(delegate); picker.delegate = delegate
        topController()?.presentViewController(picker, animated = true, completion = null)
    }

    // Seçici açıkken temsilci nesnesi silinmesin
    private val alive = mutableListOf<Any>()
    private fun retain(o: Any) { alive += o; if (alive.size > 4) alive.removeAt(0) }

    private class PickerDelegate(val onPicked: (NSURL) -> Unit, val onCancel: () -> Unit) : NSObject(), UIDocumentPickerDelegateProtocol {
        override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
            (didPickDocumentsAtURLs.firstOrNull() as? NSURL)?.let(onPicked) ?: onCancel()
        }
        override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) = onCancel()
    }

    /** Ortak rapor içeriğini A4 PDF'e çizer (Android'deki düzenle aynı). */
    fun renderPdf(report: DoctorReportContent.Report): NSURL {
        val w = 595.0; val h = 842.0; val m = 40.0
        val renderer = UIGraphicsPDFRenderer(bounds = CGRectMake(0.0, 0.0, w, h), format = platform.UIKit.UIGraphicsPDFRendererFormat())
        val url = tempFile(report.fileName)
        renderer.writePDFToURL(url, withActions = { ctx ->
            var y = m
            ctx?.beginPage()
            fun attrs(size: Double, bold: Boolean, r: Int, g: Int, b: Int) = mapOf<Any?, Any?>(
                NSFontAttributeName to (if (bold) UIFont.boldSystemFontOfSize(size) else UIFont.systemFontOfSize(size)),
                NSForegroundColorAttributeName to UIColor(red = r / 255.0, green = g / 255.0, blue = b / 255.0, alpha = 1.0)
            )
            fun draw(text: String, a: Map<Any?, Any?>, x: Double = m, after: Double = 5.0) {
                val s = NSAttributedString.create(string = text, attributes = a)
                val needed = s.boundingRectWithSize(platform.CoreGraphics.CGSizeMake(w - x - m, 10_000.0), NSStringDrawingUsesLineFragmentOrigin, null)
                    .useContents { size.height }
                if (y + needed > h - m) { ctx?.beginPage(); y = m }
                s.drawInRect(CGRectMake(x, y, w - x - m, needed + 2))
                y += needed + after
            }
            for (l in report.lines) when (l.style) {
                DoctorReportContent.Style.TITLE -> draw(l.text, attrs(20.0, true, 52, 70, 176), after = 10.0)
                DoctorReportContent.Style.SECTION -> { y += 14.0; draw(l.text, attrs(13.0, true, 31, 34, 56)) }
                DoctorReportContent.Style.BODY -> draw(l.text, attrs(10.5, false, 60, 64, 90))
                DoctorReportContent.Style.BULLET -> draw(l.text, attrs(10.5, false, 60, 64, 90), x = m + 6)
                DoctorReportContent.Style.MUTED -> if (l.text.isEmpty()) y += 14.0 else draw(l.text, attrs(9.0, false, 120, 124, 150))
            }
        }, error = null)
        return url
    }
}
