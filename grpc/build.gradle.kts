plugins {
    kotlin("jvm")
    application
}

application {
    mainClassName = "grpc.MainKt"
}

dependencies {
    // testCompile rootProject
    // rootProject
    implementation(project(":"))
    implementation(kotlin("stdlib"))
}
