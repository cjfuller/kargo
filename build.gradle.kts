repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    application
}

val ktor_version = "1.6.7"

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-apache:$ktor_version")
    implementation("com.uchuhimo:konf:1.1.2")
    implementation("com.uchuhimo:konf-toml:1.1.2")
}


application {
    mainClass.set("kargo.MainKt")
}
