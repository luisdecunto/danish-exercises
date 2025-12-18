# GitHub Architecture - Dansk til Luis

## Overview

**Separation of Concerns:**
- **GitHub Repository** (`danish-exercises`): Exercise data ONLY (public)
- **Android App**: Stays local on your computer/phone (NOT on GitHub)

The app downloads JSON files from GitHub and processes them locally.

---

## Architecture Diagram

```
┌──────────────────────────────────────────────────┐
│  Claude Code on Phone                             │
│  ────────────────────                             │
│  Edit: data/exercises/batch_002.json              │
│  Commit & Push to GitHub                          │
└──────────────┬───────────────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────────────┐
│  GitHub Repository (PUBLIC)                       │
│  github.com/luisdecunto/danish-exercises          │
│  ─────────────────────────────────────            │
│                                                    │
│  📁 data/                                          │
│     ├── exercises/                                │
│     │   ├── example_batch_001.json                │
│     │   ├── batch_002.json      ← NEW!            │
│     │   └── batch_003.json                        │
│     └── texts/                                    │
│         └── texts_001.json                        │
│                                                    │
│  📄 README.md                                      │
│  📄 GITHUB_ARCHITECTURE.md                         │
└──────────────┬───────────────────────────────────┘
               │
               │ HTTPS Download
               │
               ▼
┌──────────────────────────────────────────────────┐
│  Dansk til Luis Android App (LOCAL)               │
│  ───────────────────────────────────              │
│                                                    │
│  📱 User Action:                                   │
│     Tap refresh button 🔄                         │
│                                                    │
│  🔄 Sync Process:                                  │
│     1. Fetch file list from GitHub API            │
│     2. Compare with local Room database           │
│     3. Download new JSON files                    │
│     4. Parse exercises                            │
│     5. Import to Room database                    │
│                                                    │
│  💾 Local Storage (Room Database):                 │
│     ├── exercises (synced from GitHub)            │
│     ├── texts (synced from GitHub)                │
│     └── user_progress (local only)                │
│                                                    │
│  ✅ Result: "10 new exercises downloaded!"        │
└────────────────────────────────────────────────────┘
```

---

## Why This Architecture?

### GitHub Repository (Public)
- ✅ **Only data** - No app code exposed
- ✅ **Easy editing** - Claude Code on phone
- ✅ **Version control** - Track all changes
- ✅ **Free hosting** - Unlimited storage for text
- ✅ **No authentication** - Public read access

### Android App (Local)
- ✅ **Privacy** - App code stays private
- ✅ **Processing power** - All logic runs on phone
- ✅ **Offline capable** - Caches exercises locally
- ✅ **Fast** - No server needed, direct GitHub API

---

## GitHub API Usage

### 1. List Files in Directory

**Endpoint:**
```
GET https://api.github.com/repos/luisdecunto/danish-exercises/contents/data/exercises
```

**Response:**
```json
[
  {
    "name": "example_batch_001.json",
    "path": "data/exercises/example_batch_001.json",
    "sha": "abc123...",
    "size": 1234,
    "url": "...",
    "download_url": "https://raw.githubusercontent.com/..."
  },
  {
    "name": "batch_002.json",
    "path": "data/exercises/batch_002.json",
    ...
  }
]
```

### 2. Download File Content

**Endpoint:**
```
GET https://raw.githubusercontent.com/luisdecunto/danish-exercises/main/data/exercises/batch_001.json
```

**Returns:** Raw JSON content directly

---

## Android App Implementation

### Dependencies Needed

**app/build.gradle:**
```gradle
dependencies {
    // Room Database (local storage)
    implementation "androidx.room:room-runtime:2.6.1"
    implementation "androidx.room:room-ktx:2.6.1"
    ksp "androidx.room:room-compiler:2.6.1"

    // HTTP Client
    implementation "com.squareup.okhttp3:okhttp:4.12.0"

    // JSON Parsing
    implementation "com.google.code.gson:gson:2.10.1"

    // Coroutines
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
}
```

### GitHub Sync Service

**GitHubSyncService.kt:**
```kotlin
class GitHubSyncService {
    private val client = OkHttpClient()
    private val gson = Gson()

    companion object {
        const val REPO_OWNER = "luisdecunto"
        const val REPO_NAME = "danish-exercises"
        const val BRANCH = "main"
    }

    suspend fun fetchExerciseFiles(): List<GitHubFile> {
        val url = "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/contents/data/exercises"
        val request = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            val json = response.body?.string() ?: "[]"
            gson.fromJson(json, Array<GitHubFile>::class.java).toList()
        }
    }

    suspend fun downloadExerciseBatch(downloadUrl: String): ExerciseBatch {
        val request = Request.Builder().url(downloadUrl).build()

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            val json = response.body?.string() ?: "{}"
            gson.fromJson(json, ExerciseBatch::class.java)
        }
    }
}
```

