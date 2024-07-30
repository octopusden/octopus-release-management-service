pluginManagement {
    plugins {
        id("org.springframework.boot") version (extra["spring-boot.version"] as String)
        id("io.spring.dependency-management") version "1.1.4"
        val kotlinVersion = extra["kotlin.version"] as String
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        id("com.avast.gradle.docker-compose") version (extra["docker-compose-plugin.version"] as String)
        id("com.bmuschko.docker-spring-boot-application") version (extra["bmuschko-docker-plugin.version"] as String)
        id("io.github.gradle-nexus.publish-plugin") version "1.1.0" apply false
        id("io.github.rodm.teamcity-server") version (extra["rodm-teamcity-plugin.version"] as String)
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "octopus-release-management-service"

include(":common")
include(":client")
include(":teamcity-plugin")
findProject(":teamcity-plugin")?.name = "release-management-teamcity-plugin"
include(":test-common")
include(":server")
findProject(":server")?.name = "release-management-service"
include(":ft")
