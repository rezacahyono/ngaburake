import org.gradle.plugin.compatibility.compatibility

plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gradle.plugin.publish)
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

// Plugin Portal requires a fixed (non-SNAPSHOT) version. Group must share the same top-level
// namespace as the plugin ID below. Domain ownership (rezacah.com) is verified by the Portal
// during the initial manual review — a fixed TXT record isn't known ahead of time, the
// reviewer provides it by email when needed.
group = "com.rezacah.ngaburake"
version = "0.1.1"

gradlePlugin {
    website = "https://github.com/rezacahyono/ngaburake"
    vcsUrl = "https://github.com/rezacahyono/ngaburake"
    plugins {
        create("obfuscationVerify") {
            id = "com.rezacah.ngaburake.obfuscation-verify"
            implementationClass = "com.rezacah.ngaburake.plugin.ObfuscationPlugin"
            displayName = "Obfuscation Verify"
            description = "Verifies that ProGuard/R8 obfuscation actually renamed configured " +
                "sensitive classes, not just that minification is enabled. Parses the R8 " +
                "mapping.txt, checks a developer-declared list of sensitive classes, and " +
                "generates a console/JSON report — optionally failing the build as a CI " +
                "security gate."
            tags = listOf("proguard", "r8", "obfuscation", "security", "android")

            // Declared per Plugin Portal policy (v2.1.0+) — task inputs/outputs are all
            // annotated (@Input/@InputFile/@OutputDirectory) with no unmanaged mutable state,
            // so Configuration Cache is supported.
            compatibility {
                features {
                    configurationCache = true
                }
            }
        }
    }
}

dependencies {
    implementation("com.rezacah.ngaburake:report:0.1.0")
    implementation("com.rezacah.ngaburake:mapping:0.1.1")
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(gradleTestKit())
}
