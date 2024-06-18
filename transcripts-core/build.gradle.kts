import java.lang.System.getenv
import java.util.*

plugins {
    id("java-library")
    `maven-publish`
    signing
    kotlin("jvm") version("2.0.0")
}

group = "net.dungeon-hub"
version = "1.0-SNAPSHOT"
description = "The core project to bring Discord Transcripts to the JVM."

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api(libs.org.jsoup.jsoup)

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

val isReleaseVersion = !version.toString().endsWith("SNAPSHOT")

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])

        groupId = project.group.toString()
        artifactId = "transcripts-core"
        version = project.version.toString()

        pom {
            groupId = project.group.toString()
            name = artifactId
            description = project.description
            url = "https://github.com/dungeon-hub/discord-html-transcripts"

            //TODO license?

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
                    organizationUrl = "https://dungeon-hub.net/"
                }
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

signing {
    val secretKey = getenv("SIGNING_KEY")?.let { String(Base64.getDecoder().decode(it)) }
    val password = getenv("SIGNING_PASSWORD")
    useInMemoryPgpKeys(secretKey, password)
    sign(publishing.publications)
}

tasks.withType(Sign::class) {
    onlyIf { isReleaseVersion }
}