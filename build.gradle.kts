buildscript {
    repositories {
        jcenter()
    }
}

repositories {
    jcenter()
    mavenCentral()
    google()

//    maven("https://dl.bintray.com/kotlin/ktor")
//    maven("https://dl.bintray.com/kotlin/kotlinx")
}

plugins {
    kotlin("jvm") version Versions.kotlin
    kotlin("plugin.serialization") version Versions.kotlin
}


tasks.compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0") // JVM dependency
    implementation("io.ktor:ktor-server-core:${Versions.ktor}")
    implementation("io.ktor:ktor-server-netty:${Versions.ktor}")
    implementation("log4j:log4j:1.2.17")
    implementation("org.slf4j:slf4j-simple:1.7.29")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))

    testImplementation("io.ktor:ktor-client-core:${Versions.ktor}")
    testImplementation("io.ktor:ktor-client-cio:${Versions.ktor}")
}