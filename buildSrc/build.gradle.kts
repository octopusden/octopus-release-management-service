plugins {
    kotlin("jvm") version "1.9.23"
    groovy
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("org.mock-server:mockserver-client-java:5.15.0")
}
