# 5. Multi-Module Project Architecture

Date: 2026-03-08

## Status

Accepted

## Context

As Day Keeper grows to cover calendars, tasks, lists, contacts, sync, and notifications, a
single-module project would become difficult to maintain:

- Build times increase as the entire project recompiles for any change.
- Feature boundaries blur, leading to tight coupling.
- Testing becomes harder when everything depends on everything else.
- New contributors struggle to understand the project structure.

## Decision

We organize the project into **multiple Gradle modules** with clear responsibilities:

```text
:app                  → Android application shell (entry point, navigation, DI)
:core:model           → Pure Kotlin data models (no Android dependencies)
:core:common          → Shared Android utilities
:core:ui              → Shared Compose UI components and theming
:feature:<name>       → Feature modules (one per domain, e.g., calendar, tasks)
```

Convention plugins in `build-logic/` enforce consistent configuration:

| Plugin                          | Purpose                                |
| ------------------------------- | -------------------------------------- |
| `daykeeper.android.application` | App module setup (targetSdk, ProGuard) |
| `daykeeper.android.library`     | Library module setup (lint baselines)  |
| `daykeeper.android.feature`     | Feature module setup (Compose, nav)    |
| `daykeeper.kotlin.library`      | Pure Kotlin module setup (JVM target)  |
| `daykeeper.android.test`        | Test module configuration              |

## Consequences

- **Faster builds** — Gradle only recompiles changed modules and their dependents.
- **Enforced boundaries** — a feature module cannot accidentally depend on another feature module's
  internals.
- **Parallel development** — different features can evolve independently.
- **Convention plugins** reduce boilerplate — module `build.gradle.kts` files are small and
  consistent.
- Adding a new feature requires creating a module and applying the appropriate convention plugin.
- Dependency injection (Koin) must be configured to span module boundaries.
