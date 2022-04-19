import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val log4jVersion = "2.17.2"
val xmlunitVersion = "2.9.0"

plugins {
    kotlin("jvm") version "1.6.21"
    application

    id("com.github.ben-manes.versions") version "0.42.0"
}

group = "org.devshred"
version = "1.1-SNAPSHOT"

application {
    mainClass.set("org.devshred.tracks.AppKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // Google Docs API
    implementation("com.google.api-client:google-api-client:1.34.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.33.3")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20220411-1.32.1")

    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("io.jenetics:jpx:3.0.1")
    implementation("commons-io:commons-io:2.11.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")
    implementation("io.arrow-kt:arrow-core:1.0.1")

    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:$log4jVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:7.1.1")
    implementation("org.slf4j:slf4j-api:1.7.36")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.xmlunit:xmlunit-core:$xmlunitVersion")
    testImplementation("org.xmlunit:xmlunit-matchers:$xmlunitVersion")
    testImplementation("org.xmlunit:xmlunit-assertj:$xmlunitVersion")
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<Test> {
    useJUnitPlatform()
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
