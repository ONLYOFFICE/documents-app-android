import java.io.FileInputStream
import java.util.Properties

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

        val keystoreProperties = Properties()

        rootProject.file("Onlyoffice-keystore.properties").let { file ->
            if (file.exists()) {
                keystoreProperties.load(FileInputStream(file))
            }
        }

        //Google
        buildConfigField("String", "GOOGLE_INFO_CLIENT_ID","\"" + keystoreProperties["GOOGLE_INFO_CLIENT_ID"] + "\"" )
        buildConfigField("String", "GOOGLE_INFO_REDIRECT_URL","\"" + keystoreProperties["GOOGLE_INFO_REDIRECT_URL"] + "\"" )
        buildConfigField("String", "GOOGLE_COM_CLIENT_ID","\"" + keystoreProperties["GOOGLE_COM_CLIENT_ID"] + "\"" )
        buildConfigField("String", "GOOGLE_COM_CLIENT_SECRET","\"" + keystoreProperties["GOOGLE_COM_CLIENT_SECRET"] + "\"" )
        buildConfigField("String", "GOOGLE_COM_REDIRECT_URL","\"" + keystoreProperties["GOOGLE_COM_REDIRECT_URL"] + "\"" )
        buildConfigField("String", "GOOGLE_AUTH_URL","\"" + keystoreProperties["GOOGLE_AUTH_URL"] + "\"" )
        buildConfigField("String", "GOOGLE_VALUE_RESPONSE_TYPE","\"" + keystoreProperties["GOOGLE_VALUE_RESPONSE_TYPE"] + "\"" )
        buildConfigField("String", "GOOGLE_VALUE_ACCESS_TYPE","\"" + keystoreProperties["GOOGLE_VALUE_ACCESS_TYPE"] + "\"" )
        buildConfigField("String", "GOOGLE_VALUE_APPROVAL_PROMPT","\"" + keystoreProperties["GOOGLE_VALUE_APPROVAL_PROMPT"] + "\"" )
        buildConfigField("String", "GOOGLE_VALUE_SCOPE","\"" + keystoreProperties["GOOGLE_VALUE_SCOPE"] + "\"" )
        buildConfigField("String", "GOOGLE_WEB_ID","\"" + keystoreProperties["GOOGLE_WEB_ID"] + "\"" )
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
    implementation(project(":core:model"))

    implementation(Kotlin.kotlinSerialization)
    implementation(Kotlin.coroutineCore)

    implementation(Dagger.dagger)
    ksp(Dagger.daggerCompiler)

    implementation(Retrofit.retrofit)
    implementation(Retrofit.retrofitKotlinSerialization)
}