# 10. Code Coverage with Jacoco

Date: 2026-03-15

## Status

Accepted

## Context

The project has comprehensive testing infrastructure (JUnit 5, MockK, Kotest, Turbine, Robolectric)
but no way to measure how much production code is exercised by tests. Without coverage metrics, it
is difficult to identify untested code paths or enforce quality standards in CI.

Two main options were considered:

- **Jacoco**: Built into Gradle, mature, widely used in the Android ecosystem. Works with both
  Android and pure JVM modules.
- **Kover**: JetBrains' Kotlin-specific coverage tool. Better Kotlin support but adds an external
  dependency and has less ecosystem maturity.

## Decision

Use **Jacoco** configured via a convention plugin (`daykeeper.jacoco`) applied to all modules.

Key design choices:

- **Convention plugin**: `JacocoConventionPlugin` applies the `jacoco` Gradle plugin, sets the tool
  version from the version catalog, and configures per-module reports with standard exclusions.
- **Merged report**: A root-level `jacocoMergedReport` task aggregates coverage data from all
  subprojects into a single HTML/XML/CSV report.
- **Threshold**: 80% minimum instruction coverage, enforced by `jacocoCoverageVerification` in CI.
  Configurable via `jacoco.minimumCoverage` Gradle property.
- **Exclusions**: Android generated code (`R`, `BuildConfig`, `Manifest`), Compose generated code
  (`ComposableSingletons`), and Room generated code (`*_Impl`) are excluded from coverage metrics.
- **Badge**: Coverage percentage displayed in README via shields.io dynamic badge backed by a GitHub
  Gist, updated on pushes to `main`.

## Consequences

- All modules automatically get coverage reporting via convention plugins.
- CI enforces the 80% threshold — PRs that drop below will fail.
- The threshold can be ratcheted upward as coverage improves.
- Developers can run `task coverage` locally to generate reports and `task coverage:report` to view
  them in a browser.
- Compose-heavy modules may need additional exclusion patterns as the UI layer grows.
