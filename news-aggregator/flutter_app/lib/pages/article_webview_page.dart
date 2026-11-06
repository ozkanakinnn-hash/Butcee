import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:share_plus/share_plus.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:webview_flutter/webview_flutter.dart';

import '../l10n/app_strings.dart';

/// Haberi orijinal sitesinde, uygulama içi tarayıcıda (WebView) açan tam ekran sayfa.
///
/// Kullanım:
/// ```dart
/// Navigator.of(context).push(ArticleWebViewPage.route(
///   url: article.originalUrl,
///   sourceName: article.sourceName,
///   articleTitle: article.title,
/// ));
/// ```
///
/// Üst bar: solda "X" (kapat), ortada kaynak adı + kilit simgesi (https), sağda paylaş ve menü.
/// Telif açısından içerik kopyalanmaz; site kendi tasarımı ve reklamlarıyla gösterilir.
class ArticleWebViewPage extends StatefulWidget {
  const ArticleWebViewPage({super.key, required this.url, this.sourceName = '', this.articleTitle = ''});

  /// Açılacak haber adresi (articles.original_url).
  final String url;

  /// Üst barda gösterilecek kaynak adı (articles.source_name).
  final String sourceName;

  /// Paylaşım metninde kullanılacak başlık (articles.title).
  final String articleTitle;

  static Route<void> route({required String url, String sourceName = '', String articleTitle = ''}) =>
      MaterialPageRoute<void>(
        fullscreenDialog: true, // Aşağıdan açılır, solda "X" gösterilir.
        builder: (_) => ArticleWebViewPage(url: url, sourceName: sourceName, articleTitle: articleTitle),
      );

  @override
  State<ArticleWebViewPage> createState() => _ArticleWebViewPageState();
}

class _ArticleWebViewPageState extends State<ArticleWebViewPage> {
  late final WebViewController _controller;
  late Uri _currentUri;
  int _progress = 0;
  bool _hasError = false;

  /// http adreslerini https'e yükseltir: Android şifresiz trafiği varsayılan olarak engeller.
  static Uri _safeUri(String raw) {
    final uri = Uri.tryParse(raw.trim()) ?? Uri();
    return uri.scheme == 'http' ? uri.replace(scheme: 'https') : uri;
  }

  @override
  void initState() {
    super.initState();
    _currentUri = _safeUri(widget.url);

    _controller = WebViewController()
      ..setJavaScriptMode(JavaScriptMode.unrestricted)
      ..setNavigationDelegate(
        NavigationDelegate(
          onProgress: (p) => mounted ? setState(() => _progress = p) : null,
          onPageStarted: (url) {
            if (!mounted) return;
            setState(() {
              _hasError = false;
              _currentUri = Uri.tryParse(url) ?? _currentUri;
            });
          },
          onPageFinished: (_) => mounted ? setState(() => _progress = 100) : null,
          onWebResourceError: (error) {
            // Yalnızca ana sayfa yüklenemezse hata ekranı göster (reklam/görsel hataları önemsiz).
            if (error.isForMainFrame ?? true) {
              if (mounted) setState(() => _hasError = true);
            }
          },
          onNavigationRequest: (request) {
            final uri = Uri.tryParse(request.url);
            if (uri == null) return NavigationDecision.prevent;
            if (uri.scheme == 'http' || uri.scheme == 'https' || uri.scheme == 'about' || uri.scheme == 'data') {
              return NavigationDecision.navigate;
            }
            // tel:, mailto:, intent:, whatsapp: vb. → ilgili uygulamada aç.
            _launchExternal(uri);
            return NavigationDecision.prevent;
          },
        ),
      );

    if (_currentUri.hasScheme && _currentUri.host.isNotEmpty) {
      _controller.loadRequest(_currentUri);
    } else {
      _hasError = true;
    }
  }

  Future<void> _launchExternal(Uri uri) async {
    try {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    } catch (_) {
      // Uygun uygulama yoksa sessizce yok say.
    }
  }

