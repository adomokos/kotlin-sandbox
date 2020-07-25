plugins {
    kotlin("jvm")
    application
    // id("com.adarshr.test-logger")
}

application {
    mainClassName = "web.AppKt"
}

val ktorVersion = "1.3.2"

dependencies {
    implementation(kotlin("stdlib"))

    // rootProject
    implementation(project(":"))

    // Ktor
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}
