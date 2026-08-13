# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [runtime 0.1.2 / testing 0.1.1 / gradle-plugin 0.1.1 / mapping 0.1.1] - 2026-08-13

### Added
- `ObfuscationSDK.Builder.addSensitivePackages(List<String>)` (`runtime`) — add multiple sensitive
  classes at once instead of calling `addSensitivePackage` one at a time. Freely mixable with
  `addSensitivePackage`.
- Package-prefix wildcard support: a `"com.example.payment.*"`-style entry (ending in `.*`) is
  expanded into every class under that package found in the mapping file — available in both the
  Gradle plugin (`obfuscationVerify { sensitivePackages }`) and the runtime SDK (`runtime`,
  requires `withMappingFile()`). A wildcard that matches no class produces a `WARNING` finding
  instead of being silently dropped.
- `MappingIndex.classesUnder(packagePrefix)` (`mapping`) — the primitive wildcard expansion is
  built on; boundary-aware (`"com.foo.payment"` doesn't match `"com.foo.paymentXyz"`).
- `FakeObfuscationChecker(type: FindingType = FindingType.CLASS_NAME)` (`testing`) — simulate a
  `FIELD_NAME`/`METHOD_NAME` finding instead of always `CLASS_NAME`.

### Changed
- `ObfuscationSDK.Builder.build()` (`runtime`) now deduplicates sensitive packages — adding the
  same class twice no longer produces duplicate findings.
- `MappingParser` (`mapping`) uses `split(limit = 2)` instead of an unbounded split, removing a
  theoretical silent-drop case for a mapping line with more than one `->`.

### Fixed
- `ObfuscationRepositoryImpl` (sample app, not published) now catches `IOException` specifically
  (with a log) instead of a generic `Exception` when reading the bundled `mapping.txt` asset.

## [0.1.1] - 2026-08-12

### Added
- `ObfuscationSDK.Builder.withSensitiveKeywords(List<String>)` — override the keywords
  `ReflectionChecker` flags in field/method names instead of the defaults (`apiKey`, `secret`,
  `token`, `password`).

### Changed
- `ReflectionChecker.DEFAULT_KEYWORDS` is now `internal` — the keyword list is configured solely
  through the builder API.

## [0.1.0] - 2026-08-10

### Added
- Runtime SDK (`ObfuscationSDK`) — verifies obfuscation in a running app via reflection
  (`ClassNameChecker`, `ReflectionChecker`) with an optional `withMappingFile()` cross-check
  against real R8 output.
- Gradle plugin `com.rezacah.ngaburake.obfuscation-verify` — `verifyObfuscation` task that
  parses R8 `mapping.txt`, checks declared sensitive classes, and fails the build via
  `failOnViolation`.
- Report generation (`Console`/`JSON`/`HTML`) via the shared report module.
- Consumer testing fake (`FakeObfuscationChecker`) for unit testing code that calls into the SDK.
