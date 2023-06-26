buildscript {

    extra.apply {
        set("NDK_VERSION", AppDependency.NDK_VERSION)
        set("kotlin_version", Kotlin.version)
    }

    repositories {
        google()
        maven { setUrl("https://mvnrepository.com/artifact/com.github.gundy/semver4j") }
        mavenCentral()
        maven { setUrl("https://maven.google.com") }
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Kotlin.version}")
        classpath("com.google.gms:google-services:4.3.14")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.2")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${Kotlin.version}")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { setUrl("https://jitpack.io") }
        maven {
            url = uri(PublishEditors.publishUrl)
        }
    }

    configurations.configureEach {
        resolutionStrategy {
            force("org.xerial:sqlite-jdbc:3.34.0")
        }
    }
}

plugins {
    id("maven-publish")
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
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

tasks.create("publishToGithub") {
    childProjects.forEach { (projectKey, project) ->
        if (projectKey == "libtoolkit" || projectKey == "appmanager" || projectKey == "core") {
            return@forEach
        }
        dependsOn("$projectKey::assembleRelease")
        dependsOn("$projectKey::publish")
    }
}
