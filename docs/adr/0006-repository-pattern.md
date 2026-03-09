# 6. Repository Pattern with Three-Layer Data Separation

Date: 2026-03-08

## Status

Accepted

## Context

Day Keeper has three distinct data representations: domain models used by ViewModels and UI, Room
entities stored in SQLite, and network DTOs exchanged with the backend API. Without clear
boundaries, these concerns bleed into each other — Room annotations appear in UI code, serialization
logic couples to business logic, and schema changes ripple across the entire app.

## Decision

We adopt a **three-layer data separation** with a **repository abstraction**:

- **Domain models** (`core/model`) — Pure Kotlin data classes with no framework annotations. Used by
  ViewModels, use cases, and UI layers.
- **Room entities** (`core/database`) — Annotated with `@Entity`, `@PrimaryKey`, etc. Only used
  within the database module and repository implementations.
- **Network DTOs** (`core/network`) — Annotated with `@Serializable`. Only used within the network
  module and sync logic.

**Repository interfaces** live in `core/data` and expose:

- `Flow`-based queries for reactive data observation
- `suspend` functions for CRUD operations

**Repository implementations** in `core/data` delegate to Room DAOs and use explicit **mapper
functions** to convert between entities and domain models.

Each module defines its own Koin module for dependency injection.

## Consequences

- Domain models remain pure Kotlin — testable without Android framework dependencies.
- Room schema changes are isolated to `core/database` and its mappers.
- Network DTO changes are isolated to `core/network` and sync mappers.
- ViewModels depend only on repository interfaces, making them easy to test with fakes.
- The mapper layer adds some boilerplate but provides clear transformation points.
- `core/model` has zero dependencies, keeping it fast to compile and easy to share.
