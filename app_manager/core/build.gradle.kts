import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.library")
    id("kotlinx-serialization")
    kotlin("android")
    kotlin("kapt")
    alias(libs.plugins.kotlin.ksp)
}

android {

    namespace = "app.documents.core"

    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

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

        val keystoreProperties = Properties()

        rootProject.file("Onlyoffice-keystore.properties").let { file ->
            if (file.exists()) {
                keystoreProperties.load(FileInputStream(file))
            }
        }

        //Box
        buildConfigField("String", "BOX_INFO_CLIENT_ID","\"" + keystoreProperties["BOX_INFO_CLIENT_ID"] + "\"" )
        buildConfigField("String", "BOX_INFO_REDIRECT_URL","\"" + keystoreProperties["BOX_INFO_REDIRECT_URL"] + "\"" )
        buildConfigField("String", "BOX_COM_CLIENT_ID","\"" + keystoreProperties["BOX_COM_CLIENT_ID"] + "\"" )
        buildConfigField("String", "BOX_COM_REDIRECT_URL","\"" + keystoreProperties["BOX_COM_REDIRECT_URL"] + "\"" )
        buildConfigField("String", "BOX_AUTH_URL","\"" + keystoreProperties["BOX_AUTH_URL"] + "\"" )
        buildConfigField("String", "BOX_VALUE_RESPONSE_TYPE","\"" + keystoreProperties["BOX_VALUE_RESPONSE_TYPE"] + "\"" )
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

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":libtoolkit"))
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))

    // Kotlin
    implementation(libs.kotlin.core)
    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.coroutines.android)
    implementation(libs.kotlin.serialization.json)

    // Androidx
    implementation(libs.ktx)
    implementation(libs.appcompat)
    implementation(libs.lifecycle.runtime)

    // Firebase
    implementation(libs.firebase.messaging)

    // Google
    implementation(libs.google.material)

    // Dagger
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)

    // Koin
    implementation(libs.koin.android)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.retrofit.xml)
    implementation(libs.retrofit.rx)
    implementation(libs.retrofit.kotlin.serialization)

    // Rx
    implementation(libs.rx.java)
    implementation(libs.rx.relay)

    // Room
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    //noinspection KaptUsageInsteadOfKsp
    ksp(libs.glideKsp)
    implementation(libs.glide)
    implementation(libs.glideOkhttp) { exclude("glide-parent") }

    implementation(libs.jackson)

    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}