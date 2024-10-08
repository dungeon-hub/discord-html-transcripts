/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    `maven-publish`
}

repositories {
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
    maven {
        url = uri("https://repo.kordex.dev/releases")
        name = "KordEx (Releases)"
    }
    maven {
        url = uri("https://repo.kordex.dev/snapshots")
        name = "KordEx (Snapshots)"
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        name = "Sonatype Snapshots (Legacy)"
    }
    mavenLocal()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}
