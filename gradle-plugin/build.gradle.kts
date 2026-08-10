plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    // Gradle's own API (used by ProjectBuilder in tests) requires JVM 17+ to run — this is
    // build-time tooling, not Android runtime bytecode, so there's no minSdk-driven reason to
    // target 11 here like the `app`/`report` modules do.
    jvmToolchain(17)
}

gradlePlugin {
    plugins {
        create("obfuscationVerify") {
            id = "com.jalo.ngaburake.obfuscation-verify"
            implementationClass = "com.jalo.ngaburake.plugin.ObfuscationPlugin"
        }
    }
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(gradleTestKit())
}
