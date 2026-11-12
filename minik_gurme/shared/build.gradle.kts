// Minik Gurme ortak modülü: Android ve iPhone (iOS) aynı Kotlin kodunu kullanır.
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
}

kotlin {
  androidLibrary {
    namespace = "akinai.minikgurme.shared"
    compileSdk = 36
    minSdk = 24
    // Compose kaynakları (görseller, yazı tipleri, sözlük) Android'de de paketlensin
    experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
  }

  listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
    target.binaries.framework {
      baseName = "Shared"
      isStatic = true
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(libs.kotlinx.coroutines.core)
      api(compose.runtime)
      api(compose.foundation)
      api(compose.material3)
      api(compose.materialIconsExtended)
      api(compose.components.resources)
      implementation(libs.kotlinx.serialization.json)
      api(libs.androidx.room.runtime) // @Entity modelleri (Room KMP)
    }
    androidMain.dependencies {
      implementation(libs.androidx.core.ktx)
    }
    iosMain.dependencies {
      implementation(libs.androidx.sqlite.bundled)
    }
    commonTest.dependencies {
      implementation(kotlin("test"))
    }
  }
}

compose.resources {
  publicResClass = true
  packageOfResClass = "akinai.minikgurme.resources"
  generateResClass = always
}

// Room: veritabanı kodunu her platform için üret
dependencies {
  add("kspAndroid", libs.androidx.room.compiler)
  add("kspIosArm64", libs.androidx.room.compiler)
  add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
