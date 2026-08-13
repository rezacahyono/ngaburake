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
    // api, not implementation: FakeObfuscationChecker implements runtime's ObfuscationChecker,
    // so that type (and report's Finding/Severity it exposes) leaks through this module's public
    // API — consumers need compile-time access to both.
    // project() dependency: resolvable locally before 0.1.2 is on Central; the published POM gets
    // the Maven coordinates (com.rezacah.ngaburake:runtime:0.1.2) automatically via the
    // vanniktech plugin's project-dependency conversion.
    api(project(":runtime"))
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}

// Maven Central publishing config — see docs/PUBLISHING.md "Publishing to Maven Central" for
// the full account/GPG/namespace setup this depends on. Publish runtime first — this module's
// POM references runtime:0.1.2 by coordinate, and it isn't resolvable until it exists on Central.
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

    coordinates("com.rezacah.ngaburake", "testing", version.toString())

    pom {
        name.set("ngaburake testing")
        description.set(
            "Deterministic FakeObfuscationChecker for unit testing code that calls into the " +
                "ngaburake runtime SDK.",
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
