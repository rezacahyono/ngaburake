# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
