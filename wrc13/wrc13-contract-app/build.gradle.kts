import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val mavenUser: String by project
val mavenPassword: String by project
val registry: String by rootProject.extra

plugins {
    kotlin("jvm")
    application
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation(project(":wrc13:wrc13-contract-api"))
    implementation(project(":wrc10:wrc10-contract-app"))

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testImplementation("com.wavesenterprise:we-node-domain-test")
    testImplementation("com.wavesenterprise:we-contract-sdk-test")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType<ShadowJar> {
    manifest {
        attributes["Main-Class"] = "com.wavesenterprise.sdk.wrc.wrc13.WRC13RegistryContractStarterKt"
    }
}

project.setProperty("mainClassName", "com.wavesenterprise.sdk.wrc.wrc13.WRC13RegistryContractStarterKt")
