# Complete Workflow - Dansk til Luis

## System Overview

This system consists of two separate components:

1. **GitHub Repository** (public) - Stores exercise data only
2. **Android App** (local) - Downloads and processes exercises

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    YOUR PHONE                                │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Claude Code App                                              │
│  ────────────────                                             │
│  • Edit: data/exercises/database.json                        │
│  • Add new texts and exercises                               │
│  • Git commit & push to GitHub                               │
│                                                               │
└──────────────────┬────────────────────────────────────────────┘
                   │
                   │ Git Push
                   │
                   ▼
┌─────────────────────────────────────────────────────────────┐
│              GITHUB (Cloud Storage - Public)                 │
│  https://github.com/luisdecunto/danish-exercises            │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Repository Contains ONLY:                                   │
│  📁 data/                                                     │
│     └── exercises/                                           │
│         └── database.json  ← Single JSON file                │
│                                                               │
│  Structure of database.json:                                 │
│  {                                                            │
│    "texts": {                                                │
│      "text_001": { ...text object... },                      │
│      "text_002": { ...text object... }                       │
│    },                                                         │
│    "exercises": [                                            │
│      { ...exercise 1... },                                   │
│      { ...exercise 2... }                                    │
│    ]                                                          │
│  }                                                            │
│                                                               │
└──────────────────┬────────────────────────────────────────────┘
                   │
                   │ HTTPS Download
                   │
                   ▼
┌─────────────────────────────────────────────────────────────┐
│                    YOUR PHONE                                │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Dansk til Luis App                                           │
│  ───────────────────                                          │
│                                                               │
│  📱 User Action:                                              │
│     Tap refresh button 🔄 in toolbar                         │
│                                                               │
│  🔄 Sync Process:                                             │
│     1. Download database.json from GitHub                    │
│     2. Parse JSON (texts + exercises)                        │
│     3. Import into Room Database                             │
│     4. Update UI                                             │
│                                                               │
│  💾 Local Storage (Room Database):                            │
│     ┌──────────────────────────────────┐                     │
│     │ texts table                      │                     │
│     │ - Downloaded from GitHub         │                     │
│     │ - Cached for offline use         │                     │
│     └──────────────────────────────────┘                     │
│     ┌──────────────────────────────────┐                     │
│     │ exercises table                  │                     │
│     │ - Downloaded from GitHub         │                     │
│     │ - Cached for offline use         │                     │
│     └──────────────────────────────────┘                     │
│     ┌──────────────────────────────────┐                     │
│     │ user_progress table              │                     │
│     │ - Stored ONLY locally            │                     │
│     │ - Never sent to GitHub           │                     │
│     └──────────────────────────────────┘                     │
│                                                               │
│  📊 App Shows:                                                │
│     - List of exercises                                      │
│     - Progress stats                                         │
│     - Practice interface                                     │
│                                                               │
│  ✅ Result: "12 new exercises downloaded!"                   │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

---

## Data Flow

### Direction 1: Creating Exercises (You → GitHub)

```
You (Claude Code)
    ↓
Edit database.json
    ↓
Add new texts/exercises
    ↓
Git commit
    ↓
Git push
    ↓
GitHub stores the data
```

### Direction 2: Practicing Exercises (GitHub → App)

```
GitHub (database.json)
    ↓
App taps refresh 🔄
    ↓
Download JSON file
    ↓
Parse into objects
    ↓
Import to Room Database
    ↓
App displays exercises
    ↓
You practice
    ↓
Progress saved LOCALLY
```

---

## Workflow Step-by-Step

### Phase 1: Creating Exercises (Claude Code on Phone)

**Location:** Claude Code app on your phone

1. **Open the repository:**
   ```bash
   cd danish-exercises
   ```

2. **Edit the database file:**
   ```bash
   # Open: data/exercises/database.json
   ```

3. **Add a new text** (if needed):
   ```json
   "text_003": {
     "id": "text_003",
     "title": "At the Restaurant",
     "content": "Jeg vil gerne bestille mad. Kan jeg få en menu?",
     "translation": "I would like to order food. Can I get a menu?"
   }
   ```

4. **Add new exercises:**
   ```json
   {
     "id": "ex_007",
     "type": "write_word",
     "text_id": "text_003",
     "question": "What does 'bestille' mean?",
     "correct": "order",
     "hint": "Related to making a request"
   }
   ```

5. **Save and validate:**
   - Make sure JSON is valid
   - Check for typos
   - Ensure unique IDs

6. **Commit changes:**
   ```bash
   git add data/exercises/database.json
   git commit -m "Add 5 restaurant vocabulary exercises"
   git push
   ```

7. **Verify on GitHub:**
   - Open https://github.com/luisdecunto/danish-exercises
   - Check that your changes appear

### Phase 2: Syncing to App (Android App)

**Location:** Dansk til Luis app on your phone

1. **Open the app**
   - Launch "Dansk til Luis"

2. **Tap refresh button** 🔄
   - Located in top-right toolbar
   - Icon: circular arrow

3. **App downloads:**
   - Fetches database.json from GitHub
   - Parses the JSON
   - Checks what's new

4. **App imports:**
   - Adds new texts to local database
   - Adds new exercises to local database
   - Shows confirmation message

5. **You see:**
   ```
   ✅ Downloaded 5 new exercises!
   ```

