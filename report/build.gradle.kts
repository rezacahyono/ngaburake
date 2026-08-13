plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.vanniktech.maven.publish)
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
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
}

// Maven Central publishing config — see docs/PUBLISHING.md "Publishing to Maven Central" for
// the full account/GPG/namespace setup this depends on.
mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("com.rezacah.ngaburake", "report", version.toString())

    pom {
        name.set("ngaburake report")
        description.set(
            "Report generation model and formatters (Console/JSON/HTML) for the ngaburake " +
                "obfuscation verification SDK.",
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
