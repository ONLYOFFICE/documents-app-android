import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.library")
    id("kotlinx-serialization")
    kotlin("android")
    kotlin("kapt")
    id("com.google.devtools.ksp") version Kotlin.kspVersion
}

android {

    namespace = "app.documents.core"

    compileSdk = AppDependency.COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = AppDependency.MIN_SDK_VERSION

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
        sourceCompatibility(JavaVersion.VERSION_17)
        targetCompatibility(JavaVersion.VERSION_17)
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
    implementation(Kotlin.kotlinCore)
    implementation(Kotlin.kotlinSerialization)
    implementation(Kotlin.coroutineCore)
    implementation(Kotlin.coroutineAndroid)

    // Androidx
    implementation(AndroidX.ktx)
    implementation(AndroidX.appCompat)
    implementation(Lifecycle.runtime)

    // Google
    implementation(Google.material)
    implementation(Firebase.firebaseMessaging)

    // Dagger
    implementation(Dagger.dagger)
    ksp(Dagger.daggerCompiler)

    // Koin
    implementation(Koin.koinAndroid)

    // Retrofit
    implementation(Retrofit.retrofit)
    implementation(Retrofit.retrofitRx)
    implementation(Retrofit.retrofitKotlinSerialization)
    implementation(Retrofit.retrofitXml)
    implementation(Retrofit.retrofitGson)

    // Dropbox
    implementation(Libs.dropboxSdk)

    // Rx
    implementation(Rx.androidRx)
    implementation(Rx.rxRelay)

    // Room
    implementation(Room.roomRuntime)
    implementation(Room.roomKtx)
    ksp(Room.roomCompiler)

    //noinspection KaptUsageInsteadOfKsp
    kapt("com.github.bumptech.glide:compiler:4.12.0")
    implementation(Libs.glide)
    implementation(Libs.glideOkHttpIntegration) { exclude("glide-parent") }

    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}