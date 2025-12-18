# Danish Exercises Database

Exercise data repository for the "Dansk til Luis" Android app.

## What's This?

This repository contains **only the exercise data** (JSON files) for learning Danish. The Android app (kept locally) downloads these exercises and processes them on your phone.

## Repository Structure

```
danish-exercises/
├── data/
│   ├── exercises/
│   │   ├── example_batch_001.json
│   │   ├── batch_002.json
│   │   └── batch_003.json
│   ├── texts/
│   │   └── texts_001.json
│   └── README.md
├── GITHUB_ARCHITECTURE.md
└── README.md
```

## How It Works

```
┌──────────────────────────────────┐
│  This GitHub Repo (Public)       │
│  Contains: Exercise JSON files   │
└────────────┬─────────────────────┘
             │
             │ Downloads from
             │
             ▼
┌──────────────────────────────────┐
│  Android App (Local)             │
│  - Downloads JSON files          │
│  - Stores in Room Database       │
│  - Processes exercises           │
│  - Tracks your progress          │
└──────────────────────────────────┘
```

## Exercise Format

See [data/README.md](data/README.md) for detailed format specifications.

**Quick example:**

```json
{
  "batch_id": "batch_001",
  "created_at": "2025-12-18",
  "version": 1,
  "exercises": [
    {
      "id": "ex_001",
      "type": "multiple_choice",
      "question": "What is 'Hello' in Danish?",
      "text_id": null,
      "difficulty": "beginner",
      "data": {
        "options": ["Hej", "Tak", "Farvel", "Undskyld"],
        "correctAnswerIndex": 0
      }
    },
    {
      "id": "ex_002",
      "type": "fill_in_blank",
      "question": "How do you say 'Good morning'?",
      "text_id": null,
      "difficulty": "beginner",
      "data": {
        "correctAnswer": "Godmorgen",
        "hint": "Similar to English"
      }
    }
  ]
}
```

## Adding Exercises

### On Your Phone with Claude Code:

1. Clone this repository
2. Create/edit files in `data/exercises/`
3. Commit and push changes
4. Open the Dansk til Luis app
5. Tap the refresh button 🔄
6. New exercises download automatically!

### On a Computer:

1. Clone this repository
2. Edit JSON files in `data/exercises/`
3. Commit and push to GitHub
4. App will sync on next refresh

## Exercise Types

1. **Multiple Choice** - Select the correct option
2. **Fill in the Blank** - Type the correct answer
3. **Match Pairs** - Match items from two groups

## Difficulty Levels

- `beginner` - Basic words and phrases
- `intermediate` - Common sentences and grammar
- `advanced` - Complex topics and native expressions

## File Naming Convention

- Exercise batches: `batch_XXX.json` (e.g., `batch_001.json`, `batch_002.json`)
- Text articles: `texts_XXX.json`
- Use sequential numbering
- Each batch should contain 10-20 exercises

## Architecture

For technical details about how the app syncs with this repository, see [GITHUB_ARCHITECTURE.md](GITHUB_ARCHITECTURE.md).

## License

Personal learning project. Exercise content is for educational purposes.

---

**Note:** This repository contains only exercise data. The Android app code is kept separately and is not public.
