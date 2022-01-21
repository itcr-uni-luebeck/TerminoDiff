import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.compose") version "1.0.0"
    id("org.openjfx.javafxplugin") version "0.0.10"
}

group = "de.uzl.itcr"
version = "1.0.0"

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    mavenCentral()
}

val hapiVersion = "5.6.2"
val slf4jVersion = "1.7.32"
val graphStreamVersion = "2.0"
val jGraphTVersion = "1.5.1"
val material3DesktopVersion = "1.0.0"
val jungraphtVersion = "1.3"

dependencies {
    testImplementation(kotlin("test"))
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.components:components-splitpane:1.0.1")
    implementation("org.jetbrains.compose.material3:material3-desktop:$material3DesktopVersion")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-base:$hapiVersion")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:$hapiVersion")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-validation:$hapiVersion")
    //implementation("ca.uhn.hapi.fhir:hapi-fhir-validation-resources-r4:$hapiVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation("org.jgrapht:jgrapht-core:$jGraphTVersion")
    implementation("org.jgrapht:jgrapht-ext:$jGraphTVersion")
    implementation("com.github.tomnelson:jungrapht-visualization:$jungraphtVersion")
    implementation("com.github.tomnelson:jungrapht-layout:$jungraphtVersion")
    implementation("net.mahdilamb:colormap:0.9.511")
    implementation("li.flor:native-j-file-chooser:1.6.4")
    implementation("javax.xml.bind:jaxb-api:2.4.0-b180830.0359") // provides org.xml.sax
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("com.formdev:flatlaf:2.0")
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

javafx {
    // add javafx to the classpath
    // TODO: 03/01/22 figure out a way to modularize this app, to suppress javafx message
    version = "17.0.1"
    modules("javafx.controls", "javafx.swing")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

compose.desktop {
    application {
        mainClass = "terminodiff.MainKt"
        nativeDistributions {
            val resourceDir = project.layout.projectDirectory.dir("resources")
            appResourcesRootDir.set(resourceDir)
            licenseFile.set(project.file("LICENSE"))
            packageName = "TerminoDiff"
            packageVersion = "1.0.0"
            description = "Visually compare HL7 FHIR Terminology"
            vendor = "IT Center for Clinical Reserach, University of Lübeck"
            copyright = "Joshua Wiedekopf / IT Center for Clinical Research, 2022-"

            linux {
                iconFile.set(resourceDir.file("terminodiff.png"))
                targetFormats(
                    TargetFormat.Deb,
                    TargetFormat.Rpm,
                    TargetFormat.AppImage,
                )
            }
            /*macOS {
                bundleID = "de.uzl.itcr.terminodiff"
                signing {
                    sign.set(false)
                }
                iconFile.set(resourceDir.file("macos/terminodiff.icns"))
                targetFormats(
                    TargetFormat.Dmg
                )
            }*/
            /*windows {
                iconFile.set(resourceDir.file("windows/terminodiff.ico"))
                perUserInstall = true
                dirChooser = true
                upgradeUuid = "ECFA19D9-D1F2-4AF5-9E5E-59A8F21C3A79"
                menuGroup = "TerminoDiff"
                targetFormats(
                    TargetFormat.Exe
                )
            }*/
        }
    }
}