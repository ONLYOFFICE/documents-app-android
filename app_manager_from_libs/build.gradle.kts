buildscript {

    extra.apply {
        set("NDK_VERSION", libs.versions.ndk.get())
        set("kotlin_version", libs.versions.kotlin.get())
    }

    repositories {
        google()
        maven { setUrl("https://mvnrepository.com/artifact/com.github.gundy/semver4j") }
        mavenCentral()
        maven { setUrl("https://maven.google.com") }
        gradlePluginPortal()
    }

    dependencies {
        classpath(libs.gradle)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.google.services)
        classpath(libs.firebase.crashlytics.gradle)
    }
}

plugins {
    alias(libs.plugins.kotlin.serialization.plugin) apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { setUrl("https://jitpack.io") }
    }

    configurations.configureEach {
        resolutionStrategy {
            force("org.xerial:sqlite-jdbc:3.34.0")
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
