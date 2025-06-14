plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.gruas"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gruas"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    android {
        buildFeatures {
            viewBinding = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Implementaciones para obtener los datos de la API
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Biblioteca de Google Maps para Android
    implementation ("com.google.android.gms:play-services-maps:18.0.2")

    // Utilidades de Google Maps para funciones como `PolyUtil`
    implementation ("com.google.maps.android:android-maps-utils:2.4.0")
    implementation ("com.google.android.material:material:1.11.0")

    // Biblioteca OkHttp para solicitudes HTTP
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")

}