# Buddie 🐾 — Floating Companion & Timetable Deadline Reminder

**Buddie** is an intelligent, kawaii floating companion app for Android designed to keep students on top of their assignments, class timetables, and exams. At its center is **Buddie**, a custom vector-rendered kawaii mochi mascot that floats above other apps as an interactive overlay bubble, dynamically reflecting deadline urgency through expressive real-time mood changes.

---

## 📸 Overview & Highlights

- **Floating Overlay Bubble (v2)**: Lives on your screen across any app using `WindowManager` (`TYPE_APPLICATION_OVERLAY`), draggable with physics-based edge snapping (left or right dock).
- **Dynamic Mood Engine**: Buddie's mood automatically adapts based on the single closest deadline across submissions and exams:
  - 🟢 **Calm**: Everything under control. Shiny catchlight eyes, soft pastel pink blush, and content smile.
  - 🟡 **Aware**: Due in 2–3 days. Curious dot eyes, round "o" mouth.
  - 🟠 **Urgent**: Due within 24 hours. Anxious furrowed brow, inverted-V mouth, deep pink cheeks.
  - 🔴 **Overdue**: Past deadline! Drooping eyes, cartoon teardrops, and wobbly frown.
- **Tap-To-Expand Card**: Tapping the collapsed bubble expands into a full overlay card without leaving your current app. Reuses live Room-backed ViewModels with instant task completion and deletion.
- **Two Interchangeable Visual Shells**:
  - 💬 **Speech Bubble Style**: Mascot sits outside the card with a comic-style speech bubble tail pointer and full-width tab navigation.
  - 👤 **Companion Header Style**: Mascot perches directly across the card's top edge, featuring an integrated header and urgency notification badge dots on tabs.
- **Robust Background Survivability**:
  - Doze mode & OEM task-killer protection (Xiaomi, OnePlus, Samsung).
  - Dismissible battery optimization exemption prompt in Settings linking directly to `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`.
  - `BootReceiver` restoring the floating bubble automatically upon device reboot.
- **Timetable-Aware Assignments & Exams**:
  - Weekly class schedule grid (Mon–Sun).
  - Automatic classroom routing (resolves lecture hall vs. practical lab rooms per subject).
  - Countdown exam tracker with seat numbers, syllabus scope, and allowed materials.

---

## 🏛️ Architecture & Tech Stack

Buddie follows modern Android architecture guidelines (Clean Architecture + MVVM) with 100% Kotlin and Jetpack Compose:

```
Buddie Architecture
├── Presentation Layer (Jetpack Compose + Material 3)
│   ├── Mascot Rendering Engine (Custom Canvas vector math)
│   ├── Floating Overlay Shells (SpeechBubbleShell & CompanionHeaderShell)
│   ├── App Screens (Onboarding, Home, Timetable, Exams, Settings)
│   └── Theme & Design System (Charcoal palette, Status colors, Typography)
├── Service & Background Layer
│   ├── BubbleService (LifecycleOwner + ViewModelStoreOwner + WindowManager)
│   ├── BootReceiver (BroadcastReceiver with EntryPointAccessors)
│   └── ReminderWorker (WorkManager advance deadline notification scheduler)
├── Domain Layer
│   ├── Use Cases (GetMascotMoodUseCase)
│   └── Repository Interfaces (Subject, Timetable, Submission, Exam)
└── Data Layer
    ├── Room Database (AppDatabase, DAOs, Entity models, TypeConverters)
    └── Preferences DataStore (UserPreferences for card styles, dock pos, battery prefs)
```

### Core Technologies
- **UI Framework**: Jetpack Compose with Material 3 Design Tokens
- **Language**: Kotlin 1.9 + Java 21 Desugaring
- **Dependency Injection**: Dagger Hilt (with `EntryPointAccessors` for receivers)
- **Local Database**: Room DB (SQLite) with reactive Kotlin Coroutines `Flow`
- **Settings & State Persistence**: AndroidX Preferences DataStore
- **Background Work**: WorkManager & Foreground Services (`specialUse`)
- **Graphics & Motion**: Compose `Canvas` path rendering with animated state transitions

---

## 📂 Project Structure

