# Workflow Architecture - Dansk til Luis

## Overview

Random exercise presentation system with progress tracking and text article support.

---

## Database Schema

### Option 1: Single Database with Related Tables (Recommended)

```
┌─────────────────────────────────────────────────────────────┐
│                     DATABASE: dansk_exercises.db             │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Table: texts                                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ id              INTEGER PRIMARY KEY                   │  │
│  │ title           TEXT                                  │  │
│  │ content         TEXT (full article text)             │  │
│  │ source          TEXT (optional: where it came from)  │  │
│  │ difficulty      TEXT (beginner/intermediate/advanced)│  │
│  │ created_at      TIMESTAMP                            │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  Table: exercises                                           │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ id              TEXT PRIMARY KEY (e.g., "ex_001")    │  │
│  │ type            TEXT (multiple_choice/match_pairs/   │  │
│  │                      fill_in_blank)                  │  │
│  │ question        TEXT                                 │  │
│  │ text_id         INTEGER (NULL if not text-related)   │  │
│  │                 FOREIGN KEY → texts.id               │  │
│  │ difficulty      TEXT (beginner/intermediate/advanced)│  │
│  │ data            TEXT (JSON blob with exercise data)  │  │
│  │ created_at      TIMESTAMP                            │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  Table: user_progress                                       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ exercise_id     TEXT PRIMARY KEY                     │  │
│  │                 FOREIGN KEY → exercises.id           │  │
│  │ is_completed    BOOLEAN                              │  │
│  │ attempts        INTEGER (number of tries)            │  │
│  │ last_answer     TEXT                                 │  │
│  │ completed_at    TIMESTAMP (NULL if not completed)    │  │
│  │ updated_at      TIMESTAMP                            │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Exercise Data JSON Examples

**Multiple Choice:**
```json
{
  "options": ["Hej", "Tak", "Farvel", "Undskyld"],
  "correctAnswerIndex": 0
}
```

**Fill in the Blank:**
```json
{
  "correctAnswer": "Godmorgen",
  "hint": "It's similar to English"
}
```

**Match Pairs:**
```json
{
  "leftItems": ["Hej", "Farvel", "Godnat"],
  "rightItems": ["Goodbye", "Hello", "Good night"],
  "correctPairs": {"0": 1, "1": 0, "2": 2}
}
```

---

## App Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      APP LAUNCH                             │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                    MainActivity                             │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  📊 Statistics Card                                   │ │
│  │  ┌─────────────────────────────────────────────────┐ │ │
│  │  │ Total Exercises: 150                            │ │ │
│  │  │ Completed: 45  (30%)                            │ │ │
│  │  │ Remaining: 105                                  │ │ │
│  │  │ [Progress Bar =========>              ]        │ │ │
│  │  └─────────────────────────────────────────────────┘ │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │           [Start Random Exercise]                     │ │
│  │                                                       │ │
│  │           [Browse by Difficulty]                      │ │
│  │                                                       │ │
│  │           [Review Completed Exercises]                │ │
│  │                                                       │ │
│  │           [Settings]                                  │ │
│  └───────────────────────────────────────────────────────┘ │
└────────────────────────┬────────────────────────────────────┘
                         │
         ┌───────────────┴───────────────┐
         │                               │
         ▼                               ▼
┌──────────────────┐            ┌──────────────────┐
│ Start Random     │            │ Browse by        │
│ Exercise         │            │ Difficulty       │
└────────┬─────────┘            └────────┬─────────┘
         │                               │
         │                               ▼
         │                      ┌──────────────────┐
         │                      │ DifficultyActivity│
         │                      │ - Beginner       │
         │                      │ - Intermediate   │
         │                      │ - Advanced       │
         │                      └────────┬─────────┘
         │                               │
         └───────────────┬───────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              ExerciseLoaderService                          │
│                                                             │
│  1. Query database for incomplete exercises                │
│  2. Filter by difficulty (if selected)                     │
│  3. SELECT random exercise:                                │
│     SELECT * FROM exercises                                │
│     WHERE id NOT IN (                                      │
│       SELECT exercise_id FROM user_progress                │
│       WHERE is_completed = true                            │
│     )                                                       │
│     ORDER BY RANDOM()                                      │
│     LIMIT 1;                                               │
│                                                             │
│  4. Check if exercise has text_id                          │
│  5. If yes, load associated text                           │
│  6. Pass to ExerciseActivity                               │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  ExerciseActivity                           │
│                                                             │
│  IF exercise.text_id != NULL:                              │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  📄 Text Article Card (Collapsible)                   │ │
│  │  ┌─────────────────────────────────────────────────┐ │ │
│  │  │ Title: "Danish Traditions"              [▼]     │ │ │
│  │  │                                                 │ │ │
│  │  │ [When expanded, shows full article text]       │ │ │
│  │  │                                                 │ │ │
│  │  └─────────────────────────────────────────────────┘ │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  Exercise Fragment (one of three types)                    │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  Question: "What is 'Hello' in Danish?"              │ │
│  │  [Exercise UI based on type]                         │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  [Submit] → Check answer → Update progress                 │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              ProgressManager                                │
│                                                             │
│  On answer submission:                                     │
│  1. Check if correct                                       │
│  2. INSERT OR REPLACE INTO user_progress:                  │
│     - exercise_id                                          │
│     - is_completed (true if correct)                       │
│     - attempts (increment)                                 │
│     - last_answer                                          │
│     - updated_at (current timestamp)                       │
│     - completed_at (if correct)                            │
│                                                             │
│  3. Show feedback to user                                  │
│  4. Load next random exercise (if correct)                 │
│     OR allow retry (if incorrect)                          │
└─────────────────────────────────────────────────────────────┘
```

