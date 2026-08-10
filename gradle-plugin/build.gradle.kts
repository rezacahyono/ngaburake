plugins {
    `java-gradle-plugin`
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
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
