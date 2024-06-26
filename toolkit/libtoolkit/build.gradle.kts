plugins {
    id("com.android.library")
    id("kotlinx-serialization")
    kotlin("android")
    kotlin("kapt")
    id("com.google.devtools.ksp") version Kotlin.kspVersion
}

android {

    namespace = "lib.toolkit.base"
    compileSdk = AppDependency.COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = AppDependency.MIN_SDK_VERSION

        buildConfigField("String", "SUBDOMAIN", "\"personal\"")
        buildConfigField("String", "DEFAULT_HOST", "\"onlyoffice.com\"")
        buildConfigField("String", "DEFAULT_INFO_HOST", "\"teamlab.info\"")
        buildConfigField("String", "ROOT_FOLDER","\"" + "Onlyoffice" + "\"" )

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        debug {
            isJniDebuggable = true
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_17)
        targetCompatibility(JavaVersion.VERSION_17)
    }

    kotlinOptions {
        jvmTarget = "17"
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
    implementation(Moxy.moxyKtx)
    kapt(Moxy.moxyCompiler)

    // Dagger
    implementation(Dagger.dagger)
    ksp(Dagger.daggerCompiler)

    // Other
    implementation(Libs.glide)
    implementation(Libs.documentFile)

    // Rx
    implementation(Rx.androidRx)
    implementation(Rx.rxRelay)

    implementation(Lifecycle.viewModel)
    implementation(Lifecycle.liveData)
    implementation(Lifecycle.runtime)
}
