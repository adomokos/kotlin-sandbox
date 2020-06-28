import com.google.protobuf.gradle.*

plugins {
    id("com.google.protobuf") version("0.8.12")
    kotlin("jvm")
    application
}

application {
    mainClassName = "grpc.MainKt"
}

val grpcVersion = "1.30.2" // CURRENT_grpcVersion
val protobufVersion = "3.12.3"
val grpcKotlinVersion = "0.1.4"
val coroutinesVersion = "1.3.7"

dependencies {
    // rootProject
    implementation(project(":"))
    implementation(kotlin("stdlib"))

    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")

    // KGen needed
    implementation("com.github.googleapis.gax-kotlin:kgax-grpc:0.6.0")

    // Java
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")

      // Grpc and Protobuf
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.8.12")
    implementation("com.google.protobuf:protobuf-java:3.12.2")
    implementation("com.google.protobuf:protobuf-java-util:3.12.2")
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
}

// Workaround for the Gradle bug issue:
// https://github.com/google/protobuf-gradle-plugin/issues/391
configurations.forEach {
    if (it.name.toLowerCase().contains("proto")) {
        it.attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "java-runtime"))
    }
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/java")
            srcDirs("build/generated/source/proto/main/grpckt")
        }
    }
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:$protobufVersion" }
    plugins {
        // Specify protoc to generate using kotlin protobuf plugin
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        // Specify protoc to generate using our grpc kotlin plugin
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without options.
                id("grpc")
                // Generate Kotlin gRPC using the custom plugin from library
                id("grpckt")
            }
        }
    }
}

repositories {
    // jitpack releases are required until we start publishing to maven
    maven(url = "https://jitpack.io")
}
