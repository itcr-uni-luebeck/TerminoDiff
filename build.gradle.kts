import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val composeVersion = "1.0.0"

plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.compose") version "1.0.0"
}

group = "de.uzl.itcr"
version = "1.0"

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    mavenCentral()
}

val hapiVersion = "5.6.0"
val slf4jVersion = "1.7.32"

dependencies {
    testImplementation(kotlin("test"))
    implementation(compose.desktop.currentOs)
    implementation("ca.uhn.hapi.fhir:hapi-fhir-base:$hapiVersion")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:$hapiVersion")
    //implementation("ca.uhn.hapi.fhir:hapi-fhir-client-okhttp:$hapiVersion")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-validation:$hapiVersion")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-validation-resources-r4:$hapiVersion")
    implementation("org.slf4j:slf4j-api:${slf4jVersion}")
    implementation("org.slf4j:slf4j-simple:${slf4jVersion}")
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "TerminoDiff"
            packageVersion = "1.0.0"
        }
    }
}