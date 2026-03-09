# 7. Koin for Dependency Injection

Date: 2026-03-08

## Status

Accepted

## Context

The app needs a dependency injection framework to manage object graphs across multiple modules (app,
core/database, core/data, core/network, feature/\*). The main candidates are:

- **Hilt/Dagger** — Compile-time DI with annotation processing. Industry standard for Android but
  adds KSP/KAPT build time and requires Android framework coupling in module setup.
- **Koin** — Runtime DI with a Kotlin DSL. Lightweight, no code generation, multiplatform-ready.

## Decision

We use **Koin 4.x** for dependency injection.

### Module Organization

Each Gradle module defines its own Koin module:

- `core/database` → `databaseModule` (Room DB singleton + all DAOs)
- `core/data` → `dataModule` (repository interface-to-implementation bindings)
- `core/network` → `networkModule` (Ktor HttpClient, API services, TokenStore)
- `feature/*` → `*Module` (ViewModels for each feature)
- `app` → `appModule` (aggregates all modules)

### Wiring

`DayKeeperApplication.onCreate()` calls:

```kotlin
startKoin {
    androidContext(this@DayKeeperApplication)
    modules(appModule)
}
```

ViewModels are injected in Composables via `koinViewModel()`.

## Consequences

- No annotation processing — faster builds, especially in a multi-module project.
- Kotlin-native DSL feels natural and is easy to read.
- Runtime resolution means DI errors surface at runtime, not compile time. Mitigated by Koin's
  `checkModules()` test utility.
- Koin is multiplatform-ready if we ever target iOS via KMP.
- Each module is self-contained — adding a new feature module just means defining a new Koin module
  and adding it to the aggregator.
