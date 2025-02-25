import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("io.github.rodm.teamcity-server")
    `maven-publish`
}

configurations {
    create("distributions")
}

val teamcityPlugin = artifacts.add(
    "distributions",
    layout.buildDirectory.file("distributions/$name${if (version == "unspecified") "" else "-$version"}.zip").get().asFile
) {
    type = "zip"
    builtBy("serverPlugin")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(teamcityPlugin)
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
    sign(publishing.publications["maven"])
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

teamcity {
    version = "2022.04"
    server {
        descriptor {
            name = project.name
            displayName = "Octopus Release Management TeamCity Plugin"
            version = project.version as String
            vendorName = "octopus"
            description = "Octopus module: ${project.name}"
            useSeparateClassloader = true
        }
    }
}

dependencyManagement {
    imports {
        mavenBom("io.github.openfeign:feign-bom:${properties["openfeign.version"]}")
    }
}

dependencies {
    implementation(project(":client"))
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
}
