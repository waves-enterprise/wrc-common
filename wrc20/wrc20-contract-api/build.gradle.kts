val mavenUser: String by project
val mavenPassword: String by project
val vstCommonsVersion: String by project

plugins {
    `maven-publish`
}

dependencies {
    api("com.wavesplatform.we:vst-contract-common:$vstCommonsVersion")
    api(project(":wrc10:wrc10-contract-api"))
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
