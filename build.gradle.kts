import io.gitlab.arturbosch.detekt.Detekt
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val jacocoToolVersion: String by project
val detektVersion: String by project

val junitVersion: String by project
val mockkVersion: String by project

val weSdkContractVersion: String by project
val weNodeClientVersion: String by project
val jacksonVersion: String by project

val sonaTypeBasePath: String by project
val gitHubProject: String by project
val githubUrl: String by project
val weMavenUser: String? by project
val weMavenPassword: String? by project
val sonaTypeMavenUser: String? by project
val sonaTypeMavenPassword: String? by project
val weMavenBasePath: String by project

plugins {
    kotlin("jvm") apply false
    `maven-publish`
    signing
    id("io.codearte.nexus-staging")
    id("io.spring.dependency-management") apply false
    id("io.gitlab.arturbosch.detekt")
    id("com.palantir.git-version") apply false
    id("com.gorylenko.gradle-git-properties") apply false
    id("fr.brouillard.oss.gradle.jgitver")
    id("org.jetbrains.dokka")
    id("jacoco")
}

nexusStaging {
    serverUrl = "$sonaTypeBasePath/service/local/"
    username = sonaTypeMavenUser
    password = sonaTypeMavenPassword
}

jgitver {
    strategy = fr.brouillard.oss.jgitver.Strategies.PATTERN
    versionPattern =
        "\${M}.\${m}.\${meta.COMMIT_DISTANCE}-\${meta.GIT_SHA1_8}\${-~meta.QUALIFIED_BRANCH_NAME}-SNAPSHOT"
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
                username = weMavenUser
                password = weMavenPassword
            }
        }

        maven {
            name = "maven-releases"
            url = uri("https://artifacts.wavesenterprise.com/repository/maven-releases/")
            mavenContent {
                releasesOnly()
            }
            credentials {
                username = weMavenUser
                password = weMavenPassword
            }
        }
    }
}

subprojects {
    apply(plugin = "maven-publish")

    publishing {
        repositories {
            if (weMavenUser != null && weMavenPassword != null) {
                maven {
                    name = "WE-artifacts"
                    afterEvaluate {
                        url = uri(
                            "$weMavenBasePath${
                                if (project.version.toString()
                                        .endsWith("-SNAPSHOT")
                                ) "maven-snapshots" else "maven-releases"
                            }"
                        )
                    }
                    credentials {
                        username = weMavenUser
                        password = weMavenPassword
                    }
                }
            }

            if (sonaTypeMavenPassword != null && sonaTypeMavenUser != null) {
                maven {
                    name = "SonaType-maven-central-staging"
                    val releasesUrl = uri("$sonaTypeBasePath/service/local/staging/deploy/maven2/")
                    afterEvaluate {
                        url = if (version.toString()
                                .endsWith("SNAPSHOT")
                        ) throw kotlin.Exception("shouldn't publish snapshot") else releasesUrl
                    }
                    credentials {
                        username = sonaTypeMavenUser
                        password = sonaTypeMavenPassword
                    }
                }
            }
        }
    }
}

configure(
    subprojects.filter { it.name != "wrc-common-bom" }
) {
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "kotlin")
    apply(plugin = "signing")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "jacoco")
    apply(plugin = "org.jetbrains.dokka")

    dependencies {
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
    }

    val jacocoCoverageFile = layout.buildDirectory.file("jacocoReports/test/jacocoTestReport.xml").get().asFile
    tasks.withType<JacocoReport> {
        reports {
            xml.apply {
                required.set(true)
                outputLocation.set(jacocoCoverageFile)
            }
        }
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

    tasks.register<Detekt>("detektFormat") {
        description = "Runs detekt with auto-correct to format the code."
        group = "formatting"
        autoCorrect = true
        exclude("resources/")
        exclude("build/")
        config.setFrom(detektConfigFilePath)
        setSource(files("src/main/java", "src/main/kotlin"))
    }

    val sourcesJar by tasks.creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles sources JAR"
        archiveClassifier.set("sources")
        from(project.the<SourceSetContainer>()["main"].allSource)
    }

    val dokkaJavadoc by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)
    val javadocJar by tasks.creating(Jar::class) {
        dependsOn(dokkaJavadoc)
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles javadoc JAR"
        archiveClassifier.set("javadoc")
        from(dokkaJavadoc.outputDirectory)
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                versionMapping {
                    allVariants {
                        fromResolutionResult()
                    }
                }
                afterEvaluate {
                    artifact(sourcesJar)
                    artifact(javadocJar)
                }
                pom {
                    packaging = "jar"
                    name.set(project.name)
                    url.set(githubUrl + gitHubProject)
                    description.set("WE Node Client for Java/Kotlin")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    scm {
                        connection.set("scm:$githubUrl$gitHubProject")
                        developerConnection.set("scm:git@github.com:$gitHubProject.git")
                        url.set(githubUrl + gitHubProject)
                    }

                    developers {
                        developer {
                            id.set("kt3")
                            name.set("Stepan Kashintsev")
                            email.set("kpote3@gmail.com")
                            url.set("https://github.com/kt3")
                        }
                        developer {
                            id.set("donyfutura")
                            name.set("Daniil Georgiev")
                            email.set("donyfutura@gmail.com")
                            url.set("https://github.com/donyfutura")
                        }
                        developer {
                            id.set("danilagridnev")
                            name.set("Danila Gridnev")
                            email.set("danilagridnev@gmail.com")
                            url.set("https://github.com/danilagridnev")
                        }
                    }
                }
            }
        }
    }

    signing {
        afterEvaluate {
            if (!project.version.toString().endsWith("SNAPSHOT")) {
                sign(publishing.publications["mavenJava"])
            }
        }
    }

    the<DependencyManagementExtension>().apply {
        imports {
            mavenBom("com.fasterxml.jackson:jackson-bom:$jacksonVersion")
            mavenBom("com.wavesenterprise:we-contract-sdk-bom:$weSdkContractVersion")
            mavenBom("com.wavesenterprise:we-node-client-bom:$weNodeClientVersion")
            mavenBom("org.junit:junit-bom:$junitVersion")
        }
        dependencies {
            dependency("io.mockk:mockk:$mockkVersion")
        }
    }

    jacoco {
        toolVersion = jacocoToolVersion
        reportsDirectory.set(layout.buildDirectory.dir("jacocoReports").get().asFile)
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all-compatibility")
            jvmTarget = JavaVersion.VERSION_17.toString()
        }
    }
}
