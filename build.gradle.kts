import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.net.InetAddress
import java.util.zip.CRC32

plugins {
    java
    idea
    id("io.spring.dependency-management")
    id("org.jetbrains.kotlin.jvm")
    id("io.github.gradle-nexus.publish-plugin")
    id("org.jetbrains.kotlinx.kover")
    id("org.owasp.dependencycheck")
    id("io.gitlab.arturbosch.detekt") apply false
    id("org.jlleitschuh.gradle.ktlint") apply false
    signing
}

val defaultVersion = "${
    with(CRC32()) {
        update(InetAddress.getLocalHost().hostName.toByteArray())
        value
    }
}-SNAPSHOT"

fun requiredIntProperty(propertyName: String): Int {
    val value = findProperty(propertyName)?.toString()?.trim()
        ?: throw IllegalStateException("Missing Gradle property '$propertyName' required for Kover verification")
    return value.toIntOrNull()
        ?: throw IllegalStateException("Gradle property '$propertyName' must be an integer, but was '$value'")
}

allprojects {
    group = "org.octopusden.octopus.release-management-service"
    if (version == "unspecified") {
        version = defaultVersion
    }
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

dependencies {
    val coverageProjects = listOf(
        ":automation",
        ":client",
        ":common",
        ":legacy-releng-client",
        ":release-management-service",
        ":release-management-teamcity-plugin"
    )
    coverageProjects.forEach { add("kover", project(it)) }
}

kover {
    reports {
        verify {
            rule("Line coverage") {
                bound {
                    minValue.set(requiredIntProperty("coverage.line.min"))
                    coverageUnits = CoverageUnit.LINE
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                }
            }
            rule("Branch coverage") {
                bound {
                    minValue.set(requiredIntProperty("coverage.branch.min"))
                    coverageUnits = CoverageUnit.BRANCH
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                }
            }
        }
    }
}

dependencyCheck {
    failBuildOnCVSS = 11.0F
    suppressionFile = "$rootDir/config/owasp/suppressions.xml"
    formats = listOf("HTML", "JSON", "SARIF")
    outputDirectory.set(layout.buildDirectory.dir("reports/dependency-check"))
}

tasks.register("qualityStatic") {
    group = "verification"
    description = "Runs static analysis checks for all modules."
    dependsOn(subprojects.map { "${it.path}:detekt" })
    dependsOn(subprojects.map { "${it.path}:ktlintCheck" })
}

tasks.register("qualityCoverage") {
    group = "verification"
    description = "Runs tests, generates merged Kover XML report and verifies coverage thresholds."
    dependsOn(subprojects.map { "${it.path}:test" })
    dependsOn(tasks.matching { it.name == "koverMergedXmlReport" || it.name == "koverXmlReport" })
    dependsOn(tasks.matching { it.name == "koverMergedVerify" || it.name == "koverVerify" })
}

tasks.register("qualityCheck") {
    group = "verification"
    description = "Runs all mandatory quality gates."
    dependsOn("qualityStatic", "qualityCoverage")
}

tasks.named("check") {
    dependsOn(tasks.matching { it.name == "koverXmlReport" || it.name == "koverHtmlReport" })
}

tasks.register("securityReport") {
    group = "verification"
    description = "Runs security checks in report-only mode."
    dependsOn(
        if (tasks.names.contains("dependencyCheckAggregate")) {
            "dependencyCheckAggregate"
        } else {
            "dependencyCheckAnalyze"
        }
    )
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "idea")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlinx.kover")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
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

    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        baseline = file("$projectDir/detekt-baseline.xml")
        ignoreFailures = false
    }

    tasks.withType<Detekt>().configureEach {
        reports {
            xml.required.set(true)
            html.required.set(true)
            sarif.required.set(true)
            txt.required.set(false)
        }
    }

    extensions.configure<KtlintExtension> {
        ignoreFailures.set(false)
        outputToConsole.set(true)
        reporters {
            reporter(ReporterType.PLAIN)
            reporter(ReporterType.CHECKSTYLE)
        }
        baseline.set(file("$projectDir/ktlint-baseline.xml"))
        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
            include("**/src/**/*.kt")
        }
    }

    tasks.matching {
        it.name == "runKtlintCheckOverKotlinScripts" ||
                it.name == "ktlintKotlinScriptCheck" ||
                it.name == "runKtlintFormatOverKotlinScripts" ||
                it.name == "ktlintKotlinScriptFormat"
    }.configureEach {
        enabled = false
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging{
            info.events = setOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        }
        systemProperties["release-management-service.version"] = version
    }

    ext {
        System.getenv().let {
            set("signingRequired", it.containsKey("ORG_GRADLE_PROJECT_signingKey") && it.containsKey("ORG_GRADLE_PROJECT_signingPassword"))
            set("testPlatform", it.getOrDefault("TEST_PLATFORM", properties["test.platform"]))
            set("testMockserverPort", it.getOrDefault("TEST_MOCKSERVER_PORT", properties["test.mockserver.port"]))
            set("dockerRegistry", it.getOrDefault("DOCKER_REGISTRY", properties["docker.registry"]))
            set("octopusGithubDockerRegistry", it.getOrDefault("OCTOPUS_GITHUB_DOCKER_REGISTRY", project.properties["octopus.github.docker.registry"]))
            set("okdActiveDeadlineSeconds", it.getOrDefault("OKD_ACTIVE_DEADLINE_SECONDS", properties["okd.active-deadline-seconds"]))
            set("okdProject", it.getOrDefault("OKD_PROJECT", properties["okd.project"]))
            set("okdClusterDomain", it.getOrDefault("OKD_CLUSTER_DOMAIN", properties["okd.cluster-domain"]))
            set("okdWebConsoleUrl", (it.getOrDefault("OKD_WEB_CONSOLE_URL", properties["okd.web-console-url"]) as String).trimEnd('/'))
        }
    }
    val supportedTestPlatforms = listOf("docker", "okd")
    if (project.ext["testPlatform"] !in supportedTestPlatforms) {
        throw IllegalArgumentException("Test platform must be set to one of the following $supportedTestPlatforms. Start gradle build with -Ptest.platform=... or set env variable TEST_PLATFORM")
    }
    val mandatoryProperties = mutableListOf("dockerRegistry", "octopusGithubDockerRegistry")
    if (project.ext["testPlatform"] == "okd") {
        mandatoryProperties.add("okdActiveDeadlineSeconds")
        mandatoryProperties.add("okdProject")
        mandatoryProperties.add("okdClusterDomain")
    }
    val undefinedProperties = mandatoryProperties.filter { (project.ext[it] as String).isBlank() }
    if (undefinedProperties.isNotEmpty()) {
        throw IllegalArgumentException(
            "Start gradle build with" +
                    (if (undefinedProperties.contains("dockerRegistry")) " -Pdocker.registry=..." else "") +
                    (if (undefinedProperties.contains("octopusGithubDockerRegistry")) " -Poctopus.github.docker.registry=..." else "") +
                    (if (undefinedProperties.contains("okdActiveDeadlineSeconds")) " -Pokd.active-deadline-seconds=..." else "") +
                    (if (undefinedProperties.contains("okdProject")) " -Pokd.project=..." else "") +
                    (if (undefinedProperties.contains("okdClusterDomain")) " -Pokd.cluster-domain=..." else "") +
                    " or set env variable(s):" +
                    (if (undefinedProperties.contains("dockerRegistry")) " DOCKER_REGISTRY" else "") +
                    (if (undefinedProperties.contains("octopusGithubDockerRegistry")) " OCTOPUS_GITHUB_DOCKER_REGISTRY" else "") +
                    (if (undefinedProperties.contains("okdActiveDeadlineSeconds")) " OKD_ACTIVE_DEADLINE_SECONDS" else "") +
                    (if (undefinedProperties.contains("okdProject")) " OKD_PROJECT" else "") +
                    (if (undefinedProperties.contains("okdClusterDomain")) " OKD_CLUSTER_DOMAIN" else "")
        )
    }
}
