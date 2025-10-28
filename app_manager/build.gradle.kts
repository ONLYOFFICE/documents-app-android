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

apply(from = "buildSrc/translations.gradle.kts")

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

tasks.register("clearAssets") {
    childProjects.forEach { (projectKey, project) ->
        if (projectKey == "libtoolkit"
            || projectKey == "appmanager"
            || projectKey == "core"
            || projectKey == "libcompose"
            || projectKey == "libgeditors"
            || projectKey == "libgdocs"
            || projectKey == "libgcells"
        ) {
            return@forEach
        }

        val assets = File("${project.projectDir.path}/src/main/assets")
        println("Delete path: ${assets.path}")
        if (assets.exists()) {
            assets.deleteRecursively()
        }
    }
}

tasks.register("buildAar") {
    childProjects.forEach { (projectKey, project) ->
        if (projectKey == "libtoolkit" || projectKey == "appmanager" || projectKey == "core") {
            return@forEach
        }

        dependsOn("$projectKey::assembleRelease")

        doLast {
            val lib = File("${project.projectDir.path}/build/outputs/aar/${projectKey}-release.aar")
            copy {
                from(lib)
                into("${rootDir.parent}/libs")
            }
        }
    }
}
