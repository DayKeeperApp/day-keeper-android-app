# 3. Jetpack Compose for UI

Date: 2026-03-08

## Status

Accepted

## Context

Android offers two UI frameworks:

1. **XML Views** — the legacy system using XML layouts, fragments, and the View class hierarchy.
   Mature but verbose, with complex lifecycle management.
2. **Jetpack Compose** — a modern declarative UI toolkit built on Kotlin. Google's recommended
   approach for new Android projects since 2021.

Day Keeper is a greenfield project with no legacy XML layouts to maintain.

## Decision

We use **Jetpack Compose** as the sole UI framework. No XML layouts or fragments are used for screen
content.

Key factors:

- **Declarative model** — UI is a function of state, reducing bugs from inconsistent view/state
  synchronization.
- **Kotlin-native** — no XML/Kotlin split; everything is Kotlin, simplifying tooling and
  refactoring.
- **Less boilerplate** — no `findViewById`, view binding, or adapter patterns.
- **First-class Google support** — Compose receives the most investment and new APIs land here
  first.
- **Better testability** — Compose UI tests use semantic trees, making tests more stable than
  view-hierarchy-based tests.

## Consequences

- All UI code is written in Kotlin using `@Composable` functions.
- We depend on the Compose compiler plugin and the Compose BOM for versioning.
- Team members need Compose knowledge (not legacy View skills).
- Some third-party libraries may still require XML (e.g., `AndroidManifest.xml` configuration), but
  these are infrastructure, not UI.
- Preview tooling in Android Studio is used for rapid UI iteration.
