import com.avast.gradle.dockercompose.ComposeExtension
import org.gradle.kotlin.dsl.provideDelegate
import org.octopusden.task.MigrateMockData

plugins {
    id("com.avast.gradle.docker-compose")
    id("org.octopusden.octopus.oc-template")
}

sourceSets {
    create("ft") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val ftImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

ftImplementation.isCanBeResolved = true

configurations["ftRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

tasks {
    val migrateMockData by registering(MigrateMockData::class)
}

configure<ComposeExtension> {
    useComposeFiles.add(layout.projectDirectory.file("docker/docker-compose.yml").asFile.path)
    waitForTcpPorts.set(true)
    captureContainersOutputToFiles.set(layout.buildDirectory.dir("docker-logs"))
    environment.putAll(
        mapOf(
            "RELEASE_MANAGEMENT_SERVICE_VERSION" to version,
            "OCTOPUS_COMPONENTS_REGISTRY_SERVICE_VERSION" to properties["octopus-components-registry.version"],
            "MOCKSERVER_VERSION" to properties["mockserver.version"],
            "TEAMCITY_2022_IMAGE_TAG" to properties["teamcity-2022.image-tag"],
            "DOCKER_REGISTRY" to "dockerRegistry".getExt(),
            "OCTOPUS_GITHUB_DOCKER_REGISTRY" to "octopusGithubDockerRegistry".getExt(),
            "TEST_MOCKSERVER_HOST" to "mockserver:1080",
            "TEST_COMPONENTS_REGISTRY_HOST" to "components-registry-service:4567"
        )
    )
}

tasks.register<Copy>("deployTeamcity2022Plugin") {
    dependsOn(prepareTeamcity2022Data)
    dependsOn(":release-management-teamcity-plugin:serverPlugin")
    from(rootProject.project("release-management-teamcity-plugin").configurations["distributions"].artifacts.files)
    into(layout.buildDirectory.dir("teamcity-server-2022/datadir/plugins"))
}

tasks.named("composeUp") {
    dependsOn(":release-management-service:dockerBuildImage")
    dependsOn("deployTeamcity2022Plugin")
}

val prepareTeamcity2022Data = tasks.register<Sync>("prepareTeamcity2022Data") {
    from(zipTree(layout.projectDirectory.file("docker/data.zip")))
    into(layout.buildDirectory.dir("teamcity-server-2022"))
}

fun String.getExt() = project.ext[this] as String
fun String.getPort() = when (this) {
    "teamcity22" -> 8111
    "comp-reg" -> 4567
    "mockserver" -> 1080
    "rm" -> 8080
    else -> throw Exception("Unknown service '$this'")
}
fun getOkdExternalHost(serviceName: String) = "${ocTemplate.getPod(serviceName)}-service:${serviceName.getPort()}"

val commonOkdParameters = mapOf(
    "ACTIVE_DEADLINE_SECONDS" to "okdActiveDeadlineSeconds".getExt(),
    "DOCKER_REGISTRY" to "dockerRegistry".getExt()
)

ocTemplate {
    workDir.set(layout.buildDirectory.dir("okd"))
    clusterDomain.set("okdClusterDomain".getExt())
    namespace.set("okdProject".getExt())
    prefix.set("rm-service-ft")

    "okdWebConsoleUrl".getExt().takeIf { it.isNotBlank() }?.let {
        webConsoleUrl.set(it)
    }

    group("teamcityPVC").apply {
        service("teamcity22-pvc") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/teamcity-pvc.yaml"))
            parameters.set(mapOf(
                "TEAMCITY_ID" to "22"
            ))
        }
    }

    group("teamcitySeedUploader").apply {
        service("teamcity22-uploader") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/teamcity-uploader.yaml"))
            parameters.set(mapOf(
                "SERVICE_ACCOUNT_ANYUID" to project.properties["okd.service-account-anyuid"] as String,
                "ACTIVE_DEADLINE_SECONDS" to "okdActiveDeadlineSeconds".getExt(),
                "TEAMCITY_ID" to "22"
            ))
        }
    }

    group("teamcityServer").apply {
        service("teamcity22") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/teamcity.yaml"))
            parameters.set(commonOkdParameters + mapOf(
                "SERVICE_ACCOUNT_ANYUID" to project.properties["okd.service-account-anyuid"] as String,
                "TEAMCITY_IMAGE_TAG" to properties["teamcity-2022.image-tag"] as String,
                "TEAMCITY_ID" to "22"
            ))
        }
    }

    group("teamcityAgent").apply {
        service("tc22-agent") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/teamcity-agent.yaml"))
            parameters.set(commonOkdParameters + mapOf(
                "SERVICE_ACCOUNT_ANYUID" to project.properties["okd.service-account-anyuid"] as String,
                "TEAMCITY_IMAGE_TAG" to properties["teamcity-2022.image-tag"] as String,
                "TEAMCITY_ID" to "22",
                "TEAMCITY_SERVER_HOST" to getOkdExternalHost("teamcity22").replace(":", "\\:"),
                "BUILD_AGENT_PROPERTIES_CONTENT" to layout.projectDirectory.dir("docker/buildAgent.properties").asFile.readText()
            ))
        }
    }

    group("componentsRegistry").apply {
        service("comp-reg") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/components-registry.yaml"))
            val componentsRegistryWorkDir = layout.projectDirectory.dir("../test-common/src/main/components-registry").asFile.absolutePath
            parameters.set(commonOkdParameters + mapOf(
                "COMPONENTS_REGISTRY_SERVICE_VERSION" to properties["octopus-components-registry.version"] as String,
                "AGGREGATOR_GROOVY_CONTENT" to file("${componentsRegistryWorkDir}/Aggregator.groovy").readText(),
                "DEFAULTS_GROOVY_CONTENT" to file("${componentsRegistryWorkDir}/Defaults.groovy").readText(),
                "TEST_COMPONENTS_GROOVY_CONTENT" to file("${componentsRegistryWorkDir}/TestComponents.groovy").readText(),
                "APPLICATION_DEV_CONTENT" to layout.projectDirectory.dir("docker/components-registry-service.yaml").asFile.readText()
            ))
        }
    }

    group("mockserver").apply {
        service("mockserver") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/mockserver.yaml"))
            parameters.set(commonOkdParameters + mapOf(
                "MOCK_SERVER_VERSION" to properties["mockserver.version"] as String
            ))
        }
    }

    group("releaseManagement").apply {
        service("rm") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/release-management.yaml"))
            parameters.set(commonOkdParameters + mapOf(
                "RELEASE_MANAGEMENT_SERVICE_VERSION" to version as String,
                "OCTOPUS_GITHUB_DOCKER_REGISTRY" to "octopusGithubDockerRegistry".getExt(),
                "APPLICATION_DEV_CONTENT" to layout.projectDirectory.dir("docker/release-management-service.yaml").asFile.readText(),
                "TEST_MOCKSERVER_HOST" to getOkdExternalHost("mockserver"),
                "TEST_COMPONENTS_REGISTRY_HOST" to getOkdExternalHost("comp-reg")
            ))
        }
    }
}

