plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    application
}

repositories { mavenCentral() }

dependencies {
    // Koog + Ollama
    implementation("ai.koog:koog-agents:0.4.1")

    // Kotlin BOM
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:2.2.0"))
    implementation(kotlin("stdlib"))

    // Core Kotlin libraries
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // Phase 1: Core Infrastructure
    // Ktor - Modern web framework
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("io.ktor:ktor-server-cors:2.3.7")
    implementation("io.ktor:ktor-server-call-logging:2.3.7")
    implementation("io.ktor:ktor-server-status-pages:2.3.7")

    // Koin - Dependency injection
    implementation("io.insert-koin:koin-core:3.5.3")
    implementation("io.insert-koin:koin-ktor:3.5.3")

    // Logback - Logging framework
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Exposed - Database framework
    implementation("org.jetbrains.exposed:exposed-core:0.44.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.44.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.44.1")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.44.1")

    // H2 Database (for development)
    implementation("com.h2database:h2:2.2.224")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.ktor:ktor-server-test-host:2.3.7")
    testImplementation("io.insert-koin:koin-test:3.5.3")
}

kotlin { jvmToolchain(17) }

application {
    mainClass.set("dev.craftmind.agent.MainKt")
}

tasks.named<Test>("test") { useJUnitPlatform() }

// Configure JAR to include main class and dependencies
tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "dev.craftmind.agent.MainKt"
    }
    
    // Include all dependencies in the JAR
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    
    // Avoid duplicate files
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}