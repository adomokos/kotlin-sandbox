plugins {
    kotlin("jvm") version "1.3.50"
    kotlin("kapt") version "1.3.50"
    id("com.adarshr.test-logger") version "1.7.1"
    id("com.gradle.build-scan") version "2.1"
    id("org.jlleitschuh.gradle.ktlint") version "9.0.0"
    application
}

application {
    mainClassName = "MainKt"
}

apply {
    plugin("com.adarshr.test-logger")
}

val arrowVersion = "0.10.1-SNAPSHOT"
val kotlinTestVersion = "3.4.2"

dependencies {
    implementation(kotlin("stdlib"))

    // Exposed - db access
    implementation("org.jetbrains.exposed:exposed:0.16.1")
    implementation("org.xerial:sqlite-jdbc:3.21.0.1")

    // Arrow
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
    implementation("io.arrow-kt:arrow-syntax:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx:$arrowVersion")
    kapt("io.arrow-kt:arrow-meta:$arrowVersion")

    // Coroutins
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.0")

    // kotlintest
    testImplementation("io.kotlintest:kotlintest-runner-junit5:$kotlinTestVersion") {
        // https://github.com/kotlintest/kotlintest/issues/1026
        exclude("io.arrow-kt")
    }
    testImplementation("io.kotlintest:kotlintest-assertions-arrow:$kotlinTestVersion") {
        exclude("io.arrow-kt")
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform()

        testlogger {
            setTheme("mocha") // project level
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
