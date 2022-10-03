plugins {
    id("com.android.library")
    id("kotlinx-serialization")
    kotlin("android")
    kotlin("kapt")
    id("com.google.devtools.ksp") version Kotlin.kspVersion
}

android {

    buildToolsVersion = AppDependency.BUILD_TOOLS_VERSION
    compileSdk = AppDependency.COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = AppDependency.MIN_SDK_VERSION
        targetSdk = AppDependency.TARGET_SDK_VERSION

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments(mapOf(
                    "room.exportSchema" to "false",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true"
                ))
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_11)
        targetCompatibility(JavaVersion.VERSION_11)
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":libtoolkit"))

    // Kotlin
    implementation(Kotlin.kotlinCore)
    implementation(Kotlin.kotlinSerialization)

    // Androidx
    implementation(AndroidX.ktx)
    implementation(AndroidX.appCompat)

    // Google
    implementation(Google.material)

    // Dagger
    implementation(Dagger.dagger)
    kapt(Dagger.daggerCompiler)

    // Retrofit
    implementation(Retrofit.retrofit)
    implementation(Retrofit.retrofitRx)
    implementation(Retrofit.retrofitKotlinSerialization)
    implementation(Retrofit.retrofitXml)

    // Rx
    implementation(Rx.androidRx)
    implementation(Rx.rxRelay)

    // Room
    implementation(Room.roomRuntime)
    implementation(Room.roomKtx)
    ksp(Room.roomCompiler)

    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}