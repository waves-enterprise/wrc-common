val mavenUser: String by project
val mavenPassword: String by project
val vstCommonsVersion: String by project

plugins {
    `maven-publish`
}

dependencies {
    api("com.wavesenterprise:we-contract-sdk-api")
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
