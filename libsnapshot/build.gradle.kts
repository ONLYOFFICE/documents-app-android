import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "lib.bootstrap.snapshot"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }

}

dependencies {
    implementation(project(":libtoolkit"))

    implementation(libs.kotlin.core)
    implementation(libs.ktx)
}

tasks.preBuild.dependsOn("copyLibbootstrap")

tasks.register("copyLibbootstrap", Copy::class) {
    description = "Copies libbootstrap_jni.so for all ABIs into jniLibs"
    group = "build"

    destinationDir = file("src/main/jniLibs")

    val libName = "libbootstrap_jni.so"
    val srcBaseDir = file("../../build_tools/out/android/onlyoffice/mobile/lib")

    val abis = mapOf(
        "arm64-v8a" to "$srcBaseDir/arm64-v8a/$libName",
        "armeabi-v7a" to "$srcBaseDir/armeabi-v7a/$libName",
        "x86_64" to "$srcBaseDir/x86_64/$libName",
        "x86" to "$srcBaseDir/x86/$libName"
    )

    abis.forEach { (abi, srcPath) ->
        val srcFile = file(srcPath)
        if (srcFile.exists()) {
            println("✅ Found $libName for $abi at $srcPath")
            from(srcFile.parent) {
                include(srcFile.name)
                into(abi)
            }
        } else {
            println("⚠️ Missing $libName for $abi at $srcPath. Did you build native libraries?")
        }
    }

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
