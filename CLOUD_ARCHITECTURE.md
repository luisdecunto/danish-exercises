# Cloud Architecture - Dansk til Luis

## Overview

Cloud-first design for creating exercises on mobile and syncing to the app without a computer.

---

## Architecture Choice: Firebase

### Why Firebase?
1. **No server needed** - Fully managed by Google
2. **Mobile-friendly** - Works great on Android
3. **Free tier** - 1GB storage, 10GB/month downloads (plenty for text)
4. **Real-time sync** - New exercises appear automatically
5. **Easy upload** - JSON files via web interface or simple upload app
6. **Offline support** - App caches exercises locally

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    CLOUD (Firebase)                         │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Firebase Storage (File Storage)                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  exercises/                                          │  │
│  │  ├── exercises_batch_001.json                        │  │
│  │  ├── exercises_batch_002.json                        │  │
│  │  └── exercises_batch_003.json                        │  │
│  │                                                       │  │
│  │  texts/                                              │  │
│  │  ├── texts_batch_001.json                            │  │
│  │  └── texts_batch_002.json                            │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  Firestore Database (Metadata)                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Collection: exercise_batches                        │  │
│  │  {                                                    │  │
│  │    id: "batch_001",                                  │  │
│  │    filename: "exercises_batch_001.json",             │  │
│  │    count: 25,                                        │  │
│  │    uploaded_at: timestamp,                           │  │
│  │    version: 1                                        │  │
│  │  }                                                    │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          │ Download on first launch
                          │ Check for updates periodically
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                   ANDROID APP (Your Phone)                  │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Room Database (Local Cache)                                │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Tables:                                             │  │
│  │  - exercises (synced from cloud)                     │  │
│  │  - texts (synced from cloud)                         │  │
│  │  - user_progress (local only)                        │  │
│  │  - sync_status (tracks what's downloaded)            │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  SyncManager                                                │
│  - Checks Firebase for new batches on app start            │
│  - Downloads new JSON files                                 │
│  - Imports into local Room database                         │
│  - Handles conflicts (doesn't re-download existing)         │
└─────────────────────────────────────────────────────────────┘
```

---

## Your Mobile Workflow

### Step 1: Create Exercises (On Phone, No Computer)

Using Claude Android app or any text editor:

**exercises_batch_001.json**
```json
{
  "batch_id": "batch_001",
  "created_at": "2025-12-18",
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

**texts_batch_001.json**
```json
{
  "batch_id": "texts_001",
  "created_at": "2025-12-18",
  "texts": [
    {
      "id": 1,
      "title": "Danish Christmas Traditions",
      "content": "Jul er en vigtig højtid i Danmark...",
      "difficulty": "intermediate",
      "source": "Created by Claude"
    }
  ]
}
```

### Step 2: Upload to Firebase (3 Options)

#### Option A: Firebase Console Web Interface
1. Open Firebase Console on phone browser
2. Go to Storage
3. Upload JSON file
4. Done! ✅

#### Option B: Simple Upload Companion App
Create a tiny app just for uploading:
```kotlin
// Single screen with file picker
Button("Upload Exercises") {
    pickFile() // User selects JSON
    uploadToFirebase(file)
    showToast("Uploaded!")
}
```

#### Option C: Direct Integration in Claude Workflow
Save to Google Drive → Firebase auto-syncs

### Step 3: Sync to Main App (Automatic)

Your "Dansk til Luis" app:
1. Opens
2. Checks Firebase for new batches
3. Downloads JSON files
4. Imports into local database
5. Shows toast: "10 new exercises downloaded!"
6. Ready to practice offline

---

## Database Schema (Hybrid: Cloud + Local)

### Cloud (Firebase Storage)
```
Storage Structure:
/exercises/
  ├── batch_001.json
  ├── batch_002.json
  └── batch_003.json

/texts/
  ├── texts_001.json
  └── texts_002.json
```

### Local (Room Database)
```kotlin
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val type: String,
    val question: String,
    val textId: Int?,
    val difficulty: String,
    val dataJson: String,
    val batchId: String, // Track which batch it came from
    val syncedAt: Long   // When it was downloaded
)

@Entity(tableName = "texts")
data class TextEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val content: String,
    val difficulty: String,
    val source: String?,
    val batchId: String,
    val syncedAt: Long
)

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val exerciseId: String,
    val isCompleted: Boolean,
    val attempts: Int,
    val lastAnswer: String?,
    val completedAt: Long?,
    val updatedAt: Long
)

@Entity(tableName = "sync_status")
data class SyncStatusEntity(
    @PrimaryKey val batchId: String,
    val type: String, // "exercises" or "texts"
    val filename: String,
    val itemCount: Int,
    val downloadedAt: Long,
    val version: Int
)
```

---

## Sync Logic

### SyncManager.kt
```kotlin
class SyncManager(
    private val firebaseStorage: FirebaseStorage,
    private val database: AppDatabase
) {
    suspend fun checkForUpdates() {
        // 1. List all files in Firebase Storage
        val remoteFiles = firebaseStorage.reference
            .child("exercises")
            .listAll()
            .await()

        // 2. Check which ones we don't have
        val localBatches = database.syncStatusDao().getAllBatchIds()
        val newFiles = remoteFiles.items.filter {
            !localBatches.contains(it.name.removeSuffix(".json"))
        }

        // 3. Download and import new files
        newFiles.forEach { file ->
            downloadAndImport(file)
        }
    }

    private suspend fun downloadAndImport(file: StorageReference) {
        // Download JSON
        val json = file.getBytes(10_000_000).await().decodeToString()

        // Parse and import
        val batch = gson.fromJson(json, ExerciseBatch::class.java)
        database.exerciseDao().insertAll(batch.exercises)

        // Mark as synced
        database.syncStatusDao().insert(
            SyncStatusEntity(
                batchId = batch.batch_id,
                type = "exercises",
                filename = file.name,
                itemCount = batch.exercises.size,
                downloadedAt = System.currentTimeMillis(),
                version = 1
            )
        )
    }
}
```

---

## App Flow with Sync

```
App Launch
    ↓
MainActivity.onCreate()
    ↓
SyncManager.checkForUpdates()
    ↓
[Background] Download new JSON files from Firebase
    ↓
[Background] Import into local Room database
    ↓
[UI] Show toast: "5 new exercises downloaded!"
    ↓
User taps "Start Random Exercise"
    ↓
Query local database (fast, offline)
    ↓
Show exercise
```

---

## Firebase Setup (One-Time, 10 Minutes)

### 1. Create Firebase Project
1. Go to: https://console.firebase.google.com
2. Click "Add Project"
3. Name: "dansk-til-luis"
4. Follow wizard

### 2. Enable Storage
1. In Firebase Console → Storage
2. Click "Get Started"
3. Use default rules (can secure later)

### 3. Add Firebase to Android App
1. Download `google-services.json`
2. Place in `app/` folder
3. Add dependencies to `build.gradle`

### 4. Done! ✅

---

## Dependencies to Add

### build.gradle (project level)
```gradle
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.4.0'
    }
}
```

### app/build.gradle
```gradle
plugins {
    id 'com.google.gms.google-services'
}

dependencies {
    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-storage-ktx'
    implementation 'com.google.firebase:firebase-firestore-ktx'

    // Room (Local Database)
    implementation "androidx.room:room-runtime:2.6.1"
    implementation "androidx.room:room-ktx:2.6.1"
    ksp "androidx.room:room-compiler:2.6.1"

    // Coroutines
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3"
}
```

---

## Upload Helper App (Optional)

Simple companion app for uploading JSON files:

### UploadActivity.kt
```kotlin
class UploadActivity : AppCompatActivity() {
    private val storage = Firebase.storage
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { uploadFile(it) }
    }

    fun pickFile() {
        filePickerLauncher.launch("application/json")
    }

    private fun uploadFile(uri: Uri) {
        val filename = "exercises_${System.currentTimeMillis()}.json"
        val ref = storage.reference.child("exercises/$filename")

        ref.putFile(uri)
            .addOnSuccessListener {
                Toast.makeText(this, "Uploaded!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
```

---

## Advantages of This Architecture

1. ✅ **No computer needed** - Everything on phone
2. ✅ **Create anywhere** - Claude app, text editor, anywhere
3. ✅ **Easy updates** - Just upload new JSON file
4. ✅ **Offline-first** - App works without internet after first sync
5. ✅ **Scalable** - Can handle thousands of exercises
6. ✅ **Versioning** - Can track which batch each exercise came from
7. ✅ **Progress persists** - Saved locally, never lost
8. ✅ **Simple workflow** - Create → Upload → Sync → Practice

---

## Alternative: Even Simpler Approach

### Google Drive + Auto-Import

1. Save JSON files to Google Drive folder
2. App monitors that folder
3. Auto-imports new files
4. No Firebase needed!

**Pros:** Simpler, no Firebase setup
**Cons:** Less reliable, manual folder management

---

## Recommendation

**Use Firebase Storage + Room Database**

This gives you:
- Cloud storage for exercises (create on phone)
- Local caching for offline use
- Easy upload workflow
- Professional, scalable solution

Ready to implement this architecture? 🚀
