# GitHub Architecture - Dansk til Luis

## Overview

Simple cloud storage using GitHub to sync exercises between Claude Code (phone) and the Android app.

---

## Why GitHub Instead of Firebase?

1. ✅ **You already have Claude Code on your phone** - Can edit and commit directly
2. ✅ **No setup required** - Just create a repo
3. ✅ **Version control** - Track all changes to exercises
4. ✅ **Free and unlimited** - No storage limits for text files
5. ✅ **Simpler** - No Firebase configuration needed

---

## Your Complete Workflow

```
┌─────────────────────────────────────────────┐
│  On Phone with Claude Code                  │
│                                              │
│  1. Open danish-exercises repo              │
│  2. Edit data/exercises/batch_002.json      │
│  3. git add + commit + push                 │
└───────────────┬─────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────┐
│  GitHub Repository                           │
│  github.com/yourusername/danish-exercises   │
│                                              │
│  data/                                       │
│  ├── exercises/                              │
│  │   ├── batch_001.json                     │
│  │   ├── batch_002.json  ← NEW!             │
│  │   └── batch_003.json                     │
│  └── texts/                                  │
│      └── texts_001.json                      │
└───────────────┬─────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────┐
│  "Dansk til Luis" App on Phone              │
│                                              │
│  1. Tap refresh button 🔄                   │
│  2. App fetches from GitHub                 │
│  3. Downloads new batch_002.json            │
│  4. Imports to local Room database          │
│  5. ✅ "10 new exercises downloaded!"       │
└─────────────────────────────────────────────┘
```

---

## Repository Structure

```
danish-exercises/
├── data/
│   ├── exercises/
│   │   ├── batch_001.json
│   │   ├── batch_002.json
│   │   └── batch_003.json
│   ├── texts/
│   │   └── texts_001.json
│   └── README.md  (instructions)
├── .gitignore
└── README.md  (repo description)
```

---

## How the App Syncs

### GitHub API Endpoints Used

The app uses GitHub's public API (no auth needed for public repos):

```
GET https://api.github.com/repos/{user}/{repo}/contents/data/exercises
GET https://raw.githubusercontent.com/{user}/{repo}/main/data/exercises/batch_001.json
```

### Sync Process

1. **App taps refresh button**
2. **Fetch file list** from `data/exercises/` folder
3. **Check local database** - which batches do we already have?
4. **Download new JSON files** from GitHub raw content
5. **Parse and import** exercises into Room database
6. **Update UI** - show count of new exercises

---

## Implementation Details

### GitHub Sync Manager

```kotlin
class GitHubSyncManager(private val context: Context) {

    private val repoOwner = "yourusername"
    private val repoName = "danish-exercises"
    private val branch = "main"

    suspend fun checkForUpdates(): Int {
        // 1. List files in data/exercises/
        val files = fetchExerciseFiles()

        // 2. Check which are new
        val existingBatches = database.syncStatusDao().getAllBatchIds()
        val newFiles = files.filter { !existingBatches.contains(it.batchId) }

        // 3. Download and import
        var totalNew = 0
        newFiles.forEach { file ->
            val json = downloadFileContent(file.downloadUrl)
            val batch = Gson().fromJson(json, ExerciseBatch::class.java)
            importBatch(batch)
            totalNew += batch.exercises.size
        }

        return totalNew
    }

    private suspend fun fetchExerciseFiles(): List<GitHubFile> {
        val url = "https://api.github.com/repos/$repoOwner/$repoName/contents/data/exercises"
        // HTTP GET request
        return parseGitHubResponse(response)
    }

    private suspend fun downloadFileContent(url: String): String {
        // Download raw JSON from GitHub
        return httpClient.get(url)
    }
}
```

---

## Setup Instructions

### Step 1: Create GitHub Repository

1. **On your phone**, open GitHub in browser or app
2. Create new repository: `danish-exercises`
3. Set to **Public** (so app can read without authentication)
4. Don't initialize with README (we already have one)

### Step 2: Push Initial Setup

```bash
cd "danish texts/androidApp"

# Create GitHub repo first, then:
git remote add origin https://github.com/yourusername/danish-exercises.git
git add data/
git commit -m "Initial exercises setup"
git branch -M main
git push -u origin main
```

### Step 3: Update App Configuration

In `app/src/main/java/.../services/GitHubConfig.kt`:
```kotlin
object GitHubConfig {
    const val REPO_OWNER = "yourusername"  // ← Change this
    const val REPO_NAME = "danish-exercises"
    const val BRANCH = "main"
}
```

---

## Adding New Exercises (Your Workflow)

### On Your Phone with Claude Code:

1. **Open repo** in Claude Code
2. **Create new file**: `data/exercises/batch_004.json`
3. **Write exercises** (use example as template)
4. **Commit and push**:
```bash
git add data/exercises/batch_004.json
git commit -m "Add 15 new greeting exercises"
git push
```

5. **Open "Dansk til Luis" app**
6. **Tap refresh button** 🔄
7. **See**: "✅ 15 new exercises downloaded!"
8. **Start practicing!**

---

## Advantages Over Firebase

| Feature | GitHub | Firebase |
|---------|--------|----------|
| Setup | Create repo (1 min) | Firebase project + config (10 min) |
| Phone workflow | Edit in Claude Code | Upload via web console |
| Version control | ✅ Built-in | ❌ No version history |
| Authentication | None needed (public) | Needs setup |
| Free tier | Unlimited | 1GB storage |
| Editing | Direct file edit | Must upload new file |

---

## Example Exercise Batch

**data/exercises/batch_002.json**:
```json
{
  "batch_id": "batch_002",
  "created_at": "2025-12-18",
  "version": 1,
  "exercises": [
    {
      "id": "ex_010",
      "type": "fill_in_blank",
      "question": "Translate: 'Thank you very much'",
      "text_id": null,
      "difficulty": "beginner",
      "data": {
        "correctAnswer": "Mange tak",
        "hint": "Mange = many, tak = thanks"
      }
    },
    {
      "id": "ex_011",
      "type": "multiple_choice",
      "question": "What does 'Undskyld' mean?",
      "text_id": null,
      "difficulty": "beginner",
      "data": {
        "options": [
          "Excuse me",
          "Thank you",
          "Hello",
          "Goodbye"
        ],
        "correctAnswerIndex": 0
      }
    }
  ]
}
```

---

## Private Repository Alternative

If you want to keep exercises private:

1. **Use Personal Access Token** (PAT)
2. **Store in app** (encrypted)
3. **Send in HTTP header**: `Authorization: token YOUR_PAT`

But for learning Danish, public repo is fine!

---

## Next Steps

1. ✅ Git repo initialized
2. ✅ Data structure created
3. ✅ Example exercises added
4. ⏳ Implement GitHub sync in app
5. ⏳ Create GitHub repository online
6. ⏳ Push to GitHub
7. ⏳ Test sync from phone

Ready to implement the sync functionality! 🚀
