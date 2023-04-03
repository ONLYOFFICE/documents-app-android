@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    buildToolsVersion = AppDependency.BUILD_TOOLS_VERSION
    compileSdk = AppDependency.COMPILE_SDK_VERSION
    defaultConfig {
        minSdk = AppDependency.MIN_SDK_VERSION
        targetSdk = AppDependency.TARGET_SDK_VERSION

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_11)
        targetCompatibility(JavaVersion.VERSION_11)
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Compose.versionCompiler
    }
}

dependencies {
    implementation(project(":libtoolkit"))

    implementation(Kotlin.kotlinCore)

    implementation(Compose.ui)
    implementation(Compose.material)
    implementation(Compose.preview)
    debugImplementation(Compose.tooling)
}