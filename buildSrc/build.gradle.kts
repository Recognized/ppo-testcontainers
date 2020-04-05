plugins {
    `kotlin-dsl`
}

repositories {
    jcenter()
    mavenCentral()
    google()
    maven { url = uri("https://plugins.gradle.org/m2") }
    maven { url = uri("https://dl.bintray.com/kotlin/kotlin-dev") }
    maven { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
}