import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    alias(libs.plugins.google.gms.google.services)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
}
val localProps = gradleLocalProperties(rootDir,providers)
val supabaseUrl: String = localProps.getProperty("supabaseUrl") ?: ""
val supabaseKey: String = localProps.getProperty("supabaseKey") ?: ""

android {
    namespace = "com.example.proyectorecetas"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.proyectorecetas"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$supabaseKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
        buildConfig = true
    }

}
val ktorVersion = "3.0.0"
dependencies {
    //implementation("androidx.multidex:multidex:2.0.1")
    //implementation(platform("io.ktor:ktor-bom:$ktorVersion"))
    //implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-okhttp:3.0.0")
    //implementation("io.ktor:ktor-client-content-negotiation")
    //implementation("io.ktor:ktor-serialization-kotlinx-json")
    //implementation("io.ktor:ktor-client-logging")
    //implementation("io.ktor:ktor-client-http-timeout")
    //implementation("io.ktor:ktor-client-resources")


    // Dependencias de Supabase (usa BOM para control de versiones)
    implementation(platform("io.github.jan-tennert.supabase:bom:3.1.0"))
    implementation("io.github.jan-tennert.supabase:supabase-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")

    // Serialización
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")


    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.fragment:fragment-ktx:1.8.3")
    implementation ("androidx.recyclerview:recyclerview:1.2.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.preference)
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}