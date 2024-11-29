plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Adicionando o plugin google-services
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildToolsVersion = "34.0.0"
}

dependencies {
    // Firebase dependencies using Firebase BOM
    implementation(platform(libs.firebase.bom)) // Use the Firebase BOM to manage versions
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.storage)

    // AndroidX libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // MPAndroidChart
    implementation(libs.mpandroidchart)

    // Navigation
    implementation(libs.navigation.common)
    implementation(libs.navigation.fragment)
    implementation(libs.room.common)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Adicionando a dependência do play-services-auth
    implementation("com.google.android.gms:play-services-auth:16.0.1")

    implementation(libs.gson) // Usando o nome da dependência do libs.versions.toml
}
