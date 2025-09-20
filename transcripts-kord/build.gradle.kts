import net.thebugmc.gradle.sonatypepublisher.PublishingType

plugins {
    id("java-library")
    id("net.thebugmc.gradle.sonatype-central-portal-publisher").version("1.2.3")
    kotlin("jvm") version("2.1.21")
}

group = "net.dungeon-hub"
val artifactId = "transcripts-kord"
version = "0.2.0"
description = "The Kord implementation for discord transcripts."

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://mirror-repo.kordex.dev")
}

dependencies {
    api(project(":transcripts-core"))
    compileOnly(libs.dev.kord.kord.core.jvm)

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

centralPortal {
    name = artifactId
    publishingType = PublishingType.USER_MANAGED

    pom {
        name = artifactId
        description = project.description
        url = "https://github.com/dungeon-hub/discord-html-transcripts"

        organization {
            name = "Dungeon Hub"
            url = "https://dungeon-hub.net/"
        }

        scm {
            url = "https://github.com/dungeon-hub/discord-html-transcripts"
            connection = "scm:git://github.com:dungeon-hub/discord-html-transcripts.git"
            developerConnection = "scm:git://github.com:dungeon-hub/discord-html-transcripts.git"
        }

        developers {
            developer {
                id = "taubsie"
                name = "Taubsie"
                email = "taubsie@dungeon-hub.net"
                url = "https://github.com/Taubsie/"
                organizationUrl = "https://dungeon-hub.net/"
            }
        }

        licenses {
            license {
                name = "GPL-3.0"
                url = "https://www.gnu.org/licenses/gpl-3.0"
                distribution = "https://www.gnu.org/licenses/gpl-3.0.txt"
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}