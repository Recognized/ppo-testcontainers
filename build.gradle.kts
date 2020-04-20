buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.3.72"))
    }
}

plugins {
    kotlin("jvm") version Versions.kotlin
    kotlin("plugin.serialization") version Versions.kotlin
    application
    id("com.github.johnrengelman.shadow") version "4.0.3"
}

repositories {
    jcenter()
    mavenCentral()
    google()
}

subprojects {
    repositories {
        jcenter()
        mavenCentral()
        google()
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
        implementation("io.ktor:ktor-client-okhttp:${Versions.ktor}")
        implementation("io.ktor:ktor-jackson:${Versions.ktor}")
        implementation("io.ktor:ktor-client-jackson:${Versions.ktor}")
        testImplementation(kotlin("test"))
        testImplementation(kotlin("test-junit"))
        testImplementation("org.testcontainers:testcontainers:1.14.0")
    }
}


project(":stonks") {
    application.mainClassName = "stonks.MainKt"
    val shadowJar: com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar by tasks
    shadowJar.manifest.attributes["Main-Class"] = "stonks.MainKt"
}

project(":cabinet") {
    tasks.getByName("test").dependsOn(":stonks:shadowJar")
    val test: Test by tasks
    test.apply {
        environment("projectPath", projectDir)
    }
}