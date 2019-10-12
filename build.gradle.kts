plugins {
    application
    kotlin("jvm") version "1.3.21"
	kotlin("kapt") version "1.3.50"
	id("com.adarshr.test-logger") version "1.7.0"
    id("com.gradle.build-scan") version "2.1"
}

application {
    mainClassName = "MainKt"
}

apply {
	plugin("com.adarshr.test-logger")
}

val kotlinVersion = "1.3.21"
val arrowVersion = "0.10.1-SNAPSHOT"
val kotlinTestJunitVersion = "3.3.2"

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

	// kotlintest
	testImplementation("io.kotlintest:kotlintest-runner-junit5:$kotlinTestJunitVersion")
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