### Sync Manager

**SyncManager.kt:**
```kotlin
class SyncManager(
    private val context: Context,
    private val database: AppDatabase
) {
    private val githubService = GitHubSyncService()

    suspend fun syncExercises(): Int {
        // 1. Fetch file list from GitHub
        val remoteFiles = githubService.fetchExerciseFiles()

        // 2. Check which batches we already have
        val localBatchIds = database.syncStatusDao().getAllBatchIds().toSet()

        // 3. Filter new files
        val newFiles = remoteFiles.filter { file ->
            val batchId = file.name.removeSuffix(".json")
            !localBatchIds.contains(batchId)
        }

        // 4. Download and import each new batch
        var totalNew = 0
        newFiles.forEach { file ->
            val batch = githubService.downloadExerciseBatch(file.download_url)
            importBatch(batch)
            totalNew += batch.exercises.size
        }

        return totalNew
    }

    private suspend fun importBatch(batch: ExerciseBatch) {
        // Import exercises to Room database
        database.exerciseDao().insertAll(batch.exercises.map { it.toEntity() })

        // Track sync status
        database.syncStatusDao().insert(
            SyncStatusEntity(
                batchId = batch.batch_id,
                downloadedAt = System.currentTimeMillis(),
                itemCount = batch.exercises.size
            )
        )
    }
}
```

### MainActivity Integration

**MainActivity.kt:**
```kotlin
private fun refreshExercises() {
    lifecycleScope.launch {
        try {
            // Show loading
            showLoading(true)

            // Sync with GitHub
            val syncManager = SyncManager(this@MainActivity, database)
            val newCount = syncManager.syncExercises()

            // Show result
            if (newCount > 0) {
                Snackbar.make(
                    binding.root,
                    "✅ Downloaded $newCount new exercises!",
                    Snackbar.LENGTH_LONG
                ).show()

                // Reload exercises
                loadExerciseSets()
            } else {
                Snackbar.make(
                    binding.root,
                    "You're up to date!",
                    Snackbar.LENGTH_SHORT
                ).show()
            }

        } catch (e: Exception) {
            Snackbar.make(
                binding.root,
                "❌ Sync failed: ${e.message}",
                Snackbar.LENGTH_LONG
            ).show()
        } finally {
            showLoading(false)
        }
    }
}
```

---

## Your Workflow

### Creating Exercises (Claude Code on Phone)

1. **Open repository** in Claude Code
2. **Navigate to** `data/exercises/`
3. **Create new file** `batch_002.json`:
   ```json
   {
     "batch_id": "batch_002",
     "created_at": "2025-12-18",
     "version": 1,
     "exercises": [
       {
         "id": "ex_010",
         "type": "fill_in_blank",
         "question": "What is 'water' in Danish?",
         "data": {
           "correctAnswer": "vand"
         }
       }
     ]
   }
   ```
4. **Commit and push**:
   ```bash
   git add data/exercises/batch_002.json
   git commit -m "Add 15 new vocabulary exercises"
   git push
   ```
5. **Open app** → Tap refresh 🔄 → Practice!

---

## Data Flow

```
GitHub (Source of Truth)
    ↓
  Download
    ↓
Room Database (Local Cache)
    ↓
App UI (Display Exercises)
    ↓
User Progress (Saved Locally)
```

**Key Points:**
- ✅ GitHub has ALL exercises
- ✅ App downloads and caches locally
- ✅ Progress tracking happens ONLY on device
- ✅ No user data sent to GitHub

---

## Security & Privacy

### Public Repository
- ✅ Safe: Only contains exercise data
- ✅ No user information
- ✅ No API keys or secrets
- ✅ Anyone can use the exercises (that's okay!)

### Local App
- ✅ Private: App code NOT on GitHub
- ✅ User progress stays on device
- ✅ No analytics or tracking
- ✅ Complete offline capability

---

## Benefits of This Architecture

1. **Simple** - No server to maintain
2. **Free** - GitHub hosting is free
3. **Fast** - Direct CDN access to files
4. **Reliable** - GitHub's infrastructure
5. **Versioned** - Git history of all changes
6. **Portable** - Easy to clone and edit
7. **Offline** - App caches everything locally

---

## Next Steps

1. ✅ GitHub repo created (data only)
2. ✅ Example exercises added
3. ⏳ Implement GitHub sync in Android app
4. ⏳ Add Room database for local storage
5. ⏳ Connect refresh button to sync
6. ⏳ Test full workflow on phone

Ready to implement the sync functionality in the app! 🚀