val copyTeamcity2022Plugin = tasks.register<Exec>("copyTeamcity2022Plugin") {
    dependsOn("ocCreateTeamcityPVC", "ocCreateTeamcitySeedUploader")
    dependsOn(":release-management-teamcity-plugin:serverPlugin")
    val pluginFile = rootProject.project("release-management-teamcity-plugin").configurations["distributions"].artifacts.files.asPath
    commandLine("oc", "cp", pluginFile, "-n", "okdProject".getExt(),
        "${ocTemplate.getPod("teamcity22-uploader")}:/data/teamcity_server/datadir/plugins")
}

val copyFilesTeamcity2022 = tasks.register<Exec>("copyFilesTeamcity2022") {
    dependsOn(copyTeamcity2022Plugin)
    val localFile = layout.projectDirectory.dir("docker/data.zip").asFile.absolutePath
    commandLine("oc", "cp", localFile, "-n", "okdProject".getExt(),
        "${ocTemplate.getPod("teamcity22-uploader")}:/seed/seed.zip")
}

val seedTeamcity = tasks.register("seedTeamcity") {
    dependsOn(copyFilesTeamcity2022)
    finalizedBy("ocLogsTeamcitySeedUploader", "ocDeleteTeamcitySeedUploader")
}

tasks.named("ocCreateTeamcityServer").configure {
    dependsOn(seedTeamcity)
}

tasks.named("ocDeleteTeamcityPVC").configure {
    dependsOn("ocDeleteTeamcityServer")
}

tasks.named("ocCreateReleaseManagement") {
    dependsOn(":release-management-service:dockerPushImage")
}

tasks.named<MigrateMockData>("migrateMockData") {
    testDataDir.set(rootDir.toString() + File.separator + "test-data")
    when ("testPlatform".getExt()) {
        "okd" -> {
            host.set(ocTemplate.getOkdHost("mockserver"))
            port.set(80)
            dependsOn("ocCreateMockserver")
        }
        "docker" -> {
            host.set("localhost")
            port.set(1080)
            dependsOn("composeUp")
        }
    }
}

val ft by tasks.creating(Test::class) {
    group = "verification"
    description = "Runs the integration tests"

    testClassesDirs = sourceSets["ft"].output.classesDirs
    classpath = sourceSets["ft"].runtimeClasspath

    when ("testPlatform".getExt()) {
        "okd" -> {
            systemProperties["test.release-management-host"] = ocTemplate.getOkdHost("rm")
            systemProperties["test.release-management-host-for-teamcity"] = ocTemplate.getOkdHost("rm")
            systemProperties["test.teamcity-2022-host"] = ocTemplate.getOkdHost("teamcity22")
            dependsOn(
                "ocCreateTeamcityServer",
                "ocCreateTeamcityAgent",
                "migrateMockData",
                "ocCreateComponentsRegistry",
                "ocCreateReleaseManagement"
            )
            finalizedBy(
                "ocLogsTeamcityServer",
                "ocLogsTeamcityAgent",
                "ocLogsComponentsRegistry",
                "ocLogsMockserver",
                "ocDeleteTeamcityPVC",
                "ocDeleteTeamcityAgent",
                "ocDeleteComponentsRegistry",
                "ocDeleteMockserver"
            )
        }
        "docker" -> {
            systemProperties["test.release-management-host"] = "localhost:8080"
            systemProperties["test.release-management-host-for-teamcity"] = "release-management-service:8080"
            systemProperties["test.teamcity-2022-host"] = "localhost:8111"
            dependsOn("migrateMockData")
            finalizedBy("composeLogs", "composeDown")
        }
    }
}

idea.module {
    scopes["PROVIDED"]?.get("plus")?.add(configurations["ftImplementation"])
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${properties["spring-boot.version"]}")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${properties["spring-cloud.version"]}")
    }
}

dependencies {
    ftImplementation(project(":client"))
    ftImplementation(project(":automation"))
    ftImplementation(project(":common"))
    ftImplementation(project(":test-common"))
    ftImplementation("org.junit.jupiter:junit-jupiter-engine")
    ftImplementation("org.junit.jupiter:junit-jupiter-params")
    ftImplementation("org.octopusden.octopus.octopus-external-systems-clients:teamcity-client:2.0.44")
}
