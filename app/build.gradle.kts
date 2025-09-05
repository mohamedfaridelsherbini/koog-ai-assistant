plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    application
}

repositories { mavenCentral() }

dependencies {
    // Koog + Ollama
    implementation("ai.koog:koog-agents:0.4.1")

    // محاذاة نسخ كوتلن
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:2.2.0"))
    implementation(kotlin("stdlib"))

    // أدوات هنحتاجها
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    testImplementation(kotlin("test"))
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