// Minik Gurme ortak modülü: Android ve iPhone (iOS) aynı Kotlin kodunu kullanır.
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.kotlin.compose)
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
    commonTest.dependencies {
      implementation(kotlin("test"))
    }
  }
}
