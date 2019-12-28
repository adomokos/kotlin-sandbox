plugins {
    kotlin("jvm") version "1.3.61"
    kotlin("kapt") version "1.3.61"
    id("com.adarshr.test-logger") version "2.0.0"
    id("org.jlleitschuh.gradle.ktlint") version "9.1.1"
    id("com.diffplug.gradle.spotless") version "3.26.1"
    id("io.gitlab.arturbosch.detekt").version("1.2.2")
    id("com.github.ben-manes.versions").version("0.27.0")
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

val arrowVersion = "0.10.4"
val kotlinTestVersion = "3.4.2"
val klaxonVersion = "5.2"
val coroutinesVersion = "1.3.3"

dependencies {
    implementation(kotlin("stdlib"))

    // Exposed - db access
    implementation("org.jetbrains.exposed:exposed:0.17.7")
    implementation("org.xerial:sqlite-jdbc:3.28.0")

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

    testImplementation("io.github.serpro69:kotlin-faker:1.1")
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

detekt {
    toolVersion = "1.2.2"
    input = files("src/main/kotlin", "src/test/kotlin")
    filters = ".*/resources/.*,.*/build/.*"
    // baseline = file("my-detekt-baseline.xml") // Just if you want to create a baseline file.
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/arrow-kt/arrow-kt/")
    maven(url = "https://oss.jfrog.org/artifactory/oss-snapshot-local/") // for shapshot builds
    maven(url = "https://dl.bintray.com/serpro69/maven/")
}
