import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    application
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    api(project(":wrc10:wrc10-contract-api"))

    api("com.wavesenterprise:we-contract-sdk-grpc")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testImplementation("com.wavesenterprise:we-node-domain-test")
    testImplementation("com.wavesenterprise:we-contract-sdk-test")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType<ShadowJar> {
    manifest {
        attributes["Main-Class"] = "com.wavesenterprise.sdk.wrc.wrc10.WRC10RoleBasedAccessControlStarterKt"
    }
}

project.setProperty("mainClassName", "com.wavesenterprise.sdk.wrc.wrc10.WRC10RoleBasedAccessControlStarterKt")