---

## Key Components

### 1. Database Manager
```
DatabaseManager.kt
├── createDatabase()
├── insertExercise(exercise)
├── insertText(text)
├── getRandomExercise(difficulty?, excludeCompleted)
├── getTextById(textId)
├── saveProgress(exerciseId, isCorrect, answer)
├── getProgress()
├── getExerciseById(id)
└── resetProgress()
```

### 2. Exercise Loader Service
```
ExerciseLoaderService.kt
├── loadNextExercise()
│   ├── Query incomplete exercises
│   ├── Filter by difficulty (optional)
│   ├── Select random
│   └── Load associated text (if any)
├── parseExerciseData(json)
└── createExerciseObject(type, data)
```

### 3. Text Display Component
```
TextDisplayFragment.kt
├── Collapsible card
├── Shows article title
├── Expands to show full text
└── Stays visible while doing exercise
```

---

## Exercise-Text Relationship Examples

### Example 1: Text-Related Exercise
```
Text ID: 1
Title: "Danish Food Culture"
Content: "Danes love smørrebrød, which is an open-faced sandwich..."

Exercise ID: "ex_045"
Type: multiple_choice
Question: "Based on the text, what is smørrebrød?"
Text ID: 1 ← Links to the text above
Data: {
  "options": ["A soup", "An open sandwich", "A dessert", "A drink"],
  "correctAnswerIndex": 1
}
```

### Example 2: Standalone Exercise
```
Exercise ID: "ex_012"
Type: fill_in_blank
Question: "How do you say 'Thank you' in Danish?"
Text ID: NULL ← No associated text
Data: {
  "correctAnswer": "Tak"
}
```

---

## Random Selection Algorithm

### SQL Query Strategy
```sql
-- Get random incomplete exercise
SELECT e.*, t.title, t.content
FROM exercises e
LEFT JOIN texts t ON e.text_id = t.id
WHERE e.id NOT IN (
    SELECT exercise_id
    FROM user_progress
    WHERE is_completed = 1
)
ORDER BY RANDOM()
LIMIT 1;
```

### With Difficulty Filter
```sql
SELECT e.*, t.title, t.content
FROM exercises e
LEFT JOIN texts t ON e.text_id = t.id
WHERE e.difficulty = 'beginner'
AND e.id NOT IN (
    SELECT exercise_id
    FROM user_progress
    WHERE is_completed = 1
)
ORDER BY RANDOM()
LIMIT 1;
```

---

## Progress Tracking

