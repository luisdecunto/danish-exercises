# Danish Exercises Data

This folder contains all the exercise data for the "Dansk til Luis" app.

## Structure

```
data/
├── exercises/     # Exercise batch JSON files
│   ├── batch_001.json
│   ├── batch_002.json
│   └── ...
└── texts/         # Reading text JSON files
    ├── texts_001.json
    ├── texts_002.json
    └── ...
```

## How to Add New Exercises

### Using Claude Code on Your Phone

1. **Create a new JSON file** in `data/exercises/` folder
2. **Name it** with the next batch number (e.g., `batch_002.json`)
3. **Format:**

```json
{
  "batch_id": "batch_002",
  "created_at": "2025-12-18",
  "version": 1,
  "exercises": [
    {
      "id": "ex_004",
      "type": "multiple_choice",
      "question": "Your question here?",
      "text_id": null,
      "difficulty": "beginner",
      "data": {
        "options": ["Option 1", "Option 2", "Option 3", "Option 4"],
        "correctAnswerIndex": 0
      }
    }
  ]
}
```

4. **Commit and push** to GitHub:
```bash
git add data/exercises/batch_002.json
git commit -m "Add batch 002 exercises"
git push
```

5. **Open the app** on your phone
6. **Tap the refresh button** (🔄)
7. **Start practicing!**

## Exercise Types

### 1. Multiple Choice
```json
{
  "type": "multiple_choice",
  "data": {
    "options": ["Option 1", "Option 2", "Option 3", "Option 4"],
    "correctAnswerIndex": 0
  }
}
```

### 2. Fill in the Blank
```json
{
  "type": "fill_in_blank",
  "data": {
    "correctAnswer": "Answer",
    "hint": "Optional hint"
  }
}
```

### 3. Match Pairs
```json
{
  "type": "match_pairs",
  "data": {
    "leftItems": ["Item 1", "Item 2", "Item 3"],
    "rightItems": ["Match A", "Match B", "Match C"],
    "correctPairs": {
      "0": 1,
      "1": 2,
      "2": 0
    }
  }
}
```

## Difficulty Levels

- `beginner` - Basic words and phrases
- `intermediate` - Common sentences and grammar
- `advanced` - Complex topics and native expressions

## Tips

- **Keep batch files small** (10-20 exercises each)
- **Use unique IDs** for each exercise (e.g., `ex_001`, `ex_002`)
- **Test on your phone** before creating many exercises
- **Commit frequently** so you don't lose work
