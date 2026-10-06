plugins {
    // The Kotlin that Gradle itself embeds: this code compiles against, and runs inside, Gradle's
    // own API and stdlib, which an older compiler cannot read.
    kotlin("jvm") version embeddedKotlinVersion
    groovy
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("org.mock-server:mockserver-client-java:5.15.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

tasks.test {
    useJUnitPlatform()
}
