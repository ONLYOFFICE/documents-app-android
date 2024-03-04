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

        //DropBox
        buildConfigField("String", "DROP_BOX_COM_CLIENT_ID","\"" + keystoreProperties["DROP_BOX_COM_CLIENT_ID"] + "\"" )
        buildConfigField("String", "DROP_BOX_INFO_CLIENT_ID","\"" + keystoreProperties["DROP_BOX_INFO_CLIENT_ID"] + "\"" )
        buildConfigField("String", "DROP_BOX_INFO_REDIRECT_URL","\"" + keystoreProperties["DROP_BOX_INFO_REDIRECT_URL"] + "\"" )
        buildConfigField("String", "DROP_BOX_COM_CLIENT_SECRET","\"" + keystoreProperties["DROP_BOX_COM_CLIENT_SECRET"] + "\"" )
        buildConfigField("String", "DROP_BOX_COM_REDIRECT_URL","\"" + keystoreProperties["DROP_BOX_COM_REDIRECT_URL"] + "\"" )
        buildConfigField("String", "DROP_BOX_AUTH_URL","\"" + keystoreProperties["DROP_BOX_AUTH_URL"] + "\"" )
        buildConfigField("String", "DROP_BOX_VALUE_RESPONSE_TYPE","\"" + keystoreProperties["DROP_BOX_VALUE_RESPONSE_TYPE"] + "\"" )

        //OneDrive
        buildConfigField("String", "ONE_DRIVE_INFO_CLIENT_ID","\"" + keystoreProperties["ONE_DRIVE_INFO_CLIENT_ID"] + "\"" )
        buildConfigField("String", "ONE_DRIVE_INFO_REDIRECT_URL","\"" + keystoreProperties["ONE_DRIVE_INFO_REDIRECT_URL"] + "\"" )
        buildConfigField("String", "ONE_DRIVE_COM_CLIENT_ID","\"" + keystoreProperties["ONE_DRIVE_COM_CLIENT_ID"] + "\"" )
        buildConfigField("String", "ONE_DRIVE_COM_CLIENT_SECRET","\"" + keystoreProperties["ONE_DRIVE_COM_CLIENT_SECRET"] + "\"" )
        buildConfigField("String", "ONE_DRIVE_COM_REDIRECT_URL","\"" + keystoreProperties["ONE_DRIVE_COM_REDIRECT_URL"] + "\"" )
        buildConfigField("String", "ONE_DRIVE_AUTH_URL","\"" + keystoreProperties["ONE_DRIVE_AUTH_URL"] + "\"" )
        buildConfigField("String", "ONE_DRIVE_VALUE_RESPONSE_TYPE","\"" + keystoreProperties["ONE_DRIVE_AUTH_URL"] + "\"" )
        buildConfigField("String", "ONE_DRIVE_VALUE_SCOPE","\"" + keystoreProperties["ONE_DRIVE_AUTH_URL"] + "\"" )

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