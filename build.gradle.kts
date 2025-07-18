import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.octopusden.task.MigrateMockData

plugins {
    java
    idea
    id("io.spring.dependency-management")
    id("org.jetbrains.kotlin.jvm")
    id("io.github.gradle-nexus.publish-plugin")
    signing
}

allprojects {
    group = "org.octopusden.octopus.release-management-service"
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(System.getenv("MAVEN_USERNAME"))
            password.set(System.getenv("MAVEN_PASSWORD"))
        }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "idea")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "signing")

    repositories {
        mavenCentral()
        maven { setUrl("https://maven.artifacts.atlassian.com/") }
    }

    idea.module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }

    java {
        withJavadocJar()
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            suppressWarnings = true
            jvmTarget = "21"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        testLogging{
            info.events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        }

        systemProperties["release-management-service.version"] = version
    }

    val migrateMockData by tasks.creating(MigrateMockData::class) {
        this.testDataDir = rootDir.toString() + File.separator + "test-data"
    }

    ext {
        System.getenv().let {
            set("signingRequired", it.containsKey("ORG_GRADLE_PROJECT_signingKey") && it.containsKey("ORG_GRADLE_PROJECT_signingPassword"))
            set("dockerRegistry", it.getOrDefault("DOCKER_REGISTRY", properties["docker.registry"]))
            set("octopusGithubDockerRegistry", it.getOrDefault("OCTOPUS_GITHUB_DOCKER_REGISTRY", properties["octopus.github.docker.registry"]))
        }
        set("validateFun", { properties: List<String> ->
            val emptyProperties = properties.filter { (project.ext[it] as? String).isNullOrBlank() }
            if (emptyProperties.isNotEmpty()) {
                throw IllegalArgumentException(
                    "Start gradle build with" +
                            (if (emptyProperties.contains("dockerRegistry")) " -Pdocker.registry=..." else "") +
                            (if (emptyProperties.contains("octopusGithubDockerRegistry")) " -Poctopus.github.docker.registry=..." else "") +
                            " or set env variable(s):" +
                            (if (emptyProperties.contains("dockerRegistry")) " DOCKER_REGISTRY" else "") +
                            (if (emptyProperties.contains("octopusGithubDockerRegistry")) " OCTOPUS_GITHUB_DOCKER_REGISTRY" else "")
                )
            }
        })
    }
}
