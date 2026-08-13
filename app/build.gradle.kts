import com.rezacah.ngaburake.report.ReportFormat

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.rezacah.ngaburake.obfuscation-verify")
}

android {
    namespace = "com.rezacah.ngaburake"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.rezacah.ngaburake"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

// Copies R8's real mapping.txt into assets so ObfuscationSDK.withMappingFile() cross-checks
// against actual obfuscation output, not a hand-written fixture. Single-pass: the mustRunAfter
// wiring below orders this strictly between minify/compose-mapping and mergeReleaseAssets, so
// the same assembleRelease run that produces mapping.txt is the one that embeds it.
val copyReleaseMappingToAssets = tasks.register<Copy>("copyReleaseMappingToAssets") {
    from(layout.buildDirectory.file("outputs/mapping/release/mapping.txt"))
    into("src/main/assets")
}

// Same wiring pattern as ObfuscationPlugin.kt: mapping.txt isn't final until minify AND (if
// Compose) the compose-mapping merge step both ran. Other tasks across *every* variant (not just
// release — a plain `./gradlew build` also runs mergeDebugAssets/lintAnalyzeDebug/etc.) also read
// src/main/assets — mustRunAfter tells Gradle the ordering is intentional, and as a side effect
// makes this single-pass: those readers now run after the fresh mapping.txt is copied in,
// instead of picking it up one build later.
tasks.configureEach {
    val isMappingProducer = (name.startsWith("minify") && name.endsWith("WithR8")) ||
        (name.startsWith("merge") && name.endsWith("ComposeMapping"))
    if (isMappingProducer) {
        finalizedBy(copyReleaseMappingToAssets)
        copyReleaseMappingToAssets.get().mustRunAfter(this)
    }
    val readsAssetsBeforeCopy = (name.startsWith("merge") && name.endsWith("Assets")) ||
        name.contains("lint", ignoreCase = true)
    if (readsAssetsBeforeCopy) {
        mustRunAfter(copyReleaseMappingToAssets)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.11.0")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(project(":runtime"))
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(project(":testing"))
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Sample-app report format is switchable at build time, e.g.
//   ./gradlew :app:verifyObfuscation -Pobfuscation.reportFormat=JSON
val obfuscationReportFormat = (project.findProperty("obfuscation.reportFormat") as String?)
    ?.let(ReportFormat::valueOf)
    ?: ReportFormat.HTML

// Sample-app failOnViolation is switchable at build time, e.g.
//   ./gradlew :app:assembleRelease -Pobfuscation.failOnViolation=true
// to demo the CI security gate (build fails on CRITICAL findings).
val obfuscationFailOnViolation = (project.findProperty("obfuscation.failOnViolation") as String?)
    ?.toBooleanStrictOrNull()
    ?: false

obfuscationVerify {
    sensitivePackages.set(
        listOf(
            "com.rezacah.ngaburake.data.fixture.PaymentManager",
            "com.rezacah.ngaburake.data.fixture.ApiKeyStore",
            "com.rezacah.ngaburake.data.fixture.TokenStore",
            "com.rezacah.ngaburake.data.fixture.LegacyAuthManager",
        ),
    )
    failOnViolation.set(obfuscationFailOnViolation)
    reportFormat.set(obfuscationReportFormat)
    outputDir.set(layout.buildDirectory.dir("reports/obfuscation-sample"))
}