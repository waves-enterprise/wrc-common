import fr.brouillard.oss.jgitver.Strategies.PATTERN
import io.gitlab.arturbosch.detekt.Detekt
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mavenUser: String by project
val mavenPassword: String by project

val kotlinVersion: String by project
val springBootVersion: String by project
val jacocoToolVersion: String by project
val detektVersion: String by project

val registry: String by project.extra { System.getenv("CI_REGISTRY") ?: "registry.web3tech.ru" }

val weSdkBomVersion: String by project

plugins {
    kotlin("jvm") apply false
    kotlin("kapt") apply false
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") apply false
    id("org.jlleitschuh.gradle.ktlint") apply false
    id("com.wavesplatform.vst.contract-docker") apply false
    id("fr.brouillard.oss.gradle.jgitver")
    id("io.gitlab.arturbosch.detekt")
    id("jacoco")
    `maven-publish`
}

jgitver {
    strategy = PATTERN
    versionPattern = "\${M}.\${m}.\${meta.COMMIT_DISTANCE}-\${meta.GIT_SHA1_8}\${-~meta.QUALIFIED_BRANCH_NAME}-SNAPSHOT"
    nonQualifierBranches = "master,dev,main"
}

allprojects {
    group = "com.wavesenterprise"
    version = "-" // set by jgitver

    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://repo.spring.io/libs-release") }
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

subprojects {
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "kotlin")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "jacoco")

    val jacocoCoverageFile = "$buildDir/jacocoReports/test/jacocoTestReport.xml"

    tasks.withType<JacocoReport> {
        reports {
            xml.apply {
                required.set(true)
                outputLocation.set(file(jacocoCoverageFile))
            }
        }
    }

    jacoco {
        toolVersion = jacocoToolVersion
        reportsDirectory.set(file("$buildDir/jacocoReports"))
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events = setOf(
                TestLogEvent.FAILED,
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED
            )
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
        finalizedBy("jacocoTestReport")
    }

    val detektConfigFilePath = "$rootDir/gradle/detekt-config.yml"

    tasks.withType<Detekt> {
        exclude("resources/")
        exclude("build/")
        config.setFrom(detektConfigFilePath)
        buildUponDefaultConfig = true
    }

    dependencies {
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
    }

    the<DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion") {
                bomProperty("kotlin.version", kotlinVersion)
            }
            mavenBom("com.wavesenterprise:we-sdk-bom:$weSdkBomVersion") {
                bomProperty("kotlin.version", kotlinVersion)
            }
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all-compatibility")
            jvmTarget = JavaVersion.VERSION_17.toString()
        }
    }
}
