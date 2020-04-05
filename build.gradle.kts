buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    kotlin("jvm") version Versions.kotlinVersion

//    id("kotlinx-serialization") version Versions.kotlinVersion
}

repositories {
    jcenter()
    mavenCentral()
    google()

//    maven("https://dl.bintray.com/kotlin/ktor")
//    maven("https://dl.bintray.com/kotlin/kotlinx")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}