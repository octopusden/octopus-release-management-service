import org.octopusden.task.MigrateMockData
import com.avast.gradle.dockercompose.ComposeExtension

plugins {
    id("org.springframework.boot")
    id("org.jetbrains.kotlin.plugin.spring")
    id("com.avast.gradle.docker-compose")
    id("com.bmuschko.docker-spring-boot-application")
    id("org.octopusden.octopus.oc-template")
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("bootJar") {
            artifact(tasks.getByName("bootJar"))
            from(components["java"])
            pom {
                name.set(project.name)
                description.set("Octopus module: ${project.name}")
                url.set("https://github.com/octopusden/octopus-release-management-service.git")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    url.set("https://github.com/kzaporozhtsev/octopus-release-management-service.git")
                    connection.set("scm:git://github.com/octopusden/octopus-release-management-service.git")
                }
                developers {
                    developer {
                        id.set("octopus")
                        name.set("octopus")
                    }
                }
            }
        }
    }
}

signing {
    isRequired = project.ext["signingRequired"] as Boolean
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["bootJar"])
}

fun String.getExt() = project.ext[this] as String

configure<ComposeExtension> {
    useComposeFiles.add(layout.projectDirectory.file("docker/docker-compose.yml").asFile.path )
    waitForTcpPorts.set(true)
    captureContainersOutputToFiles.set(layout.buildDirectory.dir("docker-logs"))
    environment.putAll(
        mapOf(
            "MOCKSERVER_VERSION" to properties["mockserver.version"],
            "DOCKER_REGISTRY" to "dockerRegistry".getExt()
        )
    )
}

ocTemplate {
    workDir.set(layout.buildDirectory.dir("okd"))
    clusterDomain.set("okdClusterDomain".getExt())
    namespace.set("okdProject".getExt())
    prefix.set("rm-service-ut")

    "okdWebConsoleUrl".getExt().takeIf { it.isNotBlank() }?.let{
        webConsoleUrl.set(it)
    }

    service("mockserver") {
        templateFile.set(rootProject.layout.projectDirectory.file("okd/mockserver.yaml"))
        parameters.set(mapOf(
            "DOCKER_REGISTRY" to "dockerRegistry".getExt(),
            "ACTIVE_DEADLINE_SECONDS" to "okdActiveDeadlineSeconds".getExt(),
            "MOCK_SERVER_VERSION" to properties["mockserver.version"] as String
        ))
    }
}

tasks {
    val migrateMockData by registering(MigrateMockData::class)
}

when ("testPlatform".getExt()) {
    "okd" -> {
        tasks.named<MigrateMockData>("migrateMockData") {
            testDataDir.set(rootDir.toString() + File.separator + "test-data")
            host.set(ocTemplate.getOkdHost("mockserver"))
            port.set(80)
            dependsOn("ocCreate")
        }
        tasks.withType<Test> {
            systemProperties["test.mockserver-host"] = ocTemplate.getOkdHost("mockserver")
            dependsOn("migrateMockData")
            finalizedBy("ocLogs", "ocDelete")
        }
    }
    "docker" -> {
        tasks.named<MigrateMockData>("migrateMockData") {
            testDataDir.set(rootDir.toString() + File.separator + "test-data")
            host.set("localhost")
            port.set(1080)
            dependsOn("composeUp")
        }
        tasks.withType<Test> {
            systemProperties["test.mockserver-host"] = "localhost:1080"
            dependsOn("migrateMockData")
            finalizedBy("composeLogs", "composeDown")
        }
    }
}

docker {
    springBootApplication {
        baseImage.set("${"dockerRegistry".getExt()}/eclipse-temurin:21-jdk")
        ports.set(listOf(8080, 8080))
        images.set(setOf("${"octopusGithubDockerRegistry".getExt()}/octopusden/$name:$version"))
    }
}

springBoot {
    buildInfo()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${properties["spring-boot.version"]}")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${properties["spring-cloud.version"]}")
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":legacy-releng-client"))
    implementation("org.octopusden.octopus.infrastructure:components-registry-service-client:${properties["octopus-components-registry.version"]}")
    implementation("com.atlassian.jira:jira-rest-java-client-core:${properties["jira-rest-client.version"]}")
    implementation("com.atlassian.jira:jira-rest-java-client-api:${properties["jira-rest-client.version"]}")
    implementation("io.atlassian.fugue:fugue:${properties["fugue.version"]}")
    implementation("org.glassfish.jersey.core:jersey-common:${properties["glassfish-jersey.version"]}")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.cloud:spring-cloud-starter")
    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap")
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${properties["springdoc-openapi.version"]}")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${properties["mockito-kotlin.version"]}")
    testImplementation(project(":test-common"))
}

configurations.all {
    exclude("commons-logging", "commons-logging")
}