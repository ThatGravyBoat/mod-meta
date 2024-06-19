plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java")
}

group = "tech.thatgravyboat"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    shadow("org.jsoup:jsoup:1.17.2")
    shadow("com.google.code.gson:gson:2.11.0")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("com.google.code.gson:gson:2.11.0")
}