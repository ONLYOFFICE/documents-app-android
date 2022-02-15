import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.io.FileInputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("com.android.application")
    id("com.google.firebase.crashlytics")
    id("kotlinx-serialization")
    kotlin("android")
    kotlin("kapt")
}

// Onlyoffice
val appId = "com.onlyoffice.documents"
val appIdBeta = "$appId.beta"
val appName = "onlyoffice-manager"

/*
* Create a variable called keystorePropertiesFile, and initialize it to your
* keystore.properties file, in the rootProject folder.
* Example of file content:
*   storePassword=password
*   keyPassword=password
*   keyAlias=AliasInKeyStore
*   storeFile=C:/example/MyAndroidKeys.jks
*/

fun getKeystore(filePath: String): Properties {
    // Initialize a new Properties() object called keystoreProperties.
    val keystoreProperties = Properties()

    // You can place here passwords and path to keystore instead of file properties
    keystoreProperties["keyAlias"] = "<YOUR_ALIAS>"
    keystoreProperties["keyPassword"] = "<YOUR_PASSWORD>"
    keystoreProperties["storeFile"] = "<PATH_TO_KEYSTORE_FILE>"
    keystoreProperties["storePassword"] = "<KEYSTORE_PASSWORD>"

    // Get file with properties
    val keystorePropertiesFile = rootProject.file(filePath)
    // File check to exist for success script building
    if (keystorePropertiesFile.exists()) {
        // Load your keystore.properties file into the keystoreProperties object.
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    } else {
        val writer = FileWriter(keystorePropertiesFile, false)
        keystoreProperties.store(writer, "Google keystore file")
    }

    return keystoreProperties
}