6. **Start practicing:**
   - Navigate to exercise list
   - Select an exercise
   - Practice!
   - Your progress is saved locally

---

## What Gets Stored Where

### GitHub Repository (Public - Anyone Can See)

**Stores:**
- ✅ Exercise questions and answers
- ✅ Text passages (Danish + English)
- ✅ Exercise metadata (IDs, types, hints)

**Does NOT store:**
- ❌ Your progress
- ❌ Your answers
- ❌ Statistics
- ❌ App code

### Android App Local Database (Private - Only On Your Phone)

**Stores:**
- ✅ Downloaded exercises (cached)
- ✅ Downloaded texts (cached)
- ✅ YOUR progress (which exercises completed)
- ✅ YOUR statistics (accuracy, time spent)
- ✅ App settings

**Never Sent to GitHub:**
- ❌ Nothing! Your data stays local

---

## Key Concepts

### 1. Separation of Data and Code

- **GitHub:** Data only (exercise content)
- **Local:** App code + processing logic + your personal data

### 2. Single Source of Truth

- **GitHub database.json** is the master copy
- App downloads and caches it
- You edit GitHub, app syncs from it

### 3. Offline Capable

- App caches everything locally
- Practice without internet
- Sync when you have connection

### 4. No User Accounts

- No login required
- No server communication (except GitHub)
- Progress stored on device only

---

## Common Operations

### Adding a Single Exercise

1. Open `database.json` in Claude Code
2. Find the `"exercises": [` array
3. Add at the end (before closing `]`):
   ```json
   ,
   {
     "id": "ex_008",
     "type": "multiple_choice",
     "text_id": null,
     "question": "What is 'water' in Danish?",
     "options": ["vand", "vin", "mælk", "te"],
     "correct": 0
   }
   ```
4. Save, commit, push
5. Refresh app

### Adding a Text with Related Exercises

1. Open `database.json`
2. Add to `"texts": {` section:
   ```json
   "text_004": {
     "id": "text_004",
     "title": "Shopping",
     "content": "Jeg køber tre æbler og to bananer.",
     "translation": "I buy three apples and two bananas."
   }
   ```
3. Add exercises referencing it:
   ```json
   {
     "id": "ex_009",
     "type": "write_word",
     "text_id": "text_004",
     "question": "According to the text, what fruit does the person buy? (Danish)",
     "correct": "æbler",
     "accept_variants": ["aebler"]
   }
   ```
4. Save, commit, push
5. Refresh app

### Fixing a Typo in an Exercise

1. Open `database.json`
2. Find the exercise by ID (e.g., `"id": "ex_005"`)
3. Fix the typo
4. Save, commit, push
5. Refresh app - **it will update!**

### Checking What's on GitHub

- Visit: https://github.com/luisdecunto/danish-exercises
- Navigate to: data/exercises/database.json
- Click "Raw" to see the JSON content

---

## Technical Details

### GitHub API Usage

The app uses these endpoints:

1. **Download database.json:**
   ```
   GET https://raw.githubusercontent.com/luisdecunto/danish-exercises/main/data/exercises/database.json
   ```
   - Returns: Raw JSON file
   - No authentication needed (public repo)

### Local Database Schema

**texts table:**
```sql
CREATE TABLE texts (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    translation TEXT
);
```

**exercises table:**
```sql
CREATE TABLE exercises (
    id TEXT PRIMARY KEY,
    type TEXT NOT NULL,
    text_id TEXT,
    question TEXT NOT NULL,
    data_json TEXT NOT NULL
);
```

**user_progress table:**
```sql
CREATE TABLE user_progress (
    exercise_id TEXT PRIMARY KEY,
    is_completed INTEGER NOT NULL,
    attempts INTEGER NOT NULL,
    last_answer TEXT,
    completed_at INTEGER
);
```

### Sync Logic

```
1. Download database.json from GitHub
2. Parse JSON into objects
3. Begin transaction
4. For each text:
   - Insert or replace in texts table
5. For each exercise:
   - Insert or replace in exercises table
6. Commit transaction
7. Show "X new exercises" message
```

---

## Advantages of This Architecture

1. **Simple:** No backend server needed
2. **Free:** GitHub hosting is free
3. **Fast:** Direct file download
4. **Reliable:** GitHub's infrastructure
5. **Private:** Your progress stays on device
6. **Versioned:** Git history of all changes
7. **Portable:** Easy to backup and share
8. **Flexible:** Can edit anywhere (phone, computer, web)

---

## Troubleshooting

### "Failed to sync"
- **Check:** Internet connection
- **Check:** GitHub is accessible
- **Check:** database.json has valid JSON syntax

### "No new exercises"
- You're up to date!
- Or changes haven't been pushed to GitHub yet

### Exercises don't update after edit
- Make sure you pushed to GitHub
- Try pulling latest in Claude Code first
- Refresh app again

### Special characters (æøå) don't display
- Ensure file is UTF-8 encoded
- Check your text editor settings

---

## Summary

**Your Workflow:**
```
Create → Commit → Push → Refresh → Practice
```

**Data Location:**
- Exercise content: GitHub (public)
- Your progress: Phone (private)
- App code: Phone (not on GitHub)

**Everything from your phone - no computer needed!** 📱✨
