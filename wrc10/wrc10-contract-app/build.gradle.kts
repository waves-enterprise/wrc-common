val vstCommonsVersion: String by project
val mavenUser: String by project
val mavenPassword: String by project
val registry: String by rootProject.extra

plugins {
    `maven-publish`
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
