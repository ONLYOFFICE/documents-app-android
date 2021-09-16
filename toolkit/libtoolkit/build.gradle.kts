plugins {
    id("com.android.library")
    id("kotlinx-serialization")
    kotlin("android")
    kotlin("kapt")
}

android {
    buildToolsVersion(AppDependency.BUILD_TOOLS_VERSION)
    compileSdkVersion(AppDependency.COMPILE_SDK_VERSION)

    defaultConfig {
        minSdkVersion(AppDependency.MIN_SDK_VERSION)
        targetSdkVersion(AppDependency.TARGET_SDK_VERSION)
        versionCode(1)
        versionName("1.0")
    }

    buildTypes {
        getByName("release") {
            minifyEnabled(false)
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

        getByName("debug") {
            debuggable(true)
            jniDebuggable(true)
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Androidx
    implementation(AndroidX.appCompat)
    implementation(AndroidX.appCompatResources)
    implementation(AndroidX.ktx)

    // Kotlin
    implementation(Kotlin.kotlinCore)
    implementation(Kotlin.coroutineCore)
    implementation(Kotlin.coroutineAndroid)
    implementation(Kotlin.kotlinSerialization)

    // Google
    implementation(Google.material)
    implementation(Google.gson)

    // RecyclerView
    implementation(AndroidX.recyclerView)

    implementation(AndroidX.cardView)
    implementation(AndroidX.constraint)

    // Retrofit
    implementation(Retrofit.retrofit)
    implementation(Retrofit.retrofitGson)
    implementation(Retrofit.retrofitXml)

    // Moxy
    implementation(Moxy.moxyAndroid)
    implementation(Moxy.moxyMaterial)
    kapt(Moxy.moxyCompiler)

    // Dagger
    implementation(Dagger.dagger)
    kapt(Dagger.daggerCompiler)

    // Other
    implementation(Libs.glide)
    implementation(Libs.documentFile)
    implementation(Rx.rxRelay)

    // Rx
    implementation(Rx.androidRx)
    implementation(Rx.rxRelay)
}
