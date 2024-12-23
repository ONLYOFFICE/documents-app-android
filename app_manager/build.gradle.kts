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

plugins {
    alias(libs.plugins.kotlin.serialization.plugin) apply false
    id("maven-publish")
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

tasks.register("clearAssets") {
    childProjects.forEach { (projectKey, project) ->
        if (projectKey == "libtoolkit"
            || projectKey == "appmanager"
            || projectKey == "core"
            || projectKey == "libcompose"
            || projectKey == "libgeditors") {
            return@forEach
        }

        val assets = File("${project.projectDir.path}/src/main/assets")
        println("Delete path: ${assets.path}")
        if (assets.exists()) {
            assets.deleteRecursively()
        }
    }
}

tasks.create("buildAar") {
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

//tasks.create("publishToGithub") {
//    childProjects.forEach { (projectKey, _) ->
//        if (projectKey == "libtoolkit" || projectKey == "appmanager" || projectKey == "core") {
//            return@forEach
//        }
//        dependsOn("$projectKey::assembleRelease")
//        dependsOn("$projectKey::publish")
//    }
//}
