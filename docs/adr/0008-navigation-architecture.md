# 8. Navigation Architecture

Date: 2026-03-08

## Status

Accepted

## Context

The app has ~30 screens across 5 feature modules plus cross-cutting screens (global search). We need
a navigation architecture that supports:

- Type-safe route arguments (entity IDs)
- Nested navigation graphs per feature module
- Bottom navigation with back stack preservation per tab
- Feature modules that don't depend on each other

## Decision

We use **Jetpack Navigation Compose** with **type-safe routes** via Kotlin serialization.

### Route Definitions

Each feature module defines its routes as `@Serializable` data classes/objects:

```kotlin
// In feature/calendar
@Serializable object CalendarRoute
@Serializable data class EventDetailRoute(val eventId: String)
@Serializable data class EventEditRoute(val eventId: String? = null) // null = create
```

### Navigation Graph Pattern

Each feature module exposes a `NavGraphBuilder` extension function:

```kotlin
fun NavGraphBuilder.calendarGraph(navController: NavHostController) {
    navigation<CalendarRoute>(startDestination = CalendarListRoute) {
        composable<CalendarListRoute> { ... }
        composable<EventDetailRoute> { ... }
    }
}
```

The `app` module's `DayKeeperNavHost` calls each feature's graph builder.

### Bottom Navigation

Five tabs: Calendar, People, Tasks, Lists, Profile. Each tab hosts its own nested graph. Back stack
is preserved per tab using Navigation's `saveState`/`restoreState`.

### Bottom Sheets

Configuration screens (recurrence picker, reminder editor, item edit) use `ModalBottomSheet`
composables, not navigation destinations. This keeps them lightweight and avoids back stack
complexity.

## Consequences

- Type-safe routes eliminate string-based route matching and argument parsing errors.
- Feature modules are decoupled — they expose graph builders but don't import each other.
- Bottom navigation preserves state per tab, matching user expectations.
- Kotlin serialization adds a dependency but is already needed for network DTOs.
- Create and edit screens share a single route with an optional nullable ID parameter.
