# How to Add Exercises

This guide explains how to add your own Danish exercises to the app.

## Current Setup

Right now, the app uses hardcoded sample exercises in [MainActivity.kt](app/src/main/java/com/luisdecunto/dansktilluis/MainActivity.kt:121-188). These are defined in the `createSampleExerciseSets()` function.

## Quick Start: Adding Exercises Directly in Code

The easiest way to add exercises is to edit the `createSampleExerciseSets()` function in [MainActivity.kt](app/src/main/java/com/luisdecunto/dansktilluis/MainActivity.kt).

### Adding a Multiple Choice Exercise

```kotlin
MultipleChoiceExercise(
    id = "unique_id_here",
    question = "What is 'apple' in Danish?",
    options = listOf("Æble", "Pære", "Banan", "Orange"),
    correctAnswerIndex = 0  // 0-based index (0 = first option)
)
```

### Adding a Fill in the Blank Exercise

```kotlin
FillInTheBlankExercise(
    id = "unique_id_here",
    question = "How do you say 'bicycle' in Danish?",
    correctAnswer = "Cykel",
    hint = "Starts with C"  // Optional hint
)
```

### Adding a Match Pairs Exercise

```kotlin
MatchPairsExercise(
    id = "unique_id_here",
    question = "Match the Danish words with English",
    leftItems = listOf("Hund", "Kat", "Fugl"),
    rightItems = listOf("Bird", "Dog", "Cat"),
    correctPairs = mapOf(
        0 to 1,  // Hund (index 0) matches Dog (index 1)
        1 to 2,  // Kat (index 1) matches Cat (index 2)
        2 to 0   // Fugl (index 2) matches Bird (index 0)
    )
)
```

### Creating a Complete Exercise Set

```kotlin
ExerciseSet(
    id = "animals",
    title = "Animals",
    description = "Learn the names of animals in Danish",
    exercises = listOf(
        MultipleChoiceExercise(
            id = "animal_1",
            question = "What is 'dog' in Danish?",
            options = listOf("Hund", "Kat", "Ko", "Hest"),
            correctAnswerIndex = 0
        ),
        FillInTheBlankExercise(
            id = "animal_2",
            question = "How do you say 'cat' in Danish?",
            correctAnswer = "Kat"
        ),
        MatchPairsExercise(
            id = "animal_3",
            question = "Match the animals",
            leftItems = listOf("Ko", "Hest", "Gris"),
            rightItems = listOf("Horse", "Pig", "Cow"),
            correctPairs = mapOf(0 to 2, 1 to 0, 2 to 1)
        )
    )
)
```

## Alternative: Using JSON Files (Future Enhancement)

The project includes a sample JSON format in [sample_exercises.json](sample_exercises.json). To use JSON files:

1. Place JSON files in `app/src/main/assets/`
2. Create a loader class to read and parse the JSON
3. Use Gson to deserialize into Exercise objects
4. Replace hardcoded exercises with loaded ones

Example loader implementation:

```kotlin
class ExerciseLoader(private val context: Context) {
    fun loadExercisesFromAssets(filename: String): List<ExerciseSet> {
        val json = context.assets.open(filename).bufferedReader().use { it.readText() }
        // Parse JSON and create ExerciseSet objects
        return emptyList() // TODO: Implement parsing
    }
}
```

## Tips

1. **IDs must be unique**: Each exercise needs a unique ID for progress tracking
2. **Case-insensitive answers**: Fill-in-the-blank exercises ignore case
3. **Index-based matching**: Match pairs use 0-based indices
4. **Test thoroughly**: Try each exercise type to ensure correctness

## Importing from Your Claude Project

Since you have exercises in your Claude project:

1. Export your exercises as structured data
2. Convert them to the format shown above
3. Add them to `createSampleExerciseSets()`
4. Rebuild and run the app

## Progress Tracking

Progress is automatically saved:
- Completed exercises are marked with a checkmark
- Progress persists across app restarts
- You can clear progress from the UI (future feature)
