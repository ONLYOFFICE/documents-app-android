plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("kotlinx-serialization")
}

dependencies {
    implementation(libs.kotlin.serialization.json)
}