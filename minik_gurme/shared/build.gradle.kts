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
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.materialIconsExtended)
      implementation(compose.components.resources)
      api(libs.androidx.room.runtime) // @Entity modelleri (Room KMP)
    }
    iosMain.dependencies {
      implementation(libs.androidx.sqlite.bundled)
    }
    commonTest.dependencies {
      implementation(kotlin("test"))
    }
  }
}

// Room: veritabanı kodunu her platform için üret
dependencies {
  add("kspAndroid", libs.androidx.room.compiler)
  add("kspIosArm64", libs.androidx.room.compiler)
  add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
