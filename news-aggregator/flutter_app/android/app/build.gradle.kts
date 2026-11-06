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

    buildTypes {
        release {
            // TODO: Add your own signing config for the release build.
            // Signing with the debug keys for now, so `flutter run --release` works.
            signingConfig = signingConfigs.getByName("debug")
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
