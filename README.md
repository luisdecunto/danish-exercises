# Dansk til Luis

An Android app for learning Danish through interactive exercises.

## Features

The app includes three types of exercises:

1. **Multiple Choice**: Tap the correct option from a list of choices
2. **Fill in the Blank**: Type the correct word or phrase
3. **Match Pairs**: Tap items from two groups to match them correctly

## Project Structure

```
androidApp/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/luisdecunto/dansktilluis/
│   │       │   ├── models/          # Data models
│   │       │   │   ├── Exercise.kt
│   │       │   │   └── ExerciseSet.kt
│   │       │   ├── storage/         # Progress tracking
│   │       │   │   └── ProgressManager.kt
│   │       │   ├── ui/              # UI components
│   │       │   │   ├── MultipleChoiceFragment.kt
│   │       │   │   ├── FillInTheBlankFragment.kt
│   │       │   │   ├── MatchPairsFragment.kt
│   │       │   │   └── ExerciseSetAdapter.kt
│   │       │   ├── MainActivity.kt
│   │       │   └── ExerciseActivity.kt
│   │       ├── res/
│   │       │   ├── layout/          # XML layouts
│   │       │   ├── values/          # Strings, colors, themes
│   │       │   └── drawable/        # Images and icons
│   │       └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
└── settings.gradle
```

## How It Works

1. **MainActivity**: Shows a list of exercise sets with progress tracking
2. **ExerciseActivity**: Displays exercises one at a time
3. **Exercise Fragments**: Handle the UI and logic for each exercise type
4. **ProgressManager**: Saves and loads your progress using SharedPreferences

## Current Status

This is a skeleton project with:
- Complete project structure
- Three exercise types implemented
- Progress tracking system
- Sample exercises for testing

## Next Steps

To populate the app with real exercises:

1. Create JSON files with exercise data
2. Add a data loading system to read exercises from files
3. Add more exercise sets based on your Danish learning materials
4. Customize the UI colors and themes
5. Add icons and images

## Building the App

1. Open the project in Android Studio
2. Sync Gradle files
3. Run on an emulator or physical device

## Requirements

- Android Studio Arctic Fox or newer
- Android SDK 24 or higher (Android 7.0+)
- Kotlin 1.9.0
- Gradle 8.1.0
