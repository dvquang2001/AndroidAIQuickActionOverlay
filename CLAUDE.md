# CLAUDE.md

## Project Rules
Android app using:
- MVI
- Clean Architecture
- Kotlin Coroutines
- Flow / StateFlow / SharedFlow
- AccessibilityService

Claude must preserve these patterns in all generated or refactored code.

## Architecture
- Presentation: ViewModel, UiState, Intent, Effect, UI models
- Domain: UseCase, domain models, repository interfaces
- Data: repository implementations, data sources, DTO/entity, mappers

Rules:
- UI must not contain business logic
- Domain must not depend on Android framework
- Data must not leak DTO/framework models into domain/presentation
- Prefer use cases over direct repository access from ViewModel

## MVI
- One immutable `UiState` per screen
- Intents modeled explicitly with sealed class/interface
- One-off events use `SharedFlow`/effects, not persistent state
- State updates via reducer-style `copy`

## Coroutines
- Never use `GlobalScope`
- Use structured concurrency
- Use `viewModelScope` in ViewModel
- Inject dispatchers when possible
- Handle cancellation and errors explicitly

## Flow
- Expose `StateFlow` for UI state
- Expose `SharedFlow` for effects
- Keep mutable flows private
- Avoid nested `collect` when operators can solve it
- Use `stateIn/shareIn` deliberately

## AccessibilityService
- Keep service lightweight
- Filter noisy events aggressively
- Separate event handling, node parsing, and action execution
- Avoid heavy work on main thread
- Null-check all node operations
- Avoid brittle hardcoded traversal unless documented
- Be careful with privacy and logging

## Code Style
- Write idiomatic Kotlin
- Prefer immutable data classes
- Prefer sealed interfaces/classes for state modeling
- Use meaningful names
- Keep functions/classes focused
- Avoid giant ViewModels and god classes
- Prefer constructor injection

## Testing
Prioritize testability for:
- use cases
- reducers
- mappers
- accessibility parsing/filtering
- repository logic

## Avoid
- GlobalScope
- mutable public state
- business logic in Fragment/Activity/Service/UI
- Android dependencies in domain
- direct data source calls from presentation
- unsafe accessibility assumptions
- over-engineered base classes
- generic util/helper dumping grounds

## Behavior
When generating code:
1. Identify the correct layer
2. Preserve architecture boundaries
3. Prefer the simplest correct implementation
4. Produce production-ready Kotlin
5. Keep accessibility logic safe and performant