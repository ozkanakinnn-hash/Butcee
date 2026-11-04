import 'dart:io';

import 'package:integration_test/integration_test_driver_extended.dart';

/// `flutter drive` sürücüsü: testin aldığı ekran görüntülerini screenshots/ altına yazar.
Future<void> main() => integrationDriver(
      onScreenshot: (name, bytes, [args]) async {
        final f = File('screenshots/$name.png');
        f.parent.createSync(recursive: true);
        f.writeAsBytesSync(bytes);
        return true;
      },
    );
