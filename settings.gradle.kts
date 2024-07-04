pluginManagement {
    val kotlinVersion: String by settings
    val gradleDependencyManagementVersion: String by settings
    val detektVersion: String by settings
    val jGitVerVersion: String by settings
    val nexusPublishVersion: String by settings
    val dokkaVersion: String by settings
    val palantirGitVersion: String by settings
    val gitPropertiesVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion apply false
        `maven-publish`
        id("io.spring.dependency-management") version gradleDependencyManagementVersion apply false
        id("io.gitlab.arturbosch.detekt") version detektVersion apply false
        id("fr.brouillard.oss.gradle.jgitver") version jGitVerVersion
        id("com.palantir.git-version") version palantirGitVersion apply false
        id("com.gorylenko.gradle-git-properties") version gitPropertiesVersion apply false
        id("jacoco")
        id("org.jetbrains.dokka") version dokkaVersion
        id("io.github.gradle-nexus.publish-plugin") version nexusPublishVersion
    }

    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
    }
}

rootProject.name = "wrc-common"

include(
    "wrc-common-bom",
    "wrc20:wrc20-contract-api",
    "wrc20:wrc20-contract-app",
    "wrc10:wrc10-contract-api",
    "wrc10:wrc10-contract-app",
    "wrc13:wrc13-contract-api",
    "wrc13:wrc13-contract-app"
)
