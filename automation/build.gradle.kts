import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    application
    id("com.github.johnrengelman.shadow")
    `maven-publish`
}

group = "org.octopusden.octopus.automation.release-management"
description = "Octopus Release Management Automation"

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        suppressWarnings = true
        jvmTarget = "1.8"
    }
}

java.targetCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("io.github.openfeign:feign-bom:${properties["openfeign.version"]}")
    }
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.3.14")
    implementation("com.github.ajalt.clikt:clikt:4.4.0")
    implementation(project(":client"))
    implementation("org.apache.velocity:velocity-engine-core:2.3")
    implementation("org.apache.commons:commons-text:1.13.1")
}

application {
    mainClass = "org.octopusden.octopus.automation.releasemanagement.ApplicationKt"
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest { attributes(mapOf("Main-Class" to application.mainClass)) }
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.register<Zip>("zipMetarunners") {
    archiveFileName = "metarunners.zip"
    from(layout.projectDirectory.dir("metarunners")) {
        expand(properties)
    }
}

configurations {
    create("distributions")
}

val metarunners = artifacts.add(
    "distributions",
    layout.buildDirectory.file("distributions/metarunners.zip").get().asFile
) {
    classifier = "metarunners"
    type = "zip"
    builtBy("zipMetarunners")
}

// The shadow plugin adds shadowRuntimeElements as a variant of components["java"] once
// maven-publish is applied, which is how the 20 MB shadow jar has been reaching Maven Central
// without anyone asking for it. Dropping the variant leaves the thin jar, sources and javadoc.
(components["java"] as AdhocComponentWithVariants)
    .withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) { skip() }

fun MavenPublication.automationPom() = pom {
    name.set(project.name)
    description.set(project.description)
    url.set("https://github.com/octopusden/${rootProject.name}.git")
    licenses {
        license {
            name.set("The Apache License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }
    scm {
        url.set("https://github.com/octopusden/${rootProject.name}.git")
        connection.set("scm:git://github.com/octopusden/${rootProject.name}.git")
    }
    developers {
        developer {
            id.set("octopus")
            name.set("octopus")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(metarunners)
            automationPom()
        }

        // The shadow jar, off Central and onto GitHub Packages. It cannot simply stop being
        // published: the OctopusReleaseManagementAutomation metarunner resolves it as
        // ${group}:${name}:${version}:jar:all through a TeamCity Maven2 runner, and that runner is
        // what makes the download work on both Windows and Linux agents — a GitHub release asset
        // would not. Same coordinates as above, distinguished only by the classifier.
        //
        // Which publication goes where is decided by the release workflow's
        // github-packages-publications input, not here.
        create<MavenPublication>("shadow") {
            artifact(tasks.named("shadowJar"))
            automationPom()
        }
    }

    repositories {
        // The name is the contract the release workflow's routing keys off.
        maven {
            name = "GitHubPackages"
            url = uri(
                "https://maven.pkg.github.com/" +
                    (System.getenv("GITHUB_REPOSITORY") ?: "octopusden/${rootProject.name}")
            )
            // Set by the workflow. GITHUB_TOKEN is not an ambient variable, and GITHUB_ACTOR is a
            // built-in whose value varies by trigger.
            credentials {
                username = System.getenv("GITHUB_PACKAGES_USERNAME")
                password = System.getenv("GITHUB_PACKAGES_TOKEN")
            }
        }
    }
}

signing {
    isRequired = project.ext["signingRequired"] as Boolean
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["maven"])
}

tasks.distZip.get().isEnabled = false
tasks.shadowDistZip.get().isEnabled = false
tasks.distTar.get().isEnabled = false
tasks.shadowDistTar.get().isEnabled = false