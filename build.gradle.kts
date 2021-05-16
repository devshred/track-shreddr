import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val log4jVersion = "2.14.1"

plugins {
    kotlin("jvm") version "1.5.0"
    application

    id("com.github.ben-manes.versions") version "0.38.0"
}

group = "org.devshred"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("org.devshred.tracks.AppKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // Google Docs API
    implementation("com.google.api-client:google-api-client:1.30.10")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.31.0")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20200813-1.30.10")

    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("io.jenetics:jpx:2.2.0")
    implementation("commons-io:commons-io:2.8.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.3")

    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:$log4jVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:6.6")
    implementation("org.slf4j:slf4j-api:1.7.30")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.xmlunit:xmlunit-core:2.8.2")
    testImplementation("org.xmlunit:xmlunit-matchers:2.8.2")
    testImplementation("org.xmlunit:xmlunit-assertj:2.8.2")
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks {
    dependencyUpdates {
        resolutionStrategy {
            componentSelection {
                all {
                    val rejected = listOf("alpha", "beta", "rc", "cr", "m", "preview", "b", "ea")
                        .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-+]*") }
                        .any { it.matches(candidate.version) }
                    if (rejected) {
                        reject("Release candidate")
                    }
                }
            }
        }
    }
}
