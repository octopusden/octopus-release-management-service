plugins {
    id("com.avast.gradle.docker-compose")
}

@Suppress("UNCHECKED_CAST")
val extValidateFun = project.ext["validateFun"] as ((List<String>) -> Unit)
fun String.getExt() = project.ext[this] as? String


configure<com.avast.gradle.dockercompose.ComposeExtension> {
    useComposeFiles.add(layout.projectDirectory.file("docker/docker-compose.yml").asFile.path)
    waitForTcpPorts.set(true)
    captureContainersOutputToFiles.set(layout.buildDirectory.dir("docker-logs"))
    environment.putAll(
        mapOf(
            "RELEASE_MANAGEMENT_SERVICE_VERSION" to version,
            "MOCKSERVER_VERSION" to properties["mockserver.version"],
            "TEAMCITY_VERSION" to "2022.04.7",
            "DOCKER_REGISTRY" to "dockerRegistry".getExt(),
            "OCTOPUS_GITHUB_DOCKER_REGISTRY" to "octopusGithubDockerRegistry".getExt()
        )
    )
}

tasks.getByName("composeUp").doFirst {
    extValidateFun.invoke(listOf("dockerRegistry", "octopusGithubDockerRegistry"))
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

val ft by tasks.creating(Test::class) {
    group = "verification"
    description = "Runs the integration tests"

    testClassesDirs = sourceSets["ft"].output.classesDirs
    classpath = sourceSets["ft"].runtimeClasspath
}

dockerCompose.isRequiredBy(ft)

val teamcityDir = layout.buildDirectory.dir("teamcity-server")

tasks.register<Sync>("prepareTeamcityServerData") {
    from(zipTree(layout.projectDirectory.file("docker/data.zip")))
    into(teamcityDir)
}

tasks.register<Copy>("deployTeamcityPlugin") {
    dependsOn("prepareTeamcityServerData")
    dependsOn(":release-management-teamcity-plugin:serverPlugin")
    from(rootProject.project("release-management-teamcity-plugin").configurations["distributions"].artifacts.files)
    into(teamcityDir.get().dir("datadir/plugins"))
}

tasks.named("composeUp") {
    dependsOn(":release-management-service:dockerBuildImage")
    dependsOn("deployTeamcityPlugin")
}

tasks.named("migrateMockData") {
    dependsOn("composeUp")
}

tasks.named("ft") {
    dependsOn("migrateMockData")
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
    ftImplementation(project(":common"))
    ftImplementation(project(":test-common"))
    ftImplementation("org.junit.jupiter:junit-jupiter-engine")
    ftImplementation("org.junit.jupiter:junit-jupiter-params")
    ftImplementation("org.octopusden.octopus.octopus-external-systems-clients:teamcity-client:2.0.44")
}
