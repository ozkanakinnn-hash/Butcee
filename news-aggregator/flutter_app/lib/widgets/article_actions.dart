import 'package:flutter/material.dart';
import 'package:share_plus/share_plus.dart';

import '../ads/ads_service.dart';
import '../backend/schema/articles_record.dart';
import '../l10n/app_strings.dart';
import '../pages/article_webview_page.dart';
import '../services/library_store.dart';

/// Haberi açar: okuma geçmişine ekler, uygulama içi tarayıcıda gösterir; kapanınca (aralıklı) geçiş reklamı.
Future<void> openArticle(BuildContext context, ArticlesRecord a) async {
  LibraryStore.instance.markRead(a).ignore();
  await Navigator.of(context).push(
    ArticleWebViewPage.route(url: a.originalUrl, sourceName: a.sourceName, articleTitle: a.title, article: a),
  );
  AdsService.instance.onArticleClosed();
}

/// Kaydet / kaydedilenlerden çıkar + kısa bilgi çubuğu (çıkarınca "Geri al").
Future<void> toggleSavedWithFeedback(BuildContext context, ArticlesRecord a) async {
  final s = S.of(context);
  final messenger = ScaffoldMessenger.maybeOf(context);
  final nowSaved = await LibraryStore.instance.toggleSaved(a);
  messenger
    ?..hideCurrentSnackBar()
    ..showSnackBar(
      SnackBar(
        content: Text(nowSaved ? s.savedToast : s.unsavedToast),
        behavior: SnackBarBehavior.floating,
        duration: const Duration(seconds: 2),
        action: nowSaved ? null : SnackBarAction(label: s.undo, onPressed: () => LibraryStore.instance.toggleSaved(a)),
      ),
    );
}

Future<void> shareArticle(BuildContext context, ArticlesRecord a) async {
  final box = context.findRenderObject() as RenderBox?;
  await SharePlus.instance.share(
    ShareParams(
      text: '${a.title}\n${a.originalUrl}',
      subject: a.title,
      sharePositionOrigin: box == null ? null : box.localToGlobal(Offset.zero) & box.size,
    ),
  );
}

/// Haber kartındaki "⋮" menüsü: kaydet, paylaş, bu kaynağı gizle.
Future<void> showArticleOptions(BuildContext context, ArticlesRecord a) {
  final s = S.of(context);
  final library = LibraryStore.instance;
  final messenger = ScaffoldMessenger.maybeOf(context);
  return showModalBottomSheet<void>(
    context: context,
    showDragHandle: true,
    builder: (sheet) {
      final saved = library.isSaved(a);
      return SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(24, 0, 24, 8),
              child: Text(
                a.title,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: Theme.of(sheet).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w700),
              ),
            ),
            ListTile(
              key: const ValueKey('option-save'),
              leading: Icon(saved ? Icons.bookmark_remove_outlined : Icons.bookmark_add_outlined),
              title: Text(saved ? s.unsave : s.save),
              onTap: () {
                Navigator.of(sheet).pop();
                toggleSavedWithFeedback(context, a);
              },
            ),
            ListTile(
              leading: const Icon(Icons.share_outlined),
              title: Text(s.share),
              onTap: () {
                Navigator.of(sheet).pop();
                shareArticle(context, a);
              },
            ),
            if (a.sourceName.isNotEmpty)
              ListTile(
                key: const ValueKey('option-mute'),
                leading: const Icon(Icons.visibility_off_outlined),
                title: Text(s.muteSource(a.sourceName)),
                subtitle: Text(s.muteSourceHint),
                onTap: () {
                  Navigator.of(sheet).pop();
                  library.muteSource(a.sourceName);
                  messenger
                    ?..hideCurrentSnackBar()
                    ..showSnackBar(
                      SnackBar(
                        content: Text(s.sourceMuted(a.sourceName)),
                        behavior: SnackBarBehavior.floating,
                        action: SnackBarAction(label: s.undo, onPressed: () => library.unmuteSource(a.sourceName)),
                      ),
                    );
                },
              ),
            const SizedBox(height: 8),
          ],
        ),
      );
    },
  );
}