  Future<void> _share() async {
    final s = S.of(context);
    final box = context.findRenderObject() as RenderBox?;
    final text = widget.articleTitle.isEmpty
        ? '${_safeUri(widget.url)}'
        : '${widget.articleTitle}\n${_safeUri(widget.url)}';
    await SharePlus.instance.share(
      ShareParams(
        text: text,
        subject: widget.articleTitle.isEmpty ? s.appName : widget.articleTitle,
        sharePositionOrigin: box == null ? null : box.localToGlobal(Offset.zero) & box.size,
      ),
    );
  }

  Future<void> _onMenu(_MenuAction action) async {
    final s = S.of(context);
    switch (action) {
      case _MenuAction.reload:
        setState(() => _hasError = false);
        await _controller.reload();
      case _MenuAction.openInBrowser:
        await _launchExternal(_currentUri);
      case _MenuAction.copyLink:
        await Clipboard.setData(ClipboardData(text: _currentUri.toString()));
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(s.linkCopied)));
        }
    }
  }

  /// Android geri tuşu: önce WebView geçmişinde geri git, geçmiş bitince sayfayı kapat.
  Future<void> _handleBack() async {
    if (await _controller.canGoBack()) {
      await _controller.goBack();
    } else if (mounted) {
      Navigator.of(context).pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final theme = Theme.of(context);
    final isSecure = _currentUri.scheme == 'https';
    final host = _currentUri.host.replaceFirst(RegExp(r'^www\.'), '');
    final title = widget.sourceName.isNotEmpty ? widget.sourceName : host;

    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, _) {
        if (!didPop) _handleBack();
      },
      child: Scaffold(
        appBar: AppBar(
          leading: IconButton(
            icon: const Icon(Icons.close),
            tooltip: s.close,
            onPressed: () => Navigator.of(context).pop(),
          ),
          centerTitle: true,
          titleSpacing: 0,
          title: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(title, style: theme.textTheme.titleMedium, maxLines: 1, overflow: TextOverflow.ellipsis),
              Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(
                    isSecure ? Icons.lock : Icons.lock_open,
                    size: 12,
                    color: isSecure ? Colors.green.shade700 : theme.colorScheme.error,
                    semanticLabel: isSecure ? s.secureConnection : s.insecureConnection,
                  ),
                  const SizedBox(width: 4),
                  Flexible(
                    child: Text(
                      host,
                      style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
            ],
          ),
          actions: [
            IconButton(icon: const Icon(Icons.share_outlined), tooltip: s.share, onPressed: _share),
            PopupMenuButton<_MenuAction>(
              onSelected: _onMenu,
              itemBuilder: (_) => [
                PopupMenuItem(value: _MenuAction.reload, child: Text(s.reload)),
                PopupMenuItem(value: _MenuAction.openInBrowser, child: Text(s.openInBrowser)),
                PopupMenuItem(value: _MenuAction.copyLink, child: Text(s.copyLink)),
              ],
            ),
          ],
          bottom: PreferredSize(
            preferredSize: const Size.fromHeight(2),
            child: _progress < 100 && !_hasError
                ? LinearProgressIndicator(value: _progress / 100, minHeight: 2)
                : const SizedBox(height: 2),
          ),
        ),
        body: _hasError
            ? _ErrorView(
                message: s.pageLoadError,
                retryLabel: s.retry,
                browserLabel: s.openInBrowser,
                onRetry: () {
                  setState(() => _hasError = false);
                  _controller.loadRequest(_currentUri);
                },
                onOpenInBrowser: () => _launchExternal(_currentUri),
              )
            : WebViewWidget(controller: _controller),
      ),
    );
  }
}

enum _MenuAction { reload, openInBrowser, copyLink }

class _ErrorView extends StatelessWidget {
  const _ErrorView({
    required this.message,
    required this.retryLabel,
    required this.browserLabel,
    required this.onRetry,
    required this.onOpenInBrowser,
  });

  final String message;
  final String retryLabel;
  final String browserLabel;
  final VoidCallback onRetry;
  final VoidCallback onOpenInBrowser;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.wifi_off, size: 48),
            const SizedBox(height: 16),
            Text(message, textAlign: TextAlign.center),
            const SizedBox(height: 16),
            FilledButton(onPressed: onRetry, child: Text(retryLabel)),
            TextButton(onPressed: onOpenInBrowser, child: Text(browserLabel)),
          ],
        ),
      ),
    );
  }
}
