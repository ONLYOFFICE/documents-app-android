@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.io.FileInputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.firebase.crashlytics")
    id("kotlinx-serialization")
    kotlin("android")
    kotlin("kapt")
    alias(libs.plugins.kotlin.ksp)
//    id("org.owasp.dependencycheck")
}

val withEditors: Boolean = project.findProperty("withEditors")?.toString()?.toBoolean() ?: true

// Onlyoffice
val appId = "com.onlyoffice.documents"
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

    namespace = "app.editors.manager"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        manifestPlaceholders += mapOf()
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 644
        versionName = "9.0.4"
        multiDexEnabled = true
        applicationId = "com.onlyoffice.documents"

        vectorDrawables {
            useSupportLibrary = true
        }

        val keystoreProperties = getKeystore("Onlyoffice-keystore.properties")

        manifestPlaceholders["facebookId"] = keystoreProperties["FACEBOOK_APP_ID"] as String? ?: ""
        manifestPlaceholders["dropboxKey"] = keystoreProperties["DROP_BOX_COM_CLIENT_ID"] as? String ?: ""

        buildConfigField("boolean", "IS_BETA", "false")
        buildConfigField("String", "RELEASE_ID", "\"" + appId + "\"")
        buildConfigField("String", "APP_NAME", "\"" + appName + "\"")
        buildConfigField("String", "COMMUNITY_ID", "\"" + keystoreProperties["COMMUNITY_ID"] + "\"")

        //Twitter
        buildConfigField(
            "String",
            "TWITTER_CONSUMER_SECRET",
            "\"" + keystoreProperties["TWITTER_CONSUMER_SECRET"] + "\""
        )
        buildConfigField("String", "TWITTER_CONSUMER_KEY", "\"" + keystoreProperties["TWITTER_CONSUMER_KEY"] + "\"")

        //Captcha
        buildConfigField(
            "String",
            "CAPTCHA_PUBLIC_KEY_INFO",
            "\"" + keystoreProperties["CAPTCHA_PUBLIC_KEY_INFO"] + "\""
        )
        buildConfigField("String", "CAPTCHA_PUBLIC_KEY_COM", "\"" + keystoreProperties["CAPTCHA_PUBLIC_KEY_COM"] + "\"")

        //Facebook
        buildConfigField("String", "FACEBOOK_APP_ID_INFO", "\"" + keystoreProperties["FACEBOOK_APP_ID_INFO"] + "\"")
        buildConfigField("String", "FACEBOOK_APP_ID", "\"" + keystoreProperties["FACEBOOK_APP_ID"] + "\"")

        //Tasks
        manifestPlaceholders["tasks"] = keystoreProperties["CUSTOM_TASKS"] as String? ?: ""

        buildConfigField("String", "PUSH_SCHEME", "\"" + "oodocuments" + "\"")
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
//            isMinifyEnabled = true
//            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")

            signingConfig = signingConfigs.getByName("onlyoffice")
            ndk {
                abiFilters.addAll(arrayOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
            }
        }

        debug {
            isMinifyEnabled = false
            extra["enableCrashlytics"] = false
            ndk {
                if (System.getProperty("os.arch") == "aarch64") {
                    abiFilters.add("arm64-v8a")
                } else {
                    abiFilters.addAll(arrayOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
                }
            }
        }

        applicationVariants.all {
            val variant = this
            variant.outputs
                .map { it as BaseVariantOutputImpl }
                .forEach { output ->
                    val timeMark = SimpleDateFormat("MMMMM.dd_HH-mm", Locale.ENGLISH).format(Date())
                    val buildAbi = output.filters.find { it.filterType == "ABI" }?.identifier
                    val buildType = if (buildType.isDebuggable) "debug" else "release"
                    val buildCode = "_build-${output.versionCode}"

                    output.outputFileName = "${appName}-${versionName}-" +
                            "${flavorName.uppercase()}-" +
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
        checkReleaseBuilds = false
        abortOnError = false
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs.useLegacyPackaging = true
        arrayOf("armeabi-v7a", "x86", "arm64-v8a", "x86_64").forEach { abi ->
            jniLibs.pickFirsts.add("lib/$abi/libc++_shared.so")
        }
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":core"))
    implementation(project(":core:model"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
    implementation(project(":libcompose"))
    implementation(project(":libtoolkit"))
    // Dynamic connection of editors
    if (withEditors) {
        val editorModules = listOf(
            ":libx2t",
            ":libeditors",
            ":libcells",
            ":libdocs",
            ":libslides",
            ":libgeditors",
            ":libgcells",
            ":libgdocs",
            ":libgslides"
        )

        editorModules.forEach { modulePath ->
            try {
                implementation(project(modulePath))
                println("✅ The $modulePath editor module is enabled")
            } catch (e: UnknownProjectException) {
                println("⚠️ The $modulePath editor module is missing and will be skipped.")
            }
        }
    } else {
        println("ℹ️ Build mode without editors")
    }

    // Firebase
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.core)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.config)

    // Google libs
    implementation(libs.google.playReview)
    implementation(libs.google.playServiceAuth)
    implementation(libs.google.material)
    implementation(libs.google.gson)
    implementation(libs.google.safetynet)
    implementation(libs.google.update)

    // Androidx
    implementation(libs.appcompat)
    implementation(libs.biometric)
    implementation(libs.fragmentKtx)

    // RecyclerView
    implementation(libs.recyclerView)
    implementation(libs.recyclerViewSelection)

    implementation(libs.cardView)
    implementation(libs.constraint)

    // Dagger
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.retrofit.xml) { exclude(group="xpp3", module= "xpp3" )}
    implementation(libs.retrofit.rx)
    implementation(libs.retrofit.kotlin.serialization)

    // Moxy
    implementation(libs.moxy)
    implementation(libs.moxy.material)
    implementation(libs.moxy.ktx)
    kapt(libs.moxy.compiler)

    // Kotlin
    implementation(libs.kotlin.core)
    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.coroutines.android)
    implementation(libs.kotlin.serialization.json)

    // RX
    implementation(libs.rx.java)
    implementation(libs.rx.relay)

    // Room
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    // Other
    implementation(libs.phoneNumber)
    implementation(libs.facebookLogin)
    implementation(libs.pageIndicator)
    implementation(libs.glide)
    implementation(libs.glideCompose)
    implementation(libs.photoView)
    implementation(libs.androidWorkManager)

    //TODO add to base module
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)

    //Compose
    implementation(libs.compose.ui)
    implementation(libs.compose.material)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.compose.navigation)
    implementation(libs.compose.livedata)
    implementation(libs.compose.constraint.layout)
    debugImplementation(libs.compose.uiTooling)

    //Jackson
    implementation(libs.jackson)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.databind)
}

apply(plugin = "com.google.gms.google-services")

//tasks.register("copySamples") {
//    println("Copy samples")
//
//    val documentSamplesPath = "../../../document-templates/sample"
//    val documentNewPath = "../../../document-templates/new"
//
//    val assetsSamplePath = projectDir.absolutePath + "/src/main/assets/samples"
//    val assetsNewPath = projectDir.absolutePath + "/src/main/assets/templates"
//
//    if (!File(assetsSamplePath).exists()) {
//        File(assetsSamplePath).mkdirs()
//    }
//
//    if (!File(assetsNewPath).exists()) {
//        File(assetsNewPath).mkdirs()
//    }
//
//    copy {
//        from(documentSamplesPath)
//        into(assetsSamplePath)
//    }
//
//    copy {
//        from(documentNewPath)
//        into(assetsNewPath)
//    }
//}

