# RemindFlow 📱

> **A smart daily routine & reminder manager for Android built for all areas of life.**

RemindFlow features an intuitive **4-quadrant daily organizer**, instant visual status, actionable alarms with customizable snooze/skip options, and intelligent supply/inventory tracking so you can manage **medication, workouts, hydration, work tasks, personal habits, and home chores** in one unified experience.

---

## 🌟 Key Features

### 1. The 4-Quadrant Daily Organizer
RemindFlow groups daily routines into 4 intuitive time blocks with actual scheduled timestamps:
* 🌅 **Morning** (`06:00 – 12:00`): Breakfast vitamins, morning workout, hydration goal
* ☀️ **Afternoon** (`12:00 – 18:00`): Focus blocks, lunch walk, standup reminders
* 🌆 **Evening** (`18:00 – 22:00`): Evening supplements, reading, family time
* 🌙 **Night** (`22:00 – 06:00`): Wind-down routines, skincare, sleep alarms

### 2. Dual Reminder Modes: Recurring vs. Single (1-Time)
RemindFlow supports both repeat routines and one-off tasks:
* **🔁 Recurring Mode**:
  * Select specific repeat days: **Daily**, **Weekdays (Mon–Fri)**, **Weekends**, or custom multi-day combinations (e.g. **Every Tuesday & Thursday**).
  * **Optional End Date**: Stop repeating after a specified calendar date.
* **📅 Single Mode (1-Time Only)**:
  * Schedule a reminder for a specific date (e.g. dentist appointment, parcel pickup, maintenance).
  * Fires once on the target date, then completes.

### 3. Actionable Notifications & Alarms
* Direct action buttons right from the Android notification bar:
  * **[✓ Done]**: Marks task complete, increments streak, and decrements stock.
  * **[⏱ Snooze]**: Quick snooze for 10m, 30m, 1h, or 2h.
  * **[✕ Skip]**: Logs task as skipped without breaking your active habit streak.
* Support for **Persistent Alarms** that bypass silent/DND modes for critical routines.

### 4. Category-Aware Inventory Tracking (Tailored for Medicines & Supplies)
* **Only for Physical Supplies**: Inventory counts and refill warnings apply specifically to categories like **Health & Meds** (prescriptions, vitamins, capsules).
* **Zero Clutter for General Reminders**: Work tasks, habits, fitness routines, and general reminders don't show stock tracking fields.
* **Auto-Decrement on Take**: When marking a medicine complete, remaining pill/capsule count decreases by the dose.
* **Low-Stock Alert**: Displays a warning badge when supplies fall below threshold, with instant **+10 / +30 Refill** buttons.

### 4. Compliance & Streak Analytics
* Real-time **Daily Progress Ring** showing percentage and fraction completed today.
* **Active Streak Tracker** (🔥) rewarding consistency.
* 7-day adherence bar chart and comprehensive historical action log.

---

## 🏗 Tech Stack & Architecture

* **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3
* **Language**: 100% Kotlin 2.x
* **Local Storage**: [Room Database](https://developer.android.com/training/data-storage/room) with Coroutines `Flow`
* **Scheduling**: Android `AlarmManager` with exact alarm intents (`setExactAndAllowWhileIdle`)
* **Architecture**: Clean Architecture + MVVM (Model-View-ViewModel) with unidirectional data flow
* **Navigation**: Jetpack Navigation Compose

---

## 📁 Project Structure

```
reminder/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/remindflow/app/
│   │   │   ├── RemindFlowApplication.kt       # Application class & notification channels
│   │   │   ├── domain/model/                  # Domain entities (Reminder, TimeQuadrant, Category)
│   │   │   ├── data/
│   │   │   │   ├── local/entity/              # Room entities (ReminderEntity, ReminderLogEntity)
│   │   │   │   ├── local/dao/                 # Room DAOs (ReminderDao, ReminderLogDao)
│   │   │   │   ├── local/RemindFlowDatabase.kt # Room DB + Initial seed routines
│   │   │   │   └── repository/                # ReminderRepository
│   │   │   ├── receiver/                      # AlarmReceiver, NotificationActionReceiver, BootReceiver
│   │   │   ├── service/                       # AlarmScheduler
│   │   │   └── ui/
│   │   │       ├── MainActivity.kt            # Main entry point & notification permission
│   │   │       ├── theme/                     # Material 3 colors, typography, theme
│   │   │       ├── navigation/                # NavRoutes & NavHost
│   │   │       ├── dashboard/                 # 4-Quadrant Home Hub, WeekStrip, ProgressRing
│   │   │       ├── add_edit/                  # Create & customize reminders
│   │   │       ├── inventory/                 # Stock & supply tracker with refills
│   │   │       ├── analytics/                 # Compliance score & streak analytics
│   │   │       └── settings/                  # Quadrant boundary customizer & notification test
│   │   └── res/                               # Icons, strings, themes
│   └── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml                     # Gradle version catalog
│   └── wrapper/
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 How to Run & Build

### In Android Studio
1. Open Android Studio (Ladybug / Koala or newer).
2. Choose **Open** and select `/home/warikoop/projects/reminder`.
3. Allow Gradle to sync dependencies.
4. Run on an Android Emulator (API 26+) or connected physical device.

### In Terminal via Gradle
```bash
./gradlew test        # Run unit tests
./gradlew assembleDebug # Build debug APK
```
The resulting APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`