```
d:\Buddie\
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/tanmay/buddie/
│   │   │   │   ├── BuddieApp.kt                        # Application class with @HiltAndroidApp
│   │   │   │   ├── MainActivity.kt                     # Single Activity host with NavHost
│   │   │   │   ├── data/
│   │   │   │   │   ├── datastore/                      # DataStore UserPreferences
│   │   │   │   │   ├── local/                          # Room Database, DAOs, TypeConverters
│   │   │   │   │   │   ├── dao/                        # SubjectDao, SubmissionDao, ExamDao, etc.
│   │   │   │   │   │   └── entity/                     # Room Entities (Subject, TimetableSlot, etc.)
│   │   │   │   │   └── repository/                     # Repository Implementations
│   │   │   │   ├── domain/
│   │   │   │   │   ├── repository/                     # Repository Interfaces
│   │   │   │   │   └── usecase/                        # GetMascotMoodUseCase
│   │   │   │   ├── presentation/
│   │   │   │   │   ├── components/                     # Skeletons, EmptyStateView, etc.
│   │   │   │   │   │   └── mascot/                     # BuddieMascot Canvas composable
│   │   │   │   │   │       └── card/                   # MascotCardContainer, Shells & OverlaySections
│   │   │   │   │   ├── screens/                        # Onboarding, Home, Timetable, Exams, Settings
│   │   │   │   │   ├── theme/                          # Color tokens, Typography, Shapes
│   │   │   │   │   └── util/                           # Haptics, Formatters
│   │   │   │   ├── receiver/                           # BootReceiver for auto-restart on reboot
│   │   │   │   └── service/                            # BubbleService (Foreground Overlay Service)
│   │   │   └── res/                                    # Drawables, mipmaps, strings, xml rules
│   │   └── test/                                       # JVM Unit Tests
│   └── build.gradle.kts
├── docs/                                               # Specification & design guides
├── build.gradle.kts                                    # Root Gradle config
└── settings.gradle.kts                                 # Project settings
```

---

## 🎨 Mascot Mood Calculation Logic

The mascot mood is determined reactively by [`GetMascotMoodUseCase`](file:///d:/Buddie/app/src/main/java/com/tanmay/buddie/domain/usecase/GetMascotMoodUseCase.kt) evaluating all pending assignments and upcoming exams:

$$\text{Nearest Delta} = \min(\text{Deadline} - \text{Now})$$

| Condition | Mascot Mood | Visual Traits |
|---|:---:|---|
| Any item past deadline & not marked done | **OVERDUE** | Drooping eyes, blue teardrops, wavy frown |
| Any item due within 24 hours ($\le 24\text{h}$) | **URGENT** | Furrowed brow, inverted-V mouth, hot pink blush |
| Any item due in 24h to 72 hours ($\le 72\text{h}$) | **AWARE** | Small curious dot eyes, round "o" mouth |
| No items due within 72 hours (or all caught up) | **CALM** | Big shiny eyes with catchlights, smile, soft blush |

---

## 🪟 Floating Overlay Window Management

`BubbleService` coordinates Android's `WindowManager`:
1. **Collapsed State**:
   - Layout parameters: `52dp × 52dp`, positioned at last saved `(bubbleX, bubbleY)` coordinates.
   - Flags: `FLAG_NOT_FOCUSABLE` — all background touch events pass seamlessly to underlying apps.
   - Touch Handler: Distinguishes taps from drags using system `touchSlop`.
   - Snapping: On touch release, animates horizontally to the nearest edge (left/right margin `8dp`) and persists edge & Y coordinates to DataStore.
2. **Expanded State**:
   - Layout parameters: `MATCH_PARENT × MATCH_PARENT` at `(0, 0)`.
   - Flags: `FLAG_NOT_TOUCH_MODAL` — enables full focus, text input, and gesture scrolling.
   - Scrim: Semi-transparent backdrop (`#73000000`) with scale + fade animation (`scaleIn(0.88f) + fadeIn(220ms)`).
   - Sizing: Adaptive height (`heightIn(min = 180.dp, max = 500.dp)` with `Modifier.weight(1f, fill = false)`). Single items shrink wrap snugly; multiple items scroll smoothly.
   - Touch Targets: 48dp touch targets on action buttons with 8dp spacing.
   - Dismissal: Tapping outside the card or tapping the mascot collapses the card back to the bubble.

---

## 🛡️ Permissions & Battery Optimization

| Permission | Purpose |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Enables drawing the floating companion over other applications. |
| `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_SPECIAL_USE` | Keeps `BubbleService` alive in the background with persistent notification. |
| `POST_NOTIFICATIONS` | Shows foreground service status & deadline alert notifications. |
| `RECEIVE_BOOT_COMPLETED` | Restores the floating bubble automatically when the device boots up. |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Whitelists the service against aggressive OEM battery killers (Doze mode). |
| `VIBRATE` | Tactile haptic feedback when checking off or deleting tasks. |

---

## 🚀 Getting Started & Building

### Prerequisites
- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK**: Java 17 or Java 21
- **Android SDK**: API 34 (Android 14) or API 35 (Android 15)

### Build & Run Commands (PowerShell)
```powershell
# Set Java 21 Home
$env:JAVA_HOME = 'C:\Users\User\.jdks\jbr-21.0.11'

# Compile Kotlin Sources
.\gradlew compileDebugKotlin

# Run All Unit Tests
.\gradlew testDebugUnitTest --no-daemon

# Assemble Debug APK
.\gradlew assembleDebug --no-daemon
```

The output APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Install via ADB
```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Testing Background & Boot Survivability
1. **Enable the floating bubble** in Settings and grant `SYSTEM_ALERT_WINDOW`.
2. **Simulate a device reboot** via ADB:
   ```powershell
   adb shell am broadcast -a android.intent.action.BOOT_COMPLETED -p com.tanmay.buddie
   ```
3. Confirm `BubbleService` immediately wakes up and displays the floating mascot at its previous edge position.

---

## 📄 License
Buddie is developed as an open-source educational companion project.
