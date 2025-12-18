# Quick Reference Card

## File Locations

### Want to add exercises?
Edit: `app/src/main/java/com/luisdecunto/dansktilluis/MainActivity.kt` (line 121)

### Want to change colors?
Edit: `app/src/main/res/values/colors.xml`

### Want to change text strings?
Edit: `app/src/main/res/values/strings.xml`

### Want to understand the data models?
Read: `app/src/main/java/com/luisdecunto/dansktilluis/models/Exercise.kt`

## Code Snippets

### Add a Multiple Choice Exercise
```kotlin
MultipleChoiceExercise(
    id = "mc_unique_id",
    question = "Your question here?",
    options = listOf("Option 1", "Option 2", "Option 3", "Option 4"),
    correctAnswerIndex = 0  // First option is correct (0-based)
)
```

### Add a Fill in the Blank Exercise
```kotlin
FillInTheBlankExercise(
    id = "fb_unique_id",
    question = "Your question here?",
    correctAnswer = "correct answer",
    hint = "Optional hint"  // Can be null
)
```

### Add a Match Pairs Exercise
```kotlin
MatchPairsExercise(
    id = "mp_unique_id",
    question = "Match the following",
    leftItems = listOf("Left 1", "Left 2", "Left 3"),
    rightItems = listOf("Right 1", "Right 2", "Right 3"),
    correctPairs = mapOf(
        0 to 0,  // Left 1 matches Right 1
        1 to 1,  // Left 2 matches Right 2
        2 to 2   // Left 3 matches Right 3
    )
)
```

### Create a Complete Exercise Set
```kotlin
ExerciseSet(
    id = "set_unique_id",
    title = "Exercise Set Title",
    description = "Description of what this set teaches",
    exercises = listOf(
        // Add your exercises here
    )
)
```

## Common Tasks

### Clear All Progress
1. Settings > Apps > Dansk til Luis
2. Storage > Clear Data

### Change App Name
Edit `AndroidManifest.xml` line 7: `android:label="Your Name"`

### Change Theme Colors
Edit `res/values/colors.xml`:
- `purple_500` - Primary color
- `teal_200` - Secondary color

### Build APK
1. Build > Build Bundle(s) / APK(s) > Build APK(s)
2. Find APK in `app/build/outputs/apk/debug/`

## Project Structure at a Glance

```
androidApp/
├── app/src/main/
│   ├── java/.../dansktilluis/
│   │   ├── MainActivity.kt          ← List of exercise sets
│   │   ├── ExerciseActivity.kt      ← Exercise runner
│   │   ├── models/
│   │   │   ├── Exercise.kt          ← Exercise types
│   │   │   └── ExerciseSet.kt       ← Exercise grouping
│   │   ├── storage/
│   │   │   └── ProgressManager.kt   ← Save/load progress
│   │   └── ui/
│   │       ├── MultipleChoiceFragment.kt
│   │       ├── FillInTheBlankFragment.kt
│   │       ├── MatchPairsFragment.kt
│   │       └── ExerciseSetAdapter.kt
│   ├── res/
│   │   ├── layout/                  ← UI layouts (XML)
│   │   └── values/                  ← Strings, colors, themes
│   └── AndroidManifest.xml          ← App configuration
└── Documentation
    ├── README.md                    ← Project overview
    ├── SETUP_GUIDE.md               ← How to run
    ├── ADDING_EXERCISES.md          ← How to add content
    ├── PROJECT_SUMMARY.md           ← Complete summary
    └── QUICK_REFERENCE.md           ← This file
```

## Key Concepts

### Exercise Flow
1. User opens app → sees MainActivity with exercise sets
2. User taps "Start" → opens ExerciseActivity
3. ExerciseActivity loads appropriate Fragment for each exercise
4. User completes exercise → progress saved by ProgressManager
5. Next exercise loads automatically
6. When done → returns to MainActivity

### Progress Tracking
- Stored in: SharedPreferences (key-value storage)
- Key format: `exercise_{exercise_id}`
- Contains: completion status + user's answer
- Persists: Yes, survives app restart
- Clear: Via app settings or ProgressManager.clearAllProgress()

## Useful Commands

### In Android Studio Terminal
```bash
# Build the app
./gradlew build

# Install on connected device
./gradlew installDebug

# Clean build files
./gradlew clean
```

## Keyboard Shortcuts (Android Studio)

- **Run app**: Shift + F10
- **Build**: Ctrl + F9
- **Find file**: Ctrl + Shift + N
- **Search everywhere**: Double Shift
- **Reformat code**: Ctrl + Alt + L

## Getting Help

1. Check [SETUP_GUIDE.md](SETUP_GUIDE.md) for installation issues
2. Check [ADDING_EXERCISES.md](ADDING_EXERCISES.md) for content questions
3. Check [README.md](README.md) for architecture overview
4. Check [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) for complete details

## Exercise ID Conventions

Use clear, descriptive IDs:
- `greet_1`, `greet_2` - Greetings set
- `num_1`, `num_2` - Numbers set
- `food_mc_1` - Food set, multiple choice, first exercise
- `food_fb_2` - Food set, fill in blank, second exercise
- `food_mp_1` - Food set, match pairs, first exercise

Keep IDs unique across ALL exercises!
