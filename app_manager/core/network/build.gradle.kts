plugins {
    id("com.android.library")
    id("kotlinx-serialization")
    kotlin("android")
    id("com.google.devtools.ksp") version Kotlin.kspVersion
}

android {
    namespace = "app.documents.core.network"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation(project(":core"))

    implementation(Kotlin.kotlinSerialization)
    implementation(Kotlin.coroutineCore)

    implementation(Dagger.dagger)
    ksp(Dagger.daggerCompiler)

    implementation(Retrofit.retrofit)
    implementation(Retrofit.retrofitKotlinSerialization)
}