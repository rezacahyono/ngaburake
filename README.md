# ngaburake

Code Obfuscation Verification & Reporting SDK for Android — verifies that ProGuard/R8
obfuscation is *actually effective* (sensitive classes are truly renamed), not just enabled.

## Status

- [x] Gradle plugin (`verifyObfuscation` task)
- [x] R8 `mapping.txt` parsing
- [x] Console / JSON / HTML report
- [x] Fail-on-violation build gate
- [x] Runtime SDK (`ObfuscationSDK`) — class name + reflection field/method check
- [x] Consumer testing fake (`FakeObfuscationChecker`)
- [ ] SARIF export (CI code scanning) — planned v1.2
- [ ] Historical trend dashboard — exploratory, not yet designed

## Installation

### Gradle Plugin

From the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.rezacah.ngaburake.obfuscation-verify),
in your module's `build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application")
    id("com.rezacah.ngaburake.obfuscation-verify") version "0.1.0"
}
```

**Development / local build:** if you're working inside this repo (or building the plugin from
source without publishing), the plugin is already resolved from the local composite build
(`gradle-plugin/`), wired up via `includeBuild("gradle-plugin")` in the root
`settings.gradle.kts` — omit the `version` when applying it that way.

### Runtime SDK

From [Maven Central](https://central.sonatype.com/artifact/com.rezacah.ngaburake/runtime), in
your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.rezacah.ngaburake:runtime:0.1.0")
}
```

`com.rezacah.ngaburake:report` is pulled in automatically as a transitive dependency — types like
`Finding` and `ReportFormat` are part of the runtime SDK's public API, so they need to be
available at compile time. `com.rezacah.ngaburake:mapping` is also pulled in transitively but
stays an implementation detail you never reference directly.

For consumer unit tests, add the fake checker:

```kotlin
testImplementation("com.rezacah.ngaburake:testing:0.1.0")
```

## Configuration

```kotlin
obfuscationVerify {
    sensitivePackages.set(listOf(
        "com.rezacah.ngaburake.PaymentManager",
        "com.rezacah.ngaburake.ApiKeyStore",
    ))
    failOnViolation.set(true) // default: false (report only, doesn't fail the build)
}
```

| Property | Default | Description |
|---|---|---|
| `sensitivePackages` | `[]` | Fully qualified class names that must appear renamed in the R8 mapping file. |
| `failOnViolation` | `false` | Fail the build when a class was found in the mapping but kept its original name. |
| `reportFormat` | `ReportFormat.CONSOLE` | `CONSOLE`, `JSON`, or `HTML`. |
| `outputDir` | `build/reports/obfuscation` | Directory where the report file is written. |

## Usage

Run manually:

```
./gradlew verifyObfuscation
```

If an Android R8 minify task (`minify<Variant>WithR8`) exists in the project, `verifyObfuscation`
is automatically wired to run right after it — no extra configuration needed.

## Example output

Console format:

```
[PASS] com.rezacah.ngaburake.PaymentManager — renamed as expected
[FAIL] com.rezacah.ngaburake.ApiKeyStore — kept its original name — check for an overly broad -keep rule
```

JSON format (`reportFormat.set(ReportFormat.JSON)`):

```json
[
  {
    "target": "com.rezacah.ngaburake.PaymentManager",
    "type": "CLASS_NAME",
    "severity": "OK",
    "detail": "renamed as expected"
  },
  {
    "target": "com.rezacah.ngaburake.ApiKeyStore",
    "type": "CLASS_NAME",
    "severity": "CRITICAL",
    "detail": "kept its original name — check for an overly broad -keep rule"
  }
]
```

A class that isn't found in the mapping file at all (typo, or stripped entirely by R8) is
reported as `WARNING`, not `CRITICAL` — it's not verified, but it's not a confirmed violation
either.

HTML format (`reportFormat.set(ReportFormat.HTML)`) renders a standalone page with a color-coded
table — one row per finding, `ok`/`warning`/`critical` CSS classes for styling.

## A note on trivial classes

If a sensitive class is small enough (one constructor, one trivial method, one call site), R8
may inline or merge it away entirely so it never appears in the mapping file at all — even
without a `-keep` rule. If that happens, use:

```proguard
-keep,allowobfuscation class com.rezacah.ngaburake.YourSensitiveClass
```

This keeps the class as a distinct, checkable type while still letting R8 rename it.

## Runtime SDK

`ObfuscationSDK` runs the same kind of checks as the Gradle plugin, but in-app at runtime
(embedded in a CI/QA build, or invoked on demand) instead of at build time against `mapping.txt`.

```kotlin
import com.rezacah.ngaburake.runtime.ObfuscationSDK
import com.rezacah.ngaburake.report.ReportFormat

class MyQaBuildCheck {
    suspend fun runCheck() {
        val sdk = ObfuscationSDK.Builder()
            .addSensitivePackage("com.myapp.PaymentManager")
            .addSensitivePackage("com.myapp.ApiKeyStore")
            // Optional: cross-check against a real mapping.txt if you bundle one
            // (e.g. as an asset in a dedicated CI/QA build variant).
            // .withMappingFile(File(context.filesDir, "mapping.txt"))
            .build()

        val result = sdk.verify()
        val report = sdk.generateReport(result, ReportFormat.JSON)

        if (!result.isObfuscated) {
            // handle: log to crash reporting, fail a CI/QA gate, etc.
        }
    }
}
```

Two checkers run for every configured class:
- **`ClassNameChecker`** — reflection heuristic (`Class.forName(...).simpleName` length/shape), or
  an exact cross-check against a supplied mapping file if `withMappingFile(...)` was used.
- **`ReflectionChecker`** — scans declared fields/methods for sensitive keywords (`apiKey`,
  `secret`, `token`, `password` by default) that could leak information even if the class itself
  was renamed.

Notes:
- `verify()` is a `suspend fun` — call it from a coroutine scope, don't call it blockingly from
  `Dispatchers.Main`.
- `withMappingFile(file)` is optional. Without it, the SDK relies solely on the reflection
  heuristic — still useful, but less accurate than a real mapping-file cross-check.
- `mapping.txt` is a build artifact — it isn't bundled into the APK automatically. If you want to
  use `withMappingFile()`, you're responsible for packaging it yourself.
- For unit testing your own code that calls into the SDK, use `FakeObfuscationChecker` from
  `com.rezacah.ngaburake:testing` — it returns deterministic results without real reflection.

## Development

- `./gradlew build` — build everything
- `./gradlew test` — unit tests (JVM)
- `./gradlew :gradle-plugin:test` — plugin unit + functional tests (Gradle TestKit)
- `./gradlew :runtime:test :mapping:test :report:test :testing:test` — SDK module unit tests
- `./gradlew connectedAndroidTest` — instrumented tests, needs a running emulator/device
- `./gradlew assembleRelease` — exercises the ProGuard/R8 path

## License

[Apache License 2.0](LICENSE)
