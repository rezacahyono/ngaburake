# ngaburake

Code Obfuscation Verification & Reporting SDK for Android — verifies that ProGuard/R8
obfuscation is *actually effective* (sensitive classes are truly renamed), not just enabled.

## Status

- [x] Gradle plugin (`verifyObfuscation` task)
- [x] R8 `mapping.txt` parsing
- [x] Console / JSON report
- [x] Fail-on-violation build gate
- [ ] Runtime reflection check (in-app, CI/QA) — planned v1.1
- [ ] HTML report format — planned v1.1
- [ ] SARIF export (CI code scanning) — planned v1.2
- [ ] Historical trend dashboard — exploratory, not yet designed

## Installation

From the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.rezacah.ngaburake.obfuscation-verify)
(once published), in your module's `build.gradle.kts`:

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
| `reportFormat` | `ReportFormat.CONSOLE` | `CONSOLE` or `JSON`. `HTML` is not implemented yet (throws at task execution time). |
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

## A note on trivial classes

If a sensitive class is small enough (one constructor, one trivial method, one call site), R8
may inline or merge it away entirely so it never appears in the mapping file at all — even
without a `-keep` rule. If that happens, use:

```proguard
-keep,allowobfuscation class com.rezacah.ngaburake.YourSensitiveClass
```

This keeps the class as a distinct, checkable type while still letting R8 rename it.

## Development

- `./gradlew build` — build everything
- `./gradlew test` — unit tests (JVM)
- `./gradlew :gradle-plugin:test` — plugin unit + functional tests (Gradle TestKit)
- `./gradlew connectedAndroidTest` — instrumented tests, needs a running emulator/device
- `./gradlew assembleRelease` — exercises the ProGuard/R8 path

## License

[Apache License 2.0](LICENSE)
