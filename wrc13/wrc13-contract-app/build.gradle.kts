import org.springframework.boot.gradle.tasks.bundling.BootJar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val mavenUser: String by project
val mavenPassword: String by project
val registry: String by rootProject.extra

plugins {
    id("org.springframework.boot")
    id("com.wavesplatform.vst.contract-docker")
    `maven-publish`
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

fun getDate(): String {
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
    return formatter.format(current)
}

val bootJar: BootJar by tasks
bootJar.apply {
    archiveClassifier.set("application")
}

val jar: Jar by tasks
jar.apply {
    enabled = true
}

tasks {
    docker {
        val imageVersion = if (version.toString().endsWith("-SNAPSHOT")) {
            "$version-${getDate()}"
        } else {
            "$version"
        }
        name = "$registry/icore-sc/${project.name}:$imageVersion"
        tags("latest")
        files(bootJar.get().archiveFile)
        noCache(true)
        dependsOn(bootJar.get())
    }
}

java {
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = "maven"
            url = uri(
                "https://artifacts.wavesenterprise.com/repository/" +
                    if (project.version.toString().endsWith("-SNAPSHOT")) "maven-snapshots"
                    else "maven-releases"
            )
            credentials {
                username = mavenUser
                password = mavenPassword
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
