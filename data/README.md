# Danish Exercises Data Format

This folder contains the exercise database for the "Dansk til Luis" Android app.

## Database Structure

The database is a **single JSON file** (`database.json`) with two main sections:

```json
{
  "texts": {
    "text_id": { text_object },
    ...
  },
  "exercises": [
    { exercise_object },
    ...
  ]
}
```

---

## Text Objects

Texts are reading passages that exercises can reference. Stored as a dictionary (object) with text IDs as keys.

### Format

```json
"text_001": {
  "id": "text_001",
  "title": "At the Supermarket",
  "content": "Jeg går i supermarkedet hver onsdag. Jeg køber mælk, brød og frugt.",
  "translation": "I go to the supermarket every Wednesday. I buy milk, bread and fruit."
}
```

### Fields

- **id** (string, required): Unique identifier matching the dictionary key
- **title** (string, required): Display title
- **content** (string, required): The Danish text
- **translation** (string, optional): English translation

---

## Exercise Types

All exercises share these common fields:

- **id** (string, required): Unique identifier (e.g., "ex_001")
- **type** (string, required): One of: `"multiple_choice"`, `"write_word"`, `"match_pairs"`
- **text_id** (string or null, required): Reference to text ID, or `null` for standalone
- **question** (string, required): The question/prompt to display

### 1. Multiple Choice

User selects one option from a list.

**Additional fields:**
- **options** (array of strings): The choices
- **correct** (integer): Zero-based index of correct option
- **explanation** (string, optional): Shown after answering

**Example:**
```json
{
  "id": "ex_001",
  "type": "multiple_choice",
  "text_id": null,
  "question": "What is 'Hello' in Danish?",
  "options": ["Hej", "Tak", "Farvel", "Undskyld"],
  "correct": 0,
  "explanation": "Hej is the most common Danish greeting"
}
```

### 2. Write Word

User types the correct answer.

**Additional fields:**
- **correct** (string): The correct answer
- **accept_variants** (array of strings, optional): Alternative acceptable answers
- **hint** (string, optional): Hint text

**Example:**
```json
{
  "id": "ex_002",
  "type": "write_word",
  "text_id": null,
  "question": "How do you say 'Thank you' in Danish?",
  "correct": "tak",
  "accept_variants": ["mange tak"],
  "hint": "It's very short"
}
```

**Validation:** Case-insensitive, whitespace trimmed

### 3. Match Pairs

User matches items from left column to right column.

**Additional fields:**
- **pairs** (array of objects): Each has `left` and `right` strings

**Example:**
```json
{
  "id": "ex_003",
  "type": "match_pairs",
  "text_id": null,
  "question": "Match the Danish words with their English translations",
  "pairs": [
    {"left": "mælk", "right": "milk"},
    {"left": "brød", "right": "bread"},
    {"left": "frugt", "right": "fruit"}
  ]
}
```

**Implementation:** Shuffle right column for display

---

## Creating Exercises on Your Phone

### Using Claude Code:

1. **Open** `data/exercises/database.json`
2. **Add to texts section** (if needed):
   ```json
   "text_003": {
     "id": "text_003",
     "title": "Your Title",
     "content": "Danish text here...",
     "translation": "English translation..."
   }
   ```

3. **Add to exercises array**:
   ```json
   {
     "id": "ex_007",
     "type": "write_word",
     "text_id": "text_003",
     "question": "Your question?",
     "correct": "answer",
     "hint": "Optional hint"
   }
   ```

4. **Commit and push**:
   ```bash
   git add data/exercises/database.json
   git commit -m "Add 5 new exercises"
   git push
   ```

5. **Open app** → Tap refresh 🔄 → Practice!

---

## Important Notes

- ✅ Use UTF-8 encoding (supports æ, ø, å)
- ✅ `correct` index in multiple_choice is zero-based (0 = first option)
- ✅ `text_id` must reference an existing text or be `null`
- ✅ All IDs must be unique
- ✅ Use descriptive ID prefixes: `ex_` for exercises, `text_` for texts

---

## Complete Example

See [database.json](database.json) for a complete working example with all three exercise types.

---

## Tips for Creating Good Exercises

1. **Start simple** - Begin with basic vocabulary
2. **Use texts** - Link exercises to reading passages for context
3. **Mix types** - Use all three exercise types for variety
4. **Add hints** - Help learners with challenging questions
5. **Explain answers** - Use the `explanation` field for learning
6. **Accept variants** - For write_word, include common spelling variations
7. **Test on phone** - Make sure special characters (æøå) display correctly

---

## File Format

- **Filename:** `database.json` (required)
- **Location:** `data/exercises/`
- **Encoding:** UTF-8
- **Format:** Valid JSON (use a validator if unsure)

The app downloads this file from GitHub and imports it into a local Room database for offline use.