android {

    buildToolsVersion = AppDependency.BUILD_TOOLS_VERSION
    compileSdk = AppDependency.COMPILE_SDK_VERSION

    defaultConfig {
        manifestPlaceholders += mapOf()
        minSdk = AppDependency.MIN_SDK_VERSION
        targetSdk = AppDependency.TARGET_SDK_VERSION
        versionCode = 355
        versionName = "5.3.1"
        multiDexEnabled = true
        applicationId = "com.onlyoffice.documents"
        manifestPlaceholders["permissionId"] = appIdBeta

        buildConfigField("boolean", "IS_BETA", "false")
        buildConfigField("String", "RELEASE_ID", "\"" + appId + "\"")
        buildConfigField("String", "BETA_ID", "\"" + appIdBeta + "\"")
        buildConfigField("String", "APP_NAME", "\"" + appName + "\"")


        ndk {
            abiFilters.addAll(arrayOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))  //comment to armv7
        }
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64") //comment to armv7
            isUniversalApk = true
        }
    }

    signingConfigs {
        create("onlyoffice") {
            val keystoreProperties = getKeystore("Onlyoffice-keystore.properties")
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] ?: "")
            storePassword = keystoreProperties["storePassword"] as String
        }
    }
    buildTypes {

        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")

            signingConfig = signingConfigs.getByName("onlyoffice")
        }

        debug {
            isMinifyEnabled = false
        }

        applicationVariants.all {
            val variant = this
            variant.outputs
                .map { it as BaseVariantOutputImpl }
                .forEach { output ->
                    val timeMark = SimpleDateFormat("MMMMM.dd_HH-mm").format(Date())
                    val buildAbi = output.filters.find { it.filterType == "ABI" }?.identifier
                    val buildType = if (buildType.isDebuggable) "debug" else "release"
                    val buildCode = "_build-${output.versionCode}"

                    output.outputFileName = "${appName}-${versionName}-" +
                            "${flavorName.toUpperCase()}-" +
                            "${buildAbi}-${buildType}${buildCode}${timeMark}.apk"

                }
        }

    }

    tasks.preBuild {
        doFirst {
            delete(fileTree(mapOf("dir" to "build", "include" to listOf("**/*.apk"))))
        }
    }


    lint {
        isCheckReleaseBuilds = false
        isAbortOnError = false
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    bundle {
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
        language {
            enableSplit = false
        }
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        arrayOf("armeabi-v7a", "x86", "arm64-v8a", "x86_64").forEach { abi ->
            jniLibs.pickFirsts.add("lib/$abi/lib${extra.get("NAME_LIB_DJVUFILE")}.so")
            jniLibs.pickFirsts.add("lib/$abi/lib${extra.get("NAME_LIB_DOCTRENDERER")}.so")
            jniLibs.pickFirsts.add("lib/$abi/lib${extra.get("NAME_LIB_GRAPHICS")}.so")
            jniLibs.pickFirsts.add("lib/$abi/lib${extra.get("NAME_LIB_HTMLFILE")}.so")
            jniLibs.pickFirsts.add("lib/$abi/lib${extra.get("NAME_LIB_HTMLRENDERER")}.so")
            jniLibs.pickFirsts.add("lib/$abi/lib${extra.get("NAME_LIB_KERNEL")}.so")
            jniLibs.pickFirsts.add("lib/$abi/lib${extra.get("NAME_LIB_PDFREADER")}.so")
            jniLibs.pickFirsts.add("lib/$abi/lib${extra.get("NAME_LIB_PDFWRITER")}.so")
            jniLibs.pickFirsts.add("lib/$abi/lib${extra.get("NAME_LIB_UNICODECONVERTER")}.so")
            jniLibs.pickFirsts.add("lib/$abi/lib${extra.get("NAME_LIB_X2T")}.so")
            jniLibs.pickFirsts.add("lib/$abi/lib${extra.get("NAME_LIB_XPSFILE")}.so")
            jniLibs.pickFirsts.add("lib/$abi/lib${extra.get("NAME_LIB_FB2FILE")}.so")
            jniLibs.pickFirsts.add("lib/$abi/lib${extra.get("NAME_LIB_EPUBFILE")}.so")
            jniLibs.pickFirsts.add("lib/$abi/lib${extra.get("NAME_LIB_KERNEL_NETWORK")}.so")
            jniLibs.pickFirsts.add("lib/$abi/lib${extra.get("NAME_LIB_DOCX_RENDERER")}.so")
        }
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Compose.version
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":core"))
    implementation(project(":libtoolkit"))
    implementation(project(":libx2t"))
    implementation(project(":libeditors"))
    implementation(project(":libslides"))
    implementation(project(":libdocs"))
    implementation(project(":libcells"))
    implementation(project(":libgeditors"))
    implementation(project(":libgslides"))
    implementation(project(":libgdocs"))
    implementation(project(":libgcells"))

    // Google libs
    implementation(Firebase.firebaseCore)
    implementation(Firebase.firebaseConfig)
    implementation(Firebase.firebaseCrashlytics)
    implementation(Firebase.firebaseMessaging)
    implementation(Google.playCore)
    implementation(Google.playServiceAuth)
    implementation(Google.material)
    implementation(Google.gson)
    implementation(Google.safetynet)

    // Androidx
    implementation(AndroidX.appCompat)
    implementation(AndroidX.biometric)
    // RecyclerView
    implementation(AndroidX.recyclerView)
    implementation(AndroidX.recyclerViewSelection)

    implementation(AndroidX.cardView)
    implementation(AndroidX.constraint)

    // Dagger
    implementation(Dagger.dagger)
    kapt(Dagger.daggerCompiler)

    // Retrofit
    implementation(Retrofit.retrofit)
    implementation(Retrofit.retrofitGson)
    implementation(Retrofit.retrofitXml)
    implementation(Retrofit.retrofitRx)
    implementation(Retrofit.retrofitKotlinSerialization)

    // Moxy
    implementation(Moxy.moxyAndroid)
    implementation(Moxy.moxyMaterial)
    kapt(Moxy.moxyCompiler)

    // Kotlin
    implementation(Kotlin.kotlinCore)
    implementation(Kotlin.coroutineCore)
    implementation(Kotlin.coroutineAndroid)
    implementation(Kotlin.kotlinSerialization)

    // RX
    implementation(Rx.androidRx)
    implementation(Rx.rxRelay)

    // Room
    implementation(Room.roomRuntime)
    implementation(Room.roomKtx)
    kapt(Room.roomCompiler)

    // Other
    implementation(Libs.phoneNumber)
    implementation(Libs.facebookLogin)
    implementation(Libs.pageIndicator)
    implementation(Libs.glide)
    implementation(Libs.photoView)
    implementation(Libs.androidWork)

    //TODO add to base module
    implementation(Lifecycle.viewModel)
    implementation(Lifecycle.liveData)
    implementation(Lifecycle.runtime)

    implementation("androidx.fragment:fragment-ktx:1.4.0")

    //Compose
    implementation(Compose.ui)
    implementation(Compose.material)
    implementation(Compose.preview)
    debugImplementation(Compose.tooling)
    implementation(Compose.navigation)
    implementation(AndroidX.composeActivity)
    implementation( Compose.liveData)

    //Jackson
    implementation (Jackson.core)
    implementation (Jackson.annotations)
    implementation (Jackson.databind)
}

apply(plugin = "com.google.gms.google-services")


