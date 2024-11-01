plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization.plugin)
}

android {

    namespace = "lib.toolkit.base"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

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
    implementation(libs.appcompat)
    implementation(libs.appcompat.resources)
    implementation(libs.ktx)

    // Kotlin
    implementation(libs.kotlin.core)
    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.coroutines.android)
    implementation(libs.kotlin.serialization.json)

    // Google
    implementation(libs.google.material)
    implementation(libs.google.gson)

    // RecyclerView
    implementation(libs.recyclerView)

    implementation(libs.cardView)
    implementation(libs.constraint)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.retrofit.xml)

    // Moxy
    implementation(libs.moxy)
    implementation(libs.moxy.material)
    implementation(libs.moxy.ktx)
    kapt(libs.moxy.compiler)

    // Dagger
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)

    // Other
    implementation(libs.glide)
    implementation(libs.androidDocumentFile)

    // Rx
    implementation(libs.rx.java)
    implementation(libs.rx.relay)

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)
}
