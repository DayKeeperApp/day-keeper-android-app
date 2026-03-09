# 9. ViewModel UiState Pattern

Date: 2026-03-08

## Status

Accepted

## Context

Each screen needs a consistent way to manage UI state, handle loading/error/success states, and
communicate between the ViewModel and Composable layers. Without a standard pattern, each screen
invents its own approach, leading to inconsistency and bugs.

## Decision

We adopt a **sealed interface UiState + StateFlow** pattern for all screens.

### UiState Definition

Each screen defines a sealed interface:

```kotlin
sealed interface CalendarUiState {
    data object Loading : CalendarUiState
    data class Success(
        val events: List<Event>,
        val selectedDate: LocalDate,
        val viewMode: ViewMode,
    ) : CalendarUiState
    data class Error(val message: String) : CalendarUiState
}
```

### ViewModel Exposure

ViewModels expose state as `StateFlow<UiState>`:

```kotlin
class CalendarViewModel(
    private val calendarRepository: CalendarRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CalendarUiState>(CalendarUiState.Loading)
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()
}
```

### Composable Consumption

Composables observe state with `collectAsStateWithLifecycle()`:

```kotlin
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (uiState) {
        is CalendarUiState.Loading -> LoadingIndicator()
        is CalendarUiState.Success -> CalendarContent(...)
        is CalendarUiState.Error -> ErrorView(...)
    }
}
```

### One-Shot Events

For navigation events, snackbar messages, and other one-shot side effects, ViewModels use
`Channel<UiEvent>` exposed as a `Flow`, consumed with `LaunchedEffect`.

## Consequences

- Every screen follows the same state management pattern — easy to learn and review.
- `StateFlow` survives configuration changes without additional boilerplate.
- `collectAsStateWithLifecycle()` automatically pauses collection when the lifecycle is inactive,
  preventing wasted work.
- The sealed interface ensures exhaustive `when` handling — the compiler catches missing states.
- One-shot events via `Channel` prevent the "event consumed twice" problem that `SharedFlow` can
  cause.
