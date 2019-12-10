plugins {
    kotlin("jvm") version "1.3.61"
    kotlin("kapt") version "1.3.50"
    id("com.adarshr.test-logger") version "1.7.1"
    id("com.gradle.build-scan") version "2.1"
    id("org.jlleitschuh.gradle.ktlint") version "9.0.0"
    id("com.diffplug.gradle.spotless") version "3.25.0"
    application
}

application {
    mainClassName = "MainKt"
}

apply {
    plugin("com.adarshr.test-logger")
    // from(rootProject.file("gradle/generated-kotlin-sources.gradle"))
}

spotless {
    kotlin {
        ktlint()
    }
    kotlinGradle {
        ktlint()
    }
}

val arrowVersion = "0.10.3"
val kotlinTestVersion = "3.4.2"
val klaxonVersion = "5.0.1"
val coroutinesVersion = "1.3.2"

dependencies {
    implementation(kotlin("stdlib"))

    // Exposed - db access
    implementation("org.jetbrains.exposed:exposed:0.16.1")
    implementation("org.xerial:sqlite-jdbc:3.21.0.1")

    // JSON parsing
    implementation("com.beust:klaxon:$klaxonVersion")

    // Arrow
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
    implementation("io.arrow-kt:arrow-syntax:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx:$arrowVersion")
    implementation("io.arrow-kt:arrow-optics:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx-rx2:$arrowVersion")
    kapt("io.arrow-kt:arrow-meta:$arrowVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // kotlintest
    testImplementation("io.kotlintest:kotlintest-runner-junit5:$kotlinTestVersion") {
        // https://github.com/kotlintest/kotlintest/issues/1026
        exclude("io.arrow-kt")
    }
    testImplementation("io.kotlintest:kotlintest-assertions-arrow:$kotlinTestVersion") {
        exclude("io.arrow-kt")
    }
    kaptTest("io.arrow-kt:arrow-meta:$arrowVersion")
}

tasks {
    withType<Test> {
        useJUnitPlatform()

        testlogger {
            setTheme("mocha") // project level
            // setShowSimpleNames(true)
            // setShowStandardStreams(true)
        }
    }
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/arrow-kt/arrow-kt/")
    maven(url = "https://oss.jfrog.org/artifactory/oss-snapshot-local/") // for shapshot builds
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}
