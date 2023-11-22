pluginManagement {
    val mavenUser: String by settings
    val mavenPassword: String by settings

    val kotlinVersion: String by settings
    val springBootVersion: String by settings
    val gradleDependencyManagementVersion: String by settings
    val detektVersion: String by settings
    val ktlintVersion: String by settings
    val jGitVerVersion: String by settings
    val vstContractDockerPluginVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion apply false
        kotlin("kapt") version kotlinVersion apply false
        `maven-publish`
        id("org.springframework.boot") version springBootVersion apply false
        id("io.spring.dependency-management") version gradleDependencyManagementVersion apply false
        id("io.gitlab.arturbosch.detekt") version detektVersion apply false
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion apply false
        id("com.wavesplatform.vst.contract-docker") version vstContractDockerPluginVersion apply false
        id("fr.brouillard.oss.gradle.jgitver") version jGitVerVersion
        id("jacoco")
    }

    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
        maven {
            name = "maven-snapshots"
            url = uri("https://artifacts.wavesenterprise.com/repository/maven-snapshots/")
            mavenContent {
                snapshotsOnly()
            }
            credentials {
                username = mavenUser
                password = mavenPassword
            }
        }
        maven {
            name = "maven-releases"
            url = uri("https://artifacts.wavesenterprise.com/repository/maven-releases/")
            mavenContent {
                releasesOnly()
            }
            credentials {
                username = mavenUser
                password = mavenPassword
            }
        }
    }
}

rootProject.name = "wrc-common"

include(
    "wrc20:wrc20-contract-api",
    "wrc20:wrc20-contract-app",
    "wrc10:wrc10-contract-api",
    "wrc10:wrc10-contract-app",
    "wrc13:wrc13-contract-api",
    "wrc13:wrc13-contract-app"
)