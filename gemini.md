# WallHabit Project Context

## Overview
WallHabit is an Android application that helps users build habits by visualizing their progress directly on their device wallpaper. It uses a Live Wallpaper service to render a "Consistency Map" (Artwork Grid) that reflects the user's habit completion history.

## Key Features
- **Habit Tracking**: Support for binary (yes/no) and numeric (value-based) habit tracking.
- **Priority/Wallpaper Habit**: A single "Priority" habit is selected to be displayed on the Live Wallpaper.
- **Live Wallpaper**: A custom `WallpaperService` that renders a rainbow-themed grid representing habit completions.
- **Consistency Insights**: Visualization of streaks, success rates, and weekly progress.
- **Reminders**: Integrated alarm system for habit notifications.

## Architecture
- **Clean Architecture**: Organized into `core`, `data`, `domain`, and `features` layers.
- **Jetpack Compose**: Modern UI implementation for all screens.
- **Room Database**: Local persistence for habits and completion logs.
- **Flow & ViewModel**: Reactive data streams using Kotlin Coroutines and StateFlow.

## Recent Enhancements (Priority Interaction System)
- **Unified Priority & Wallpaper**: The "Star" icon in `HabitDetailScreen` now sets a habit as both the application priority and the active wallpaper entity.
- **Wallpaper Selection Screen**: A dedicated screen (`WallpaperSelectionScreen`) for switching between habits with live-rendered miniature previews of the wallpaper artwork.
- **Context-Aware Actions**: The wallpaper preview/set button is now only enabled for the priority habit to simplify the user experience.

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose, Material 3
- **Local DB**: Room
- **Navigation**: Compose Navigation
- **Asynchronous**: Coroutines, Flow
- **Background**: Live Wallpaper Service, AlarmManager

## Development Guidelines
- Maintain the "Priority = Wallpaper" relationship for simplicity.
- Use the centralized design system in `com.dev.habitwallpaper.core.designsystem`.
- When updating habit states, ensure the Live Wallpaper service is notified via the `com.dev.habitwallpaper.UPDATE_WALLPAPER` broadcast.
