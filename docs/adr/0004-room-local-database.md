# 4. Room for Local Database

Date: 2026-03-08

## Status

Accepted

## Context

The offline-first architecture (see [ADR-0002](0002-offline-first-architecture.md)) requires a
robust local database on-device. Options considered:

1. **Room** — Google's official SQLite abstraction layer for Android. Provides compile-time SQL
   verification, LiveData/Flow integration, and migration support.
2. **SQLDelight** — a multiplatform SQL library that generates Kotlin from SQL files. Strong type
   safety but smaller ecosystem on Android.
3. **Realm** — an object database with sync built in. Adds a proprietary runtime and is harder to
   self-host.
4. **Raw SQLite** — maximum control but no compile-time checks, manual cursor management, and
   significant boilerplate.

## Decision

We use **Room** as the local persistence layer.

Key factors:

- **Official Google library** — first-class support, regular updates, integrated into Android
  Jetpack.
- **Compile-time SQL verification** — catches query errors at build time via KSP annotation
  processing.
- **Kotlin Flow support** — reactive data observation fits naturally with Compose's state model.
- **Migration framework** — structured schema migrations for evolving the local database alongside
  server changes.
- **Large community** — extensive documentation, tutorials, and Stack Overflow coverage.

## Consequences

- Entities are annotated Kotlin data classes (`@Entity`, `@PrimaryKey`).
- DAOs are interfaces with annotated query methods (`@Query`, `@Insert`, etc.).
- We use KSP (not KAPT) for annotation processing, aligning with modern Kotlin tooling.
- Schema migrations must be written and tested when the database evolves.
- The Room schema is a subset of the server's PostgreSQL schema — field naming follows the same
  conventions for consistency.
