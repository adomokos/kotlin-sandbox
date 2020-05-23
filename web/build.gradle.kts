plugins {
    kotlin("jvm")
    application
}

application {
    mainClassName = "web.AppKt"
}

val ktorVersion = "1.3.2"

dependencies {
    // rootProject
    implementation(project(":"))
    implementation(kotlin("stdlib"))

    // Ktor
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
}