### Statistics Calculation
```kotlin
// Total exercises
SELECT COUNT(*) FROM exercises

// Completed exercises
SELECT COUNT(*) FROM user_progress WHERE is_completed = 1

// Completion percentage
(completed / total) * 100

// Exercises by difficulty
SELECT difficulty, COUNT(*)
FROM exercises
GROUP BY difficulty

// Completed by difficulty
SELECT e.difficulty, COUNT(*)
FROM user_progress p
JOIN exercises e ON p.exercise_id = e.id
WHERE p.is_completed = 1
GROUP BY e.difficulty
```

---

## Data Import/Export

### JSON Import Format
```json
{
  "texts": [
    {
      "id": 1,
      "title": "Danish Traditions",
      "content": "Full text here...",
      "difficulty": "intermediate"
    }
  ],
  "exercises": [
    {
      "id": "ex_001",
      "type": "multiple_choice",
      "question": "What is 'Hello'?",
      "text_id": null,
      "difficulty": "beginner",
      "data": {
        "options": ["Hej", "Tak", "Farvel"],
        "correctAnswerIndex": 0
      }
    },
    {
      "id": "ex_002",
      "type": "fill_in_blank",
      "question": "According to the text, what do Danes celebrate?",
      "text_id": 1,
      "difficulty": "intermediate",
      "data": {
        "correctAnswer": "Jul",
        "hint": "Christmas in Danish"
      }
    }
  ]
}
```

---

## User Experience Flow

```
User Opens App
    ↓
Sees Progress Dashboard
    ↓
Taps "Start Random Exercise"
    ↓
App queries database → Finds incomplete exercise
    ↓
If exercise has text → Show text article card (collapsed)
    ↓
Show exercise question
    ↓
User can expand text to read (if available)
    ↓
User answers exercise
    ↓
Submit → Check answer
    ↓
If Correct:
    ├→ Save progress
    ├→ Show success feedback
    ├→ Wait 1.5s
    └→ Load next random exercise

If Incorrect:
    ├→ Save attempt
    ├→ Show error feedback
    └→ Allow retry (same exercise)
```

---

## Future Enhancements (Not in Skeleton)

### Phase 2
- Exercise categories/tags
- Spaced repetition algorithm
- Streak tracking
- Daily goals

### Phase 3
- Audio pronunciation exercises
- Image-based exercises
- Export progress as CSV

---

## File Structure

```
app/src/main/
├── java/.../dansktilluis/
│   ├── database/
│   │   ├── DatabaseManager.kt
│   │   ├── ExerciseDao.kt
│   │   ├── TextDao.kt
│   │   └── ProgressDao.kt
│   ├── models/
│   │   ├── Exercise.kt (already exists)
│   │   ├── Text.kt (new)
│   │   └── Progress.kt (new)
│   ├── services/
│   │   └── ExerciseLoaderService.kt (new)
│   ├── ui/
│   │   ├── MainActivity.kt (update)
│   │   ├── ExerciseActivity.kt (update)
│   │   ├── TextDisplayFragment.kt (new)
│   │   ├── MultipleChoiceFragment.kt (exists)
│   │   ├── FillInTheBlankFragment.kt (exists)
│   │   └── MatchPairsFragment.kt (exists)
│   └── utils/
│       ├── JsonImporter.kt (new)
│       └── StatisticsCalculator.kt (new)
└── assets/
    └── exercises.json (database seed file)
```

---

## Implementation Priority

### Phase 1 (Core Functionality)
1. ✅ Exercise fragments (DONE)
2. ⬜ Create database schema
3. ⬜ Implement DatabaseManager
4. ⬜ Create Text model and display
5. ⬜ Implement ExerciseLoaderService
6. ⬜ Update MainActivity with statistics
7. ⬜ Update progress tracking

### Phase 2 (Data Management)
1. ⬜ JSON importer for bulk exercise loading
2. ⬜ Database migration system
3. ⬜ Export progress feature

### Phase 3 (Polish)
1. ⬜ Difficulty filter
2. ⬜ Review completed exercises
3. ⬜ Better statistics dashboard
4. ⬜ Settings screen

---

## Notes

- **Random selection** ensures variety and prevents monotony
- **Text articles** provide context for comprehension exercises
- **Progress tracking** persists across app restarts (SQLite)
- **Flexible schema** allows easy addition of new exercise types
- **JSON import** allows you to bulk-load exercises from Claude project

This architecture supports your workflow perfectly! 🎉
