plugins {
    application
    kotlin("jvm") version "1.3.21"
	kotlin("kapt") version "1.3.50"
	id("com.adarshr.test-logger") version "1.7.0"
}

application {
    mainClassName = "MainKt"
}

apply {
	plugin("com.adarshr.test-logger")
}

val spekVersion = "2.1.0-alpha.0.13+397dc38"
val kotlinVersion = "1.3.21"
val arrowVersion = "0.10.1-SNAPSHOT"

dependencies {
    compile(kotlin("stdlib"))

	// Exposed - db access
	compile("org.jetbrains.exposed:exposed:0.16.1")
	compile("org.xerial:sqlite-jdbc:3.21.0.1")

	// Arrow
	compile("io.arrow-kt:arrow-core:$arrowVersion")
    compile("io.arrow-kt:arrow-syntax:$arrowVersion")
	compile("io.arrow-kt:arrow-fx:$arrowVersion")
    kapt("io.arrow-kt:arrow-meta:$arrowVersion")

	// assertion
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")

	//spek2
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
}


tasks {
	withType<Test> {
		useJUnitPlatform {
			includeEngines("spek2")
		}

		testlogger {
			setTheme("mocha") // project level
		}
	}
}

repositories {
	mavenCentral()
    jcenter()
	maven(url = "https://dl.bintray.com/spekframework/spek-dev/")
	maven(url = "https://dl.bintray.com/arrow-kt/arrow-kt/")
	maven(url = "https://oss.jfrog.org/artifactory/oss-snapshot-local/") // for shapshot builds
}
