// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Nota: usa la misma versión que estás usando en tus dependencias de navegación
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.9.0")
    }
}