plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.bordeauxintersections"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bordeauxintersections"
        minSdk = 30
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Réseau
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation(libs.androidx.swiperefreshlayout)
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation(libs.androidx.preference)

    // Services de localisation Google Play
    implementation ("com.google.android.gms:play-services-location:21.0.1")

    // Pour les notifications
    implementation ("androidx.core:core:1.12.0")

    // Pour les préférences
    implementation ("androidx.preference:preference:1.2.1")

    // Déjà présentes je pense, mais au cas où :
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.lifecycle:lifecycle-runtime:2.6.2")


    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}