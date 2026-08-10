plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
}

group = "com.rezacah.ngaburake"
version = "0.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    testImplementation(libs.junit)
}
