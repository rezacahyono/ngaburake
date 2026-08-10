plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain(11)
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
}
