# Danish Exercise Database Format Specification

## Overview

The database is a single JSON file containing two main sections:
1. **texts**: A dictionary of reading passages that exercises can reference
2. **exercises**: An array of exercise objects of various types

## Structure
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

## Text Object Format

Each text is stored in the `texts` dictionary with a unique ID as the key.

**Fields:**
- `id` (string, required): Unique identifier matching the dictionary key
- `title` (string, required): Display title for the text
- `content` (string, required): The Danish text content
- `translation` (string, optional): English translation of the content

**Example:**
```json
"text_001": {
  "id": "text_001",
  "title": "At the Supermarket",
  "content": "Jeg går i supermarkedet hver onsdag. Jeg køber mælk, brød og frugt.",
  "translation": "I go to the supermarket every Wednesday. I buy milk, bread and fruit."
}
```

## Exercise Types

There are three types of exercises: `multiple_choice`, `write_word`, and `match_pairs`.

### Common Fields (all exercise types)

- `id` (string, required): Unique identifier for the exercise
- `type` (string, required): One of: "multiple_choice", "write_word", "match_pairs"
- `question` (string, required): The question or prompt to display
- `text_id` (string, optional): Reference to a text ID from the texts dictionary. Use `null` for standalone exercises.
- `level` (string, optional): Difficulty level. One of: "A1", "A2", "B1", "B2", "C1", "C2". Use `null` if not specified.

### Type 1: Multiple Choice

**Additional fields:**
- `options` (array of strings, required): The answer choices to display
- `correct` (integer, required): Zero-based index of the correct option
- `explanation` (string, optional): Explanation shown after answering

**Example:**
```json
{
  "id": "ex_001",
  "type": "multiple_choice",
  "text_id": "text_001",
  "level": "A2",
  "question": "Hvornår går jeg i supermarkedet?",
  "options": ["Mandag", "Onsdag", "Fredag", "Lørdag"],
  "correct": 1,
  "explanation": "The text says 'hver onsdag' which means every Wednesday"
}
```

### Type 2: Write Word

User must type the correct answer.

**Additional fields:**
- `correct` (string, required): The correct answer (will be compared case-insensitive)
- `accept_variants` (array of strings, optional): Alternative acceptable answers (e.g., spelling variations)
- `hint` (string, optional): Hint to display to the user

**Example:**
```json
{
  "id": "ex_002",
  "type": "write_word",
  "text_id": "text_001",
  "level": "A1",
  "question": "How do you say 'milk' in Danish?",
  "correct": "mælk",
  "accept_variants": ["maelk"],
  "hint": "Starts with 'm'"
}
```

**Validation logic:**
- Normalize both user input and correct answer to lowercase
- Strip whitespace from user input
- Accept if matches `correct` OR any item in `accept_variants`

### Type 3: Match Pairs

User must match items from a left column with items from a right column.

**Additional fields:**
- `pairs` (array of objects, required): Array of matching pairs
  - Each object has `left` (string) and `right` (string) properties

**Example:**
```json
{
  "id": "ex_003",
  "type": "match_pairs",
  "text_id": null,
  "level": "A1",
  "question": "Match the Danish words with their English translations",
  "pairs": [
    {"left": "mælk", "right": "milk"},
    {"left": "brød", "right": "bread"},
    {"left": "frugt", "right": "fruit"},
    {"left": "ost", "right": "cheese"}
  ]
}
```

**Implementation notes:**
- The app should shuffle the right column for display
- User creates matches between left and right items
- Check if all pairs match correctly

## Relationship Rules

1. **Text references**: When `text_id` is not `null`, it MUST reference an existing text ID in the `texts` dictionary
2. **Standalone exercises**: Set `text_id` to `null` for exercises that don't reference any text
3. **ID uniqueness**: All exercise IDs and text IDs must be unique within their respective collections
4. **ID format**: Use descriptive prefixes (e.g., "ex_" for exercises, "text_" for texts) followed by numbers or descriptive names

## Complete Minimal Example
```json
{
  "texts": {
    "text_001": {
      "id": "text_001",
      "title": "Greetings",
      "content": "Hej! Hvordan har du det?",
      "translation": "Hello! How are you?"
    }
  },
  "exercises": [
    {
      "id": "ex_001",
      "type": "multiple_choice",
      "text_id": "text_001",
      "level": "A1",
      "question": "What does 'Hej' mean?",
      "options": ["Goodbye", "Hello", "Thank you", "Please"],
      "correct": 1
    },
    {
      "id": "ex_002",
      "type": "write_word",
      "text_id": null,
      "level": "A1",
      "question": "Translate to Danish: 'Thank you'",
      "correct": "tak",
      "accept_variants": ["mange tak"]
    },
    {
      "id": "ex_003",
      "type": "match_pairs",
      "text_id": null,
      "level": "A1",
      "question": "Match the greetings",
      "pairs": [
        {"left": "Hej", "right": "Hello"},
        {"left": "Farvel", "right": "Goodbye"}
      ]
    }
  ]
}
```

## Important Implementation Notes

- All text content should support Danish characters (æ, ø, å)
- Use UTF-8 encoding for the JSON file
- The `correct` field in multiple_choice uses zero-based indexing (0 = first option)
- For write_word exercises, perform case-insensitive comparison after trimming whitespace
- The app should handle missing optional fields gracefully (use empty string or appropriate defaults)