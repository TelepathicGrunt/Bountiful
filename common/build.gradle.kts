plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.6.0"
}

architectury {
    // Set up Architectury for the common project.
    // This sets up the transformations (@ExpectPlatform etc.) we need for production environments.
    common(
            "fabric",
            "forge",
    )
}

loom {
    accessWidenerPath.set(file("src/main/resources/bountiful.accesswidener"))
}
repositories {
    mavenCentral()
    mavenLocal()
    maven {
        name = "Shedaniel"
        url = uri("https://maven.terraformersmc.com/releases/")
    }
}

dependencies {
    // Add dependencies on the required Kotlin modules.
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    modImplementation("net.fabricmc:fabric-loader:0.14.17")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.ejekta:kambrik-common:123")
    modImplementation("me.shedaniel.cloth:cloth-config:10.0.96")
}

kotlin {
    jvmToolchain(17)
}

