import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("io.github.rodm.teamcity-server")
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
    version = "2021.1"
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
