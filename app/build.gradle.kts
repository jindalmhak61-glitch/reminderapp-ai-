plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")

}

android {
    namespace = "com.shruti.reminderapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.shruti.reminderapp"
        minSdk = 26
        targetSdk = 36
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

    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures.viewBinding = true
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.google.firebase:firebase-firestore:26.0.2")
    implementation("androidx.activity:activity:1.12.0")
    testImplementation("junit:junit:4.13.2")
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-analytics")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    implementation ("com.intuit.sdp:sdp-android:1.1.1")
    implementation("io.github.sceneview:sceneview:2.2.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.10.0")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("androidx.media3:media3-ui:1.8.0")
    implementation ("io.github.ShawnLin013:number-picker:2.4.13")
    implementation ("com.github.prolificinteractive:material-calendarview:2.0.0")
    implementation ("org.threeten:threetenbp:1.6.8:no-tzdb")
    implementation("androidx.webkit:webkit:1.11.0")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0") // latest stable


    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    // Image Loading
    implementation("io.coil-kt:coil:2.4.0")

    implementation("com.google.firebase:firebase-auth")

    implementation("com.google.firebase:firebase-database")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")


}