# Day Keeper

[![CI](https://github.com/DayKeeperApp/day-keeper-android-app/actions/workflows/ci.yml/badge.svg)](https://github.com/DayKeeperApp/day-keeper-android-app/actions/workflows/ci.yml)
[![Emulator Tests](https://github.com/DayKeeperApp/day-keeper-android-app/actions/workflows/emulator-tests.yml/badge.svg)](https://github.com/DayKeeperApp/day-keeper-android-app/actions/workflows/emulator-tests.yml)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-API%2026+-34A853?logo=android&logoColor=white)
[![Conventional Commits](https://img.shields.io/badge/Conventional%20Commits-1.0.0-FE5196?logo=conventionalcommits&logoColor=white)](https://conventionalcommits.org)
[![Code Style: ktfmt](https://img.shields.io/badge/code%20style-ktfmt-7F52FF)](https://facebook.github.io/ktfmt/)
![Coverage](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/jsamuelsen11/COVERAGE_GIST_ID/raw/coverage.json)

A personal life-management Android app — calendars, tasks, lists, contacts — built offline-first
with a self-hosted backend. See [docs/day-keeper-plan.md](docs/day-keeper-plan.md) for the full
architecture plan.

## Architecture

```text
:app
├── :core:ui         Shared Compose components & theming
├── :core:common     Shared Android utilities
└── :core:model      Pure Kotlin data models (no Android deps)

:feature:<name>      Feature modules (calendar, tasks, etc.)
```

The app follows an **offline-first** architecture: all reads and writes hit the local Room database
first, with background sync to a self-hosted C# / ASP.NET Core backend via REST push/pull.

Key decisions are documented as
[Architecture Decision Records](docs/adr/0001-record-architecture-decisions.md) in `docs/adr/`.

## Prerequisites

| Tool           | Version          | Notes                           |
| -------------- | ---------------- | ------------------------------- |
| Java           | 11+              | Homebrew or mise                |
| Kotlin         | 2.2.10           | Via Gradle (managed by project) |
| Android SDK    | API 36 (compile) | `~/Android/Sdk`                 |
| Android Studio | Latest           | For IDE support and emulators   |
| [mise]         | Latest           | Runtime version manager         |
| [Lefthook]     | Latest           | Git hooks manager               |
| [Task]         | 3.x              | Task runner (`go-task/task`)    |

[mise]: https://mise.jdx.dev
[Lefthook]: https://github.com/evilmartians/lefthook
[Task]: https://taskfile.dev

## Getting Started

```bash
# Clone the repo
git clone https://github.com/DayKeeperApp/day-keeper-android-app.git
cd day-keeper-android-app

# Check prerequisites and install git hooks
task setup

# Build the debug APK
task build
```

## Available Commands

Run `task` to see all commands. Highlights:

| Command         | Description                           |
| --------------- | ------------------------------------- |
| `task build`    | Build debug APK                       |
| `task test`     | Run unit tests                        |
| `task format`   | Auto-format all code (Spotless/ktfmt) |
| `task lint`     | Run Detekt + Android Lint             |
| `task coverage` | Run tests with coverage report        |
| `task docs`     | Generate API docs (Dokka)             |
| `task ci`       | Full CI pipeline locally              |
| `task setup`    | Check tools & install git hooks       |
| `task release`  | Bump version, tag, and push           |

## Project Structure

```text
day-keeper-android-app/
├── app/                        Main application module
├── core/
│   ├── model/                  Data models (pure Kotlin)
│   ├── common/                 Shared Android utilities
│   └── ui/                     Shared Compose components
├── feature/                    Feature modules (future)
├── build-logic/
│   └── convention/             Gradle convention plugins
├── config/
│   └── detekt/                 Detekt rules
├── docs/
│   ├── adr/                    Architecture Decision Records
│   └── day-keeper-plan.md      Full architecture plan
├── .github/workflows/          CI/CD workflows
├── Taskfile.yml                Task runner config
├── lefthook.yml                Git hooks config
└── gradle/libs.versions.toml   Dependency version catalog
```

## Commit Conventions

This project uses [Conventional Commits](https://conventionalcommits.org). Commits are validated by
a Lefthook hook on `commit-msg`.

Format:

```text
<type>(<scope>): <description>
```

Common types: `feat`, `fix`, `docs`, `chore`, `refactor`, `test`, `ci`, `build`.

## Code Style

All Kotlin code is auto-formatted by **Spotless** using **ktfmt** (Google style).

```bash
# Format all files
task format

# Check formatting (CI mode, no modifications)
task format:check
```

Formatting is enforced by a Lefthook pre-commit hook — commits will be rejected if code is not
properly formatted.
