plugins {
    kotlin("jvm") version "1.3.72"
    kotlin("kapt") version "1.3.72"
    id("com.adarshr.test-logger") version "2.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "9.3.0"
    id("com.diffplug.gradle.spotless") version "4.5.1"
    id("io.gitlab.arturbosch.detekt").version("1.10.0")
    id("com.github.ben-manes.versions").version("0.28.0")
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

val arrowVersion = "0.10.5"
val coroutinesVersion = "1.3.7"
val exposedVersion = "0.26.1"
val klaxonVersion = "5.2"
val kotestVersion = "4.1.3"
val openCsvVersion = "5.2"
val kotlinFakerVersion = "1.4.0"
val cliktVersion = "2.6.0"
val ktorVersion = "1.3.2"
val logbackVersion = "1.2.3"
val ktlintVersion = "0.37.2"

allprojects {
    apply(plugin = "java")
    apply(plugin = "com.adarshr.test-logger")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        // debug.set(true)
        // verbose.set(true)
        version.set(ktlintVersion)
        enableExperimentalRules.set(true)
    }

    dependencies {
        implementation(kotlin("stdlib"))

        // Arrow
        implementation("io.arrow-kt:arrow-core:$arrowVersion")
        implementation("io.arrow-kt:arrow-syntax:$arrowVersion")
        implementation("io.arrow-kt:arrow-fx:$arrowVersion")
        implementation("io.arrow-kt:arrow-optics:$arrowVersion")
        implementation("io.arrow-kt:arrow-fx-rx2:$arrowVersion")
        implementation("io.arrow-kt:arrow-mtl:$arrowVersion")

        // kotest
        testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
        testImplementation("io.kotest:kotest-assertions-arrow:$kotestVersion")
        testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
        testImplementation("io.kotest:kotest-property-jvm:$kotestVersion")
        testImplementation("io.kotest:kotest-runner-console-jvm:$kotestVersion")

        testImplementation("io.github.serpro69:kotlin-faker:$kotlinFakerVersion")
    }

    repositories {
        mavenCentral()
        jcenter()
        maven(url = "https://dl.bintray.com/arrow-kt/arrow-kt/")
        maven(url = "https://oss.jfrog.org/artifactory/oss-snapshot-local/") // for shapshot builds
        maven(url = "https://dl.bintray.com/serpro69/maven/")
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

        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlin.Experimental"
            kotlinOptions.jvmTarget = "1.8"
        }
    }
}

// tasks {
    /*
    withType<Jar> {
        archiveClassifier.set("uber")

        manifest {
            attributes["Main-Class"] = application.mainClassName
        }
        from(sourceSets.main.get().output)

        dependsOn(configurations.runtimeClasspath)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
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
    */
// }

dependencies {
    // Exposed - db access
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jodatime:$exposedVersion")
    implementation("org.xerial:sqlite-jdbc:3.32.3.1")

    // JSON parsing
    implementation("com.beust:klaxon:$klaxonVersion")

    // CSV Parsing
    implementation("com.opencsv:opencsv:$openCsvVersion")

    kapt("io.arrow-kt:arrow-meta:$arrowVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // clikt - for command line args parsing
    implementation("com.github.ajalt:clikt:$cliktVersion")

    kaptTest("io.arrow-kt:arrow-meta:$arrowVersion")
}

detekt {
    debug = true
    input = files("src/main/kotlin", "src/test/kotlin")
    buildUponDefaultConfig = true
    config = files("resources/detekt-config.yml")
    // filters = ".*/resources/.*,.*/build/.*"
    // baseline = file("my-detekt-baseline.xml") // Just if you want to create a baseline file.
}
