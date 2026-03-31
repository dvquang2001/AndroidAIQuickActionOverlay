# AI Quick Action Overlay

Floating AI assistant cho Android — bôi text bất kỳ trong mọi app → dịch / tóm tắt / giải thích bằng Gemini AI.

---

## Stack

| Layer | Tech |
|---|---|
| UI | Jetpack Compose + Material3 |
| Architecture | MVI (BaseViewModel + UiState/UiIntent/UiEffect) |
| DI | Hilt |
| DB | Room + Flow |
| AI | Gemini 1.5 Flash (streaming) |
| Overlay | WindowManager + ComposeView |
| Accessibility | AccessibilityService |
| Async | Coroutines + Flow |

---

## Cấu trúc project

```
├── data/
│   ├── ai/GeminiClient.kt          # Gemini API wrapper (streaming)
│   ├── local/                       # Room: Entity, DAO, Database
│   └── repository/HistoryRepository.kt
├── domain/
│   ├── model/OverlayAction.kt       # ActionType enum + ProcessResult sealed
│   └── usecase/ProcessTextUseCase.kt
├── service/
│   └── AIAccessibilityService.kt    # Bắt text selection event
├── overlay/
│   └── OverlayManager.kt            # WindowManager + ComposeView lifecycle
├── ui/
│   ├── base/                        # MVI: BaseViewModel, UiState, UiIntent, UiEffect
│   ├── main/MainScreen.kt           # Permission setup + history list
│   ├── overlay/OverlayScreen.kt     # Floating panel UI
│   └── theme/                       # Material3 theme
└── di/AppModule.kt                  # Hilt providers
```

---

## Flow hoạt động

```
User bôi text
    → AccessibilityService nhận TYPE_VIEW_TEXT_SELECTION_CHANGED
    → OverlayManager.showFloatingButton(text)
    → User nhấn "AI ✨"
    → OverlayManager.showOverlayPanel(text)
    → User chọn action (Dịch / Tóm tắt / ...)
    → OverlayViewModel.onIntent(RunAction)
    → ProcessTextUseCase invoke()
    → GeminiClient.streamResponse() [Flow<String>]
    → UI cập nhật realtime từng token
    → Lưu vào Room khi hoàn thành
```

---

## Setup

### 1. Gemini API Key
Xem `API_KEY_SETUP.md`

### 2. Permissions cần cấp
- **Accessibility Service**: Settings → Accessibility → AI Quick Action → Bật
- **Display over other apps**: Settings → Apps → AI Overlay → Display over other apps → Bật

### 3. Chạy app
```bash
./gradlew assembleDebug
```

---

## Roadmap

- [ ] Phase 1: AccessibilityService + floating button ✅
- [ ] Phase 2: Gemini streaming + overlay panel ✅
- [ ] Phase 3: Room history ✅
- [ ] Phase 4: Custom prompt templates
- [ ] Phase 5: Gemini Nano on-device fallback
- [ ] Phase 6: Insert result back vào app (paste automation)
- [ ] Phase 7: Quick settings tile