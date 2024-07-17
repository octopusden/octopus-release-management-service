dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${project.properties["spring-boot.version"]}")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${project.properties["spring-cloud.version"]}")
    }
}

dependencies {
    implementation(project(":common"))
    implementation("org.junit.jupiter:junit-jupiter-engine")
    implementation("org.junit.jupiter:junit-jupiter-params")
    implementation("com.fasterxml.jackson.core:jackson-databind")
}
