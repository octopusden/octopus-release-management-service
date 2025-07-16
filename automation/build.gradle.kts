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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(metarunners)
            pom {
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