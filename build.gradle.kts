buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    kotlin("jvm") version Versions.kotlin
    kotlin("plugin.serialization") version Versions.kotlin
    application
    id("com.github.johnrengelman.shadow") version "2.0.1"
}

subprojects {
    repositories {
        jcenter()
        mavenCentral()
        google()

//    maven("https://dl.bintray.com/kotlin/ktor")
//    maven("https://dl.bintray.com/kotlin/kotlinx")
    }

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "org.gradle.application")
    apply(plugin = "com.github.johnrengelman.shadow")

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

        implementation("io.ktor:ktor-client-core:${Versions.ktor}")
        implementation("io.ktor:ktor-client-cio:${Versions.ktor}")
        implementation("io.ktor:ktor-jackson:${Versions.ktor}")
        implementation("io.ktor:ktor-client-jackson:${Versions.ktor}")
        testImplementation(kotlin("test"))
        testImplementation(kotlin("test-junit"))
        testImplementation("org.testcontainers:testcontainers:1.14.0")
    }
}


project(":stonks") {
    val jar: org.gradle.jvm.tasks.Jar by tasks
    jar.manifest {
        attributes["Main-Class"] = "stonks.MainKt"
    }

    jar.from(configurations.runtimeClasspath.get().map { if (it.isDirectory) zipTree(it) else it })
}

project(":cabinet") {
    tasks.getByName("test").dependsOn(":stonks:jar")
}