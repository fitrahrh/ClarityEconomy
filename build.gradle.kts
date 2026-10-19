plugins {
    java
    id("eclipse")
    id("com.gradleup.shadow") version "9.6.1"
}

group = "com.harukiyu.clarityeconomy"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
    maven("https://repo.opencollab.dev/main/")
}

dependencies {
    // Paper API 26.3
    compileOnly("io.papermc.paper:paper-api:26.3.build.200-stable")
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("net.kyori:adventure-nbt:5.2.0")

    // Core Plugins & Hooks
    compileOnly(files("libs/ClarityLangCore-1.0.0.jar"))
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    // Geyser / Bedrock Cumulus & Floodgate API
    compileOnly("org.geysermc.floodgate:api:2.2.3-SNAPSHOT")
    compileOnly("org.geysermc.geyser:api:2.4.2-SNAPSHOT")
    compileOnly("org.geysermc.cumulus:cumulus:1.1.2")

    // Testing & YAML Validation
    testImplementation("io.papermc.paper:paper-api:26.3.build.200-stable")
    testImplementation("org.yaml:snakeyaml:2.2")
    testImplementation("net.kyori:adventure-text-minimessage:4.17.0")
    testImplementation("net.kyori:adventure-text-serializer-plain:4.17.0")
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

eclipse {
    classpath {
        plusConfigurations.add(configurations.compileClasspath.get())
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
        expand(props)
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
}

tasks.test {
    useJUnitPlatform()
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
