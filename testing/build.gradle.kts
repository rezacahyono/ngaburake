plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.rezacah.ngaburake.testing"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(project(":runtime"))
    implementation("com.rezacah.ngaburake:report:0.1.0")
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
