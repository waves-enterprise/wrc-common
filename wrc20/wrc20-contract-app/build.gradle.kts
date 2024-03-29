import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    application
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation(project(":wrc10:wrc10-contract-app"))
    implementation(project(":wrc20:wrc20-contract-api"))

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testImplementation("com.wavesenterprise:we-node-domain-test")
    testImplementation("com.wavesenterprise:we-contract-sdk-test")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.withType<ShadowJar> {
    manifest {
        attributes["Main-Class"] = "com.wavesenterprise.wrc.wrc20.WRC20FTokenContractStarterKt"
    }
}

project.setProperty("mainClassName", "com.wavesenterprise.wrc.wrc20.WRC20FTokenContractStarterKt")
