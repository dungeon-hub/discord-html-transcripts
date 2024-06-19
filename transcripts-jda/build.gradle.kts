plugins {
    id("java-library")
    `maven-publish`
    kotlin("jvm") version("2.0.0")
}

group = "net.dungeon-hub"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api(project(":transcripts-core"))
    api(libs.net.dv8tion.jda)

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
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