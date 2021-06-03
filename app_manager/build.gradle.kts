buildscript {

    extra.apply {
        set("NDK_VERSION", AppDependency.NDK_VERSION)
        set("kotlin_version", Kotlin.version)
    }

    repositories {
        google()
        maven { url = uri("https://jitpack.io") }
        mavenCentral()
        maven { url = uri("https://maven.google.com") }
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Kotlin.version}")
        classpath("com.google.gms:google-services:4.3.8")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.6.1")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${Kotlin.version}")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
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
