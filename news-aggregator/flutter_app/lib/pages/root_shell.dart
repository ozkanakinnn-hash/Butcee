import 'package:flutter/material.dart';

import '../ads/ad_widgets.dart';
import '../app_settings.dart';
import '../backend/news_repository.dart';
import '../l10n/app_strings.dart';
import 'home_page.dart';
import 'interests_page.dart';
import 'notifications_page.dart';
import 'search_page.dart';

/// Alt menü: Ana Sayfa · Ara · Bildirimler · İlgi Alanları. Sekmeler durumunu korur (IndexedStack).
class RootShell extends StatefulWidget {
  const RootShell({super.key, required this.settings, this.repository = const NewsRepository()});

  final AppSettings settings;
  final NewsRepository repository;

  @override
  State<RootShell> createState() => _RootShellState();
}

class _RootShellState extends State<RootShell> {
  int _index = 0;

  @override
  Widget build(BuildContext context) {
    final s = S.of(context);
    final pages = [
      HomePage(settings: widget.settings, repository: widget.repository),
      SearchPage(settings: widget.settings, repository: widget.repository),
      NotificationsPage(settings: widget.settings, repository: widget.repository),
      InterestsPage(settings: widget.settings, repository: widget.repository, embedded: true),
    ];
    return Scaffold(
      body: IndexedStack(index: _index, children: pages),
      bottomNavigationBar: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const BottomBannerAd(),
          NavigationBar(
            selectedIndex: _index,
            onDestinationSelected: (i) => setState(() => _index = i),
            labelBehavior: NavigationDestinationLabelBehavior.alwaysHide,
            height: 64,
            destinations: [
              NavigationDestination(
                icon: const Icon(Icons.home_outlined),
                selectedIcon: const Icon(Icons.home_rounded),
                label: s.home,
              ),
              NavigationDestination(
                icon: const Icon(Icons.search_rounded),
                selectedIcon: const Icon(Icons.saved_search_rounded),
                label: s.search,
              ),
              NavigationDestination(
                icon: const Icon(Icons.notifications_none_rounded),
                selectedIcon: const Icon(Icons.notifications_rounded),
                label: s.notifications,
              ),
              NavigationDestination(
                icon: const Icon(Icons.dashboard_customize_outlined),
                selectedIcon: const Icon(Icons.dashboard_customize_rounded),
                label: s.interests,
              ),
            ],
          ),
        ],
      ),
    );
  }
}
