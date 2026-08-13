# ngaburake

[![Maven Central](https://img.shields.io/maven-central/v/com.rezacah.ngaburake/runtime)](https://central.sonatype.com/artifact/com.rezacah.ngaburake/runtime)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fcom%2Frezacah%2Fngaburake%2Fobfuscation-verify%2Fcom.rezacah.ngaburake.obfuscation-verify.gradle.plugin%2Fmaven-metadata.xml&label=Gradle%20Plugin)](https://plugins.gradle.org/plugin/com.rezacah.ngaburake.obfuscation-verify)
[![License](https://img.shields.io/github/license/rezacahyono/ngaburake)](LICENSE)

Code Obfuscation Verification & Reporting SDK for Android — verifies that ProGuard/R8
obfuscation is *actually effective* (sensitive classes are truly renamed), not just enabled.

See the [Changelog](CHANGELOG.md) for release history and [Published packages](#published-packages)
for what's available where.

## Status

- [x] Gradle plugin (`verifyObfuscation` task)
- [x] R8 `mapping.txt` parsing
- [x] Console / JSON / HTML report
- [x] Fail-on-violation build gate
- [x] Runtime SDK (`ObfuscationSDK`) — class name + reflection field/method check
- [x] Consumer testing fake (`FakeObfuscationChecker`)
- [x] SARIF export (CI code scanning) — see [CI integration](#ci-integration)

## Use cases

**CI security gate on every release build.** Apply the Gradle plugin, list your sensitive
classes, set `failOnViolation.set(true)`. `verifyObfuscation` runs right after R8 and fails the
build the moment a class you expect renamed (e.g. `PaymentManager`, `ApiKeyStore`) still has its
original name — usually caused by an overly broad `-keep` rule someone added without noticing the
blast radius. See [Configuration](#configuration).

**Runtime self-check in a CI/QA build.** Some issues (reflection-based leaks, member names) only
show up against the actual running app. Embed `ObfuscationSDK.verify()` in a dedicated QA build
variant to double-check obfuscation on-device instead of trusting `mapping.txt` alone. See
[Runtime SDK](#runtime-sdk).

**Catching leaks that survive class renaming.** A class can be renamed and still leak — e.g.
`ApiKeyStore` gets renamed to `bb`, but a `-keepclassmembers` rule (added for reflection/Gson)
keeps `getApiKey()` unobfuscated. `ClassNameChecker` alone would report `OK`; `ReflectionChecker`
catches it as `CRITICAL METHOD_NAME`. Run both (the SDK does, by default) rather than relying on
class-name checks alone.

**Auditing legacy `-keep` rules without breaking the build.** Point `sensitivePackages` at classes
you suspect are covered by an old, overly broad `-keep` rule, but leave `failOnViolation` at its
default `false`. The report flags every violation without failing CI — audit first, then flip
`failOnViolation.set(true)` once the rules are cleaned up.

**Unit testing code that calls into the SDK.** Don't want real reflection running in every unit
test. Depend on `com.rezacah.ngaburake:testing` and use `FakeObfuscationChecker` to get
deterministic `ObfuscationResult`s for your own logic (e.g. "does my code correctly block login
when `result.isObfuscated` is false"), without needing a minified build to test against.

**Cross-checking a claim against real R8 output, not a guess.** The reflection heuristic alone
(class name length/shape) is a decent signal but not proof. Bundle the real `mapping.txt` as an
asset (see `copyReleaseMappingToAssets` in [Sample app](#sample-app)) and pass it via
`withMappingFile()` for an exact cross-check instead of a heuristic.

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
    implementation("com.rezacah.ngaburake:runtime:0.1.2")
}
```

`com.rezacah.ngaburake:report` is pulled in automatically as a transitive dependency — types like
`Finding` and `ReportFormat` are part of the runtime SDK's public API, so they need to be
available at compile time. `com.rezacah.ngaburake:mapping` is also pulled in transitively but
stays an implementation detail you never reference directly.

For consumer unit tests, add the fake checker:

```kotlin
testImplementation("com.rezacah.ngaburake:testing:0.1.1")
```

## Published packages

| Package | Version | Registry | Purpose |
|---|---|---|---|
| `com.rezacah.ngaburake.obfuscation-verify` | [0.1.0](https://plugins.gradle.org/plugin/com.rezacah.ngaburake.obfuscation-verify) | [Gradle Plugin Portal](https://plugins.gradle.org) | Build-time `verifyObfuscation` task |
| [`com.rezacah.ngaburake:runtime`](https://central.sonatype.com/artifact/com.rezacah.ngaburake/runtime) | 0.1.2 | Maven Central | In-app `ObfuscationSDK` |
| [`com.rezacah.ngaburake:report`](https://central.sonatype.com/artifact/com.rezacah.ngaburake/report) | 0.1.0 | Maven Central | Console/JSON/HTML/SARIF report generation |
| [`com.rezacah.ngaburake:mapping`](https://central.sonatype.com/artifact/com.rezacah.ngaburake/mapping) | 0.1.1 | Maven Central | R8 `mapping.txt` parser, package-prefix lookup |
| [`com.rezacah.ngaburake:testing`](https://central.sonatype.com/artifact/com.rezacah.ngaburake/testing) | 0.1.1 | Maven Central | `FakeObfuscationChecker` test fake |

`report`, `mapping`, and `testing` come in transitively through `runtime` (or are used directly
when needed). Latest versions are always shown by the badges at the top of this file.

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
| `sensitivePackages` | `[]` | Fully qualified class names that must appear renamed in the R8 mapping file. A `"com.example.payment.*"`-style entry (ending in `.*`) is a package-prefix wildcard, expanded into every class under that package found in the mapping file — see [wildcards](#package-prefix-wildcards). |
| `failOnViolation` | `false` | Fail the build when a class was found in the mapping but kept its original name. |
| `reportFormat` | `ReportFormat.CONSOLE` | `CONSOLE`, `JSON`, `HTML`, or `SARIF`. |
| `outputDir` | `build/reports/obfuscation` | Directory where the report file is written. |

## Package-prefix wildcards

Listing every sensitive class one at a time gets unwieldy for a package with many of them. An
entry ending in `.*` is treated as a package-prefix wildcard instead of a literal class name —
safe to detect this way because `*` is never a valid identifier character, so no real class name
can end with it:

```kotlin
obfuscationVerify {
    sensitivePackages.set(listOf("com.rezacah.ngaburake.payment.*"))
}
```

This expands, at `verifyObfuscation` time, into every class under `com.rezacah.ngaburake.payment`
that appears in `mapping.txt` (boundary-aware — it won't also match a sibling package like
`com.rezacah.ngaburake.paymentLegacy`). If the wildcard matches nothing, that's reported as a
single `WARNING` finding for the pattern itself, not silently ignored.

The runtime SDK (`ObfuscationSDK.Builder`) supports the same `.*` convention, but **requires**
`withMappingFile()` — expanding a wildcard means enumerating classes from a real mapping file;
the reflection-only heuristic has no way to do that. Calling `build()` with a wildcard entry and
no mapping file throws `IllegalArgumentException`.

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

SARIF format (`reportFormat.set(ReportFormat.SARIF)`) emits a
[SARIF 2.1.0](https://docs.oasis-open.org/sarif/sarif/v2.1.0/sarif-v2.1.0.html) log for upload to
GitHub/GitLab code scanning — see [CI integration](#ci-integration). Every severity is included
(`Severity.OK` maps to SARIF level `note`, not excluded), so the log reflects the complete set of
what was checked, not just violations. When a sensitive class's source file can be found under a
conventional `src/main/kotlin`/`src/main/java` root, the finding gets a `physicalLocation`
pointing at that file (line number is a best-effort `1`, not derived from bytecode debug info —
treat it as "somewhere in this file"); otherwise it falls back to a `logicalLocation` keyed by the
fully qualified class name.

## CI integration

`.github/workflows/obfuscation-check.yml` runs on every pull request: builds the sample app,
generates a SARIF report, and uploads it to GitHub code scanning via
`github/codeql-action/upload-sarif`. Findings then show up natively in the repo's **Security**
tab — including history across commits/PRs (new findings, resolved findings) without ngaburake
needing to build a dashboard for it.

```yaml
- name: Verify obfuscation (SARIF)
  run: ./gradlew :app:assembleRelease -Pobfuscation.reportFormat=SARIF

- name: Upload SARIF to code scanning
  if: always()
  uses: github/codeql-action/upload-sarif@v3
  with:
    sarif_file: app/build/reports/obfuscation-sample/report.sarif
```

`permissions: security-events: write` is required for `upload-sarif` to write to the Security tab.

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
            .addSensitivePackages(listOf("com.myapp.PaymentManager", "com.myapp.ApiKeyStore"))
            // Optional: cross-check against a real mapping.txt if you bundle one
            // (e.g. as an asset in a dedicated CI/QA build variant). Required if any
            // sensitivePackage uses the "com.myapp.payment.*" wildcard convention.
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
- `mapping.txt` is a build artifact, not something Gradle bundles into the APK by default — you're
  responsible for copying it into `assets/` yourself. See the `:app` module's
  `copyReleaseMappingToAssets` task for a working example: a `Copy` task ordered via
  `mustRunAfter`/`finalizedBy` to run strictly after `minify*WithR8`/`merge*ComposeMapping` and
  strictly before `mergeReleaseAssets`, so the same build that produces `mapping.txt` is the one
  that embeds it — no stale-by-one-build lag.
- For unit testing your own code that calls into the SDK, use `FakeObfuscationChecker` from
  `com.rezacah.ngaburake:testing` — it returns deterministic results without real reflection.

## Sample app

The `:app` module doubles as a live sample that demonstrates the whole SDK:

**Build-time (Gradle plugin)**
- `./gradlew :app:assembleRelease` runs `verifyObfuscation` automatically after R8. The report
  lands in `app/build/reports/obfuscation-sample/`.
- Switch report format: `-Pobfuscation.reportFormat={CONSOLE|JSON|HTML|SARIF}` (default `HTML`).
- Demo the CI security gate: `-Pobfuscation.failOnViolation=true` fails the build because
  `LegacyAuthManager` is deliberately kept by a broad `-keep` rule (default is `false`, so the
  build stays green while the report still flags it).

**Runtime (`ObfuscationSDK`) — the app's own screen**
- Runs `ObfuscationSDK.verify()` on demand (no startup cost), against the sample's fixture
  classes: `PaymentManager` (obfuscated → OK), `ApiKeyStore` (member `getApiKey` leaks →
  CRITICAL, METHOD_NAME), `TokenStore` (field `secretToken` leaks → CRITICAL, FIELD_NAME),
  `LegacyAuthManager` (kept name → CRITICAL, CLASS_NAME), and a typo class (`DoesNotExist` →
  WARNING).
- Toggle **mapping cross-check** — real R8 `mapping.txt` is copied into
  `app/src/main/assets/mapping.txt` by the `copyReleaseMappingToAssets` task (ordered strictly
  after minify/compose-mapping, strictly before asset merge) and passed via `withMappingFile()` —
  current after a single `./gradlew :app:assembleRelease`.
- Toggle **custom keywords** — `withSensitiveKeywords(listOf("legacy"))` replaces the defaults.
- Add/remove sensitive classes at runtime (shows `addSensitivePackage()` is dynamic).
- Render the report in **all four formats** (Console / JSON / HTML / SARIF) via `generateReport()`.

## Development

- `./gradlew build` — build everything
- `./gradlew test` — unit tests (JVM)
- `./gradlew :gradle-plugin:test` — plugin unit + functional tests (Gradle TestKit)
- `./gradlew :runtime:test :mapping:test :report:test :testing:test` — SDK module unit tests
- `./gradlew :app:testDebugUnitTest` — sample app unit tests (ViewModel + `FakeObfuscationChecker`)
- `./gradlew connectedAndroidTest` — instrumented tests (incl. a real `ObfuscationSDK.verify()`
  on device), needs a running emulator/device
- `./gradlew assembleRelease` — exercises the ProGuard/R8 path

## License

[Apache License 2.0](LICENSE)
