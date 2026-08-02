buildscript {
  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-metadata-jvm:2.1.0")
  }
}

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
  alias(libs.plugins.dagger.hilt.android) apply false
}
