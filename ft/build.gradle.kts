plugins {
    id("com.avast.gradle.docker-compose")
}

@Suppress("UNCHECKED_CAST")
val extValidateFun = project.ext["validateFun"] as ((List<String>) -> Unit)
fun String.getExt() = project.ext[this] as? String


configure<com.avast.gradle.dockercompose.ComposeExtension> {
    useComposeFiles.add("${projectDir}/docker/docker-compose.yml")
    waitForTcpPorts.set(true)
    captureContainersOutputToFiles.set(layout.buildDirectory.file("docker_logs").get().asFile)
    environment.putAll(
        mapOf(
            "APP_VERSION" to project.version,
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

tasks.named("composeUp") {
    dependsOn(":release-management-service:dockerBuildImage")
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

dependencies {
    ftImplementation(project(":client"))
    ftImplementation(project(":common"))
    ftImplementation(project(":test-common"))
    ftImplementation("org.junit.jupiter:junit-jupiter-engine")
    ftImplementation("org.junit.jupiter:junit-jupiter-params")
}
