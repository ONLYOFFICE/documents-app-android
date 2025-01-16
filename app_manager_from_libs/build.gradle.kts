buildscript {

    extra.apply {
        set("NDK_VERSION", libs.versions.ndk.get())
        set("kotlin_version", libs.versions.kotlin.get())
    }

    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://maven.google.com") }
        gradlePluginPortal()
    }

    dependencies {
        classpath(libs.gradle)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.google.services)
        classpath(libs.firebase.crashlytics.gradle)
//        classpath("org.owasp:dependency-check-gradle:6.5.0.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { setUrl("https://jitpack.io") }
    }
    configurations.all {
        exclude(group = "io.netty")
        exclude(group = "io.grpc")
    }
}

plugins {
    alias(libs.plugins.kotlin.serialization.plugin) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
