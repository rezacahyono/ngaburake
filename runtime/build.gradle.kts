import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.vanniktech.maven.publish)
}

group = "com.rezacah.ngaburake"
version = "0.1.1"

android {
    namespace = "com.rezacah.ngaburake.runtime"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    // api, not implementation: ObfuscationResult.findings (List<Finding>) and
    // ObfuscationSDK.generateReport(format: ReportFormat) expose report's types through this
    // module's public API — consumers need compile-time access to them.
    api("com.rezacah.ngaburake:report:0.1.0")
    // implementation is correct here: MappingIndex/MappingParser never leak past the private
    // ObfuscationSDK constructor — Builder.withMappingFile() only takes a plain java.io.File.
    implementation("com.rezacah.ngaburake:mapping:0.1.0")
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}

// Maven Central publishing config — see docs/PUBLISHING.md "Publishing to Maven Central" for
// the full account/GPG/namespace setup this depends on. Publish report + mapping first — this
// module's POM references both by coordinate, and a fresh version isn't resolvable until they
// exist on Central too.
mavenPublishing {
    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
            sourcesJar = SourcesJar.Sources(),
            javadocJar = JavadocJar.Empty(),
        ),
    )
    publishToMavenCentral()
    signAllPublications()

    coordinates("com.rezacah.ngaburake", "runtime", version.toString())

    pom {
        name.set("ngaburake runtime")
        description.set(
            "Runtime SDK (ObfuscationSDK) that verifies obfuscation in a running app via " +
                "reflection and an optional mapping-file cross-check.",
        )
        url.set("https://github.com/rezacahyono/ngaburake")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("rezacahyono")
                name.set("reza cahyono")
                url.set("https://github.com/rezacahyono/")
            }
        }
        scm {
            url.set("https://github.com/rezacahyono/ngaburake/")
            connection.set("scm:git:git://github.com/rezacahyono/ngaburake.git")
            developerConnection.set("scm:git:ssh://git@github.com/rezacahyono/ngaburake.git")
        }
    }
}
