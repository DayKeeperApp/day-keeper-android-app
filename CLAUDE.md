# Day Keeper Android App

## Project Overview

Offline-first life management Android app built with Kotlin and Jetpack Compose. Backend is C# /
ASP.NET Core (separate repository). Deployed on local Kubernetes (k3d) with Cloudflare Tunnel for
public access.

- Architecture: multi-module Gradle with convention plugins
- Design doc: `docs/day-keeper-plan.md`
- ADRs: `docs/adr/`

## Tech Stack

- **Language**: Kotlin 2.2.10
- **UI**: Jetpack Compose (BOM 2026.02.01), Material 3
- **Local DB**: Room 2.8.4
- **DI**: Koin 4.1.1
- **HTTP**: Ktor 3.4.1
- **Build**: Gradle 9.3.1, AGP 9.1.0
- **SDK**: compileSdk 36.1, minSdk 26, targetSdk 36, Java 11

## Key Commands

All commands use [Task](https://taskfile.dev) as the runner:

```shell
task build          # Build debug APK
task build:release  # Build release APK
task test           # Run unit tests
task lint           # Detekt + Android Lint
task format         # Auto-format (Spotless + Ktfmt)
task format:check   # Check formatting (CI mode)
task ci             # Full local CI: format:check → lint → test → build
task docs           # Generate API docs (Dokka)
task setup          # Check tools & install lefthook
task clean          # Clean build outputs
task deps           # Show dependency tree
task scan           # Gradle build scan (Develocity)
```

## Module Structure

```text
app/                    → Main application (com.jsamuelsen11.daykeeper.app)
core/model/             → Pure Kotlin data models (no Android dependencies)
core/common/            → Shared Android utilities
core/ui/                → Compose UI components and screens
feature/                → Feature modules (placeholder for future modules)
build-logic/convention/ → Gradle convention plugins
```

## Convention Plugins

Apply these in module `build.gradle.kts` files:

| Plugin ID                       | Use For                                                          |
| ------------------------------- | ---------------------------------------------------------------- |
| `daykeeper.android.application` | App module (linting, ProGuard)                                   |
| `daykeeper.android.library`     | Android library modules                                          |
| `daykeeper.android.feature`     | Feature modules (Compose + Navigation + Lifecycle)               |
| `daykeeper.kotlin.library`      | Pure Kotlin JVM modules                                          |
| `daykeeper.android.test`        | Test dependencies (JUnit 5, MockK, Kotest, Turbine, Robolectric) |

## Coding Standards

- **Kotlin style**: Google style via Ktfmt, enforced by Spotless
- **Commit format**: Conventional Commits, enforced by lefthook
  - Allowed types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`,
    `chore`, `revert`
- **No magic numbers**: define constants
- **Detekt**: zero-tolerance policy (`maxIssues: 0`)
- **Android Lint**: warnings treated as errors

## Testing

- **Unit tests**: JUnit 5 + MockK + Kotest assertions + Turbine (Flow testing)
- **Android tests**: Robolectric for unit-level Android tests
- **All new code must have tests**
- Tests run automatically on `git push` via lefthook

## Where to Find Things

| What                      | Where                       |
| ------------------------- | --------------------------- |
| Dependency versions       | `gradle/libs.versions.toml` |
| Convention plugins        | `build-logic/convention/`   |
| Detekt rules              | `config/detekt/detekt.yml`  |
| Git hook pipeline         | `lefthook.yml`              |
| Task runner commands      | `Taskfile.yml`              |
| Architecture decisions    | `docs/adr/`                 |
| Architecture & data model | `docs/day-keeper-plan.md`   |

## PR Workflow

- All changes go through pull requests
- CI must pass before merge
- **Pre-commit hooks** (lefthook): format → lint → security scan (gitleaks)
- **Pre-push hooks** (lefthook): unit tests + Android lint
