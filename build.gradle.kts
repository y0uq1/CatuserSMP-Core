import org.gradle.api.tasks.compile.JavaCompile

plugins {
    java
}

group = "com.catusersmp"
version = "1.0.0"

val paperApiVersion = "1.21.11-R0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 21
}

// Injects the project version into plugin.yml so the JAR and the descriptor never drift apart.
tasks.processResources {
    val placeholders = mapOf("version" to project.version.toString())
    inputs.properties(placeholders)
    filesMatching("plugin.yml") {
        expand(placeholders)
    }
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to rootProject.name,
            "Implementation-Version" to project.version,
        )
    }
}
