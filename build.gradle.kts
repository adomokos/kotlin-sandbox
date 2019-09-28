plugins {
    application
    kotlin("jvm") version "1.3.21"
}

application {
    mainClassName = "samples.HelloWorldKt"
}

val spekVersion = "2.1.0-alpha.0.13+397dc38"
val kotlinVersion = "1.3.21"

dependencies {
    compile(kotlin("stdlib"))

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
	}
}

repositories {
	mavenLocal()
    jcenter()
	maven(url = "https://dl.bintray.com/spekframework/spek-dev/")
}
