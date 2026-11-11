// Ramazan Rehberi — Wear OS (akıllı saat) uygulaması.
// Telefon uygulamasıyla AYNI applicationId ve imzayı kullanır (Wear Data Layer eşleşmesi için şart).
plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
}

android {
  namespace = "com.example.wear"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.ozkanakin.ramazanrehberi"
    minSdk = 30
    targetSdk = 36
    versionCode = 1_000_000 + (project.findProperty("APP_VERSION_CODE") as String).toInt()
    versionName = project.findProperty("APP_VERSION_NAME") as String
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures { compose = true }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.ui)
  implementation(libs.wear.compose.material)
  implementation(libs.wear.compose.foundation)
  implementation(libs.play.services.wearable)
  implementation(libs.kotlinx.coroutines.android)
}
