plugins {
    alias(libs.plugins.android.application)

}

android {
    namespace = "com.example.test3"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.test3"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_9
        targetCompatibility = JavaVersion.VERSION_1_9
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }

}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation ("androidx.appcompat:appcompat:1.7.0")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.2.0")

    // PhotoView for zoom and pan functionality
    implementation ("com.github.chrisbanes:PhotoView:2.3.0")

    // Lifecycle components
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
    implementation ("androidx.lifecycle:lifecycle-livedata:2.8.7")

    // Navigation components
    implementation ("androidx.navigation:navigation-fragment:2.8.3")
    implementation ("androidx.navigation:navigation-ui:2.8.3")

    // RecyclerView and CardView
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("androidx.cardview:cardview:1.0.0")

    // Testing
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.2.1")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.6.1")


    implementation ("com.github.bumptech.glide:glide:4.12.0")

    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    implementation ("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("com.makeramen:roundedimageview:2.3.0")

    implementation("com.github.ismaeldivita:chip-navigation-bar:1.3.4")

}