plugins {
    kotlin("jvm")
    application
}

application {
    mainClassName = "grpc.MainKt"
}

dependencies {
    implementation(kotlin("stdlib"))
}
