import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // compilador de Jetpack Compose (homes de asistente y expositor)
    alias(libs.plugins.kotlin.compose)
    // Kotlin serialization plugin for type safe routes and navigation arguments
    alias(libs.plugins.kotlin.serialization)  // ← Usa la versión del libs.versions.toml
    id("androidx.navigation.safeargs.kotlin")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("com.google.gms.google-services")
}
android {
    namespace = "com.example.jnab2025"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.jnab2025"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = project.findProperty("MAPS_API_KEY") ?: "MISSING_KEY"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // El codigo usa LocalDate / LocalTime, que entraron en la API 26.
        // Como el minSdk es 24, hace falta desugaring.
        isCoreLibraryDesugaringEnabled = true
    }
    
    buildFeatures {
        viewBinding = true
        // Conviven las dos: las pantallas existentes siguen en XML y los homes
        // de asistente y expositor estan hechos en Compose.
        compose = true
    }
}

dependencies {
    implementation(libs.protolite.well.known.types)
    implementation(libs.play.services.maps)
    val nav_version = "2.8.9"

    implementation("com.google.android.material:material:1.10.0")

    implementation("com.prolificinteractive:material-calendarview:1.4.3") {
        exclude(group = "com.android.support")
    }

    implementation("androidx.constraintlayout:constraintlayout:2.2.0-beta01")
    // To use constraintlayout in compose
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0-beta01")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ViewModel y LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Agregar RecyclerView y CardView para la implementación de la agenda
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // Views/Fragments integration
    implementation("androidx.navigation:navigation-fragment:$nav_version")
    implementation("androidx.navigation:navigation-ui:$nav_version")

    // Feature module support for Fragments
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")

    // Testing Navigation
    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")

    // JSON serialization library, works with the Kotlin serialization plugin
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ViewModel y LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Kotlin Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // java.time en minSdk 24
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    //Servicios de google
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    // Inicio de sesión con Google mediante Credential Manager
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    //lib para cargar la url de las imagenes
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Notificaciones: WorkManager corre el chequeo periodico de notificaciones
    // pendientes cuando la app esta en segundo plano, y reprograma los
    // recordatorios de charlas despues de un reinicio del telefono.
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Jetpack Compose: el BOM pone las versiones, por eso las libs de Compose van sin version
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    // collectAsStateWithLifecycle: leer los StateFlow de los ViewModels desde Compose
    implementation(libs.androidx.lifecycle.runtime.compose)
    // QR de la credencial del home del expositor
    implementation(libs.zxing.core)
    // Escaner de QR de la acreditacion (organizador)
    implementation(libs.play.services.code.scanner)
    // Habilita la vista previa (@Preview) en Android Studio; solo en debug
    debugImplementation(libs.androidx.compose.ui.tooling)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
