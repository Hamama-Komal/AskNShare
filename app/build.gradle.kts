plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.asknshare"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.asknshare"
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
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Animation
    implementation("com.daimajia.androidanimations:library:2.4@aar")
    // SpinKit
    implementation("com.github.ybq:Android-SpinKit:1.4.0")
    // ssp
    implementation(libs.intuit.ssp.android)
    // sdp
    implementation(libs.sdp.android)
    // Smooth Bottom Bar
    implementation("com.github.ibrahimsn98:SmoothBottomBar:1.7.6")
    // Dots Indicator
    implementation("com.tbuonomo:dotsindicator:5.1.0")
    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // Datastore
    implementation("androidx.datastore:datastore-preferences:1.1.2")
    // Room
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    val lifecycle_version = "2.8.7"
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")



    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib
    // implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.20-Beta2")


}