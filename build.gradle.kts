plugins {
    kotlin("jvm") version "1.3.61"
    kotlin("kapt") version "1.3.61"
    id("com.adarshr.test-logger") version "2.0.0"
    id("org.jlleitschuh.gradle.ktlint") version "9.1.1"
    id("com.diffplug.gradle.spotless") version "3.27.1"
    id("io.gitlab.arturbosch.detekt").version("1.5.1")
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
val coroutinesVersion = "1.3.3"
val exposedVersion = "0.20.3"
val klaxonVersion = "5.2"
val kotlinTestVersion = "3.4.2"
val openCsvVersion = "5.1"
val kotlinFakerVersion = "1.1.1"

dependencies {
    implementation(kotlin("stdlib"))

    // Exposed - db access
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jodatime:$exposedVersion")
    implementation("org.xerial:sqlite-jdbc:3.30.1")

    // JSON parsing
    implementation("com.beust:klaxon:$klaxonVersion")

    // CSV Parsing
    implementation("com.opencsv:opencsv:$openCsvVersion")

    // Arrow
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
    implementation("io.arrow-kt:arrow-syntax:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx:$arrowVersion")
    implementation("io.arrow-kt:arrow-optics:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx-rx2:$arrowVersion")
    implementation("io.arrow-kt:arrow-mtl:$arrowVersion")
    kapt("io.arrow-kt:arrow-meta:$arrowVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // clikt - for command line args parsing
    implementation("com.github.ajalt:clikt:2.4.0")

    // kotlintest
    testImplementation("io.kotlintest:kotlintest-runner-junit5:$kotlinTestVersion") {
        // https://github.com/kotlintest/kotlintest/issues/1026
        exclude("io.arrow-kt")
    }
    testImplementation("io.kotlintest:kotlintest-assertions-arrow:$kotlinTestVersion") {
        exclude("io.arrow-kt")
    }
    kaptTest("io.arrow-kt:arrow-meta:$arrowVersion")

    testImplementation("io.github.serpro69:kotlin-faker:$kotlinFakerVersion")
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

    withType<Jar> {
        archiveClassifier.set("uber")

        manifest {
            attributes["Main-Class"] = application.mainClassName
        }
        from(sourceSets.main.get().output)

        dependsOn(configurations.runtimeClasspath)
        from({
            exclude("META-INF/LICENSE.txt")
            exclude("META-INF/NOTICE.txt")
            configurations.runtimeClasspath.get().map {
                if (it.isDirectory)
                    it
                else
                    zipTree(it)
            }
        })
    }
}

detekt {
    toolVersion = "1.2.2"
    input = files("src/main/kotlin", "src/test/kotlin")
    // filters = ".*/resources/.*,.*/build/.*"
    // baseline = file("my-detekt-baseline.xml") // Just if you want to create a baseline file.
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/arrow-kt/arrow-kt/")
    maven(url = "https://oss.jfrog.org/artifactory/oss-snapshot-local/") // for shapshot builds
    maven(url = "https://dl.bintray.com/serpro69/maven/")
}
