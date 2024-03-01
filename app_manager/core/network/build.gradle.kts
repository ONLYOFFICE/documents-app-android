plugins {
    id("com.android.library")
    id("kotlinx-serialization")
    kotlin("android")
    id("com.google.devtools.ksp") version Kotlin.kspVersion
}

android {
    namespace = "app.documents.core.network"
    compileSdk = AppDependency.COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = AppDependency.MIN_SDK_VERSION

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:model"))

    implementation(Kotlin.kotlinSerialization)
    implementation(Kotlin.coroutineCore)

    implementation(Dagger.dagger)
    ksp(Dagger.daggerCompiler)

    implementation(Retrofit.retrofit)
    implementation(Retrofit.retrofitKotlinSerialization)
}