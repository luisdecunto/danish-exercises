# Implementation Plan - Dansk til Luis

## Current Status: ✅ Skeleton Complete

### What Works Now
- ✅ Three exercise types (Multiple Choice, Fill in Blank, Match Pairs)
- ✅ Enter key submits fill-in-blank exercises
- ✅ Basic UI with 9 sample exercises
- ✅ Progress tracking (SharedPreferences)
- ✅ Visual feedback (correct/incorrect)

---

## Architecture Decisions Made

### 1. Database Strategy
- **Choice:** Firebase Storage + Room Database (local cache)
- **Why:** Mobile-first, easy upload from phone, offline support
- **Alternative considered:** GitHub (rejected - too complex for mobile)

### 2. Sync Strategy
- **Choice:** Manual refresh button
- **Why:** User control, no battery drain, simple UX
- **Location:** Refresh icon (🔄) in toolbar

### 3. UI Structure
- **Choice:** Navigation Drawer (slide from left)
- **Why:** Clean organization, standard Android pattern
- **Menu items:** Dashboard, Random Exercise, Browse, Statistics, Settings, About

---

## Phase 1: Database Implementation

### Priority 1: Core Database (CRITICAL)

**Files to create:**
```
app/src/main/java/.../database/
├── AppDatabase.kt
├── entities/
│   ├── ExerciseEntity.kt
│   ├── TextEntity.kt
│   ├── UserProgressEntity.kt
│   └── SyncStatusEntity.kt
└── dao/
    ├── ExerciseDao.kt
    ├── TextDao.kt
    ├── ProgressDao.kt
    └── SyncStatusDao.kt
```

**What it does:**
- Stores exercises locally (Room database)
- Tracks user progress
- Remembers which batches are downloaded

**Dependencies to add:**
```gradle
// Room
implementation "androidx.room:room-runtime:2.6.1"
implementation "androidx.room:room-ktx:2.6.1"
ksp "androidx.room:room-compiler:2.6.1"
```

---

### Priority 2: Firebase Setup (CRITICAL)

**Steps:**
1. Create Firebase project at console.firebase.google.com
2. Enable Storage
3. Download `google-services.json`
4. Add to `app/` folder
5. Add dependencies

**Dependencies to add:**
```gradle
// Firebase
implementation platform('com.google.firebase:firebase-bom:32.7.0')
implementation 'com.google.firebase:firebase-storage-ktx'

// Coroutines
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3"
```

---

### Priority 3: Sync Manager (CRITICAL)

**File to create:**
```
app/src/main/java/.../services/SyncManager.kt
```

**What it does:**
- Checks Firebase for new JSON files
- Downloads them
- Imports into Room database
- Returns count of new exercises

**Function:**
```kotlin
suspend fun checkForUpdates(): Int {
    // List Firebase files
    // Compare with local database
    // Download new ones
    // Import to Room
    // Return count
}
```

---

### Priority 4: Exercise Loader (CRITICAL)

**File to create:**
```
app/src/main/java/.../services/ExerciseLoaderService.kt
```

**What it does:**
- Gets random incomplete exercise from Room
- Loads associated text (if any)
- Returns Exercise object for display

**Function:**
```kotlin
suspend fun loadRandomExercise(): Exercise? {
    // Query incomplete exercises
    // Pick random
    // Load text if text_id exists
    // Return exercise object
}
```

---

## Phase 2: UI Updates

### Priority 1: Navigation Drawer (HIGH)

**Files to create/modify:**
```
res/menu/drawer_menu.xml
res/layout/nav_header.xml
res/drawable/ic_menu.xml (and other icons)
```

**Update:**
- `activity_main.xml` - Wrap in DrawerLayout
- `MainActivity.kt` - Handle menu clicks

**Menu items:**
- Dashboard
- Random Exercise
- Browse Exercises
- Statistics
- Settings
- About

---

### Priority 2: Refresh Button (HIGH)

**Files to create/modify:**
```
res/menu/main_menu.xml
res/drawable/ic_refresh.xml
```

**Update:**
- `MainActivity.kt` - Add refresh menu handler
- Trigger `SyncManager.checkForUpdates()`
- Show spinning animation
- Display result snackbar

---

### Priority 3: Text Display (MEDIUM)

**File to create:**
```
app/src/main/java/.../ui/TextDisplayFragment.kt
res/layout/fragment_text_display.xml
```

**What it does:**
- Shows text article (if exercise has one)
- Collapsible card
- User can expand to read full text

---

### Priority 4: Statistics Dashboard (MEDIUM)

**Update MainActivity to show:**
- Total exercises
- Completed count
- Progress percentage
- Progress bar

**Add query to ProgressDao:**
```kotlin
@Query("SELECT COUNT(*) FROM user_progress WHERE is_completed = 1")
suspend fun getCompletedCount(): Int
```

---

## Phase 3: Data Population

### Your Workflow (Once database is ready)

1. **Create exercises in Claude app**
2. **Save as JSON:**
```json
{
  "batch_id": "batch_001",
  "exercises": [
    {
      "id": "ex_001",
      "type": "multiple_choice",
      "question": "What is 'Hello' in Danish?",
      "data": {
        "options": ["Hej", "Tak", "Farvel"],
        "correctAnswerIndex": 0
      }
    }
  ]
}
```
3. **Upload to Firebase Storage** (via web browser)
4. **Open app, tap refresh button 🔄**
5. **Start practicing!**

---

## Implementation Order

### Week 1: Database Foundation
- [ ] Add Room dependencies
- [ ] Create database entities
- [ ] Create DAOs
- [ ] Create AppDatabase
- [ ] Test with sample data

### Week 2: Firebase Integration
- [ ] Create Firebase project
- [ ] Add Firebase dependencies
- [ ] Create SyncManager
- [ ] Test upload/download
- [ ] Create ExerciseLoaderService

### Week 3: UI Updates
- [ ] Add Navigation Drawer
- [ ] Add Refresh button
- [ ] Update MainActivity with stats
- [ ] Add Text Display fragment
- [ ] Connect everything

### Week 4: Polish & Testing
- [ ] Test all exercise types
- [ ] Test progress tracking
- [ ] Test sync with real data
- [ ] Fix bugs
- [ ] Add error handling

---

## File Structure (Final)

```
app/src/main/java/.../dansktilluis/
├── database/
│   ├── AppDatabase.kt
│   ├── entities/
│   │   ├── ExerciseEntity.kt
│   │   ├── TextEntity.kt
│   │   ├── UserProgressEntity.kt
│   │   └── SyncStatusEntity.kt
│   └── dao/
│       ├── ExerciseDao.kt
│       ├── TextDao.kt
│       ├── ProgressDao.kt
│       └── SyncStatusDao.kt
├── models/
│   ├── Exercise.kt ✅ (exists)
│   ├── ExerciseSet.kt ✅ (exists)
│   ├── Text.kt (new)
│   └── ExerciseBatch.kt (new - for JSON import)
├── services/
│   ├── SyncManager.kt (new)
│   └── ExerciseLoaderService.kt (new)
├── storage/
│   └── ProgressManager.kt ✅ (exists - will be replaced by Room)
├── ui/
│   ├── MainActivity.kt ✅ (exists - needs updates)
│   ├── ExerciseActivity.kt ✅ (exists - needs updates)
│   ├── ExerciseSetAdapter.kt ✅ (exists)
│   ├── MultipleChoiceFragment.kt ✅ (exists)
│   ├── FillInTheBlankFragment.kt ✅ (exists with Enter key)
│   ├── MatchPairsFragment.kt ✅ (exists)
│   └── TextDisplayFragment.kt (new)
└── utils/
    └── JsonParser.kt (new - optional)
```

---

## Dependencies Summary

**Add to app/build.gradle:**
```gradle
plugins {
    id 'com.google.gms.google-services' // For Firebase
    id 'com.google.devtools.ksp' version '1.9.22-1.0.17' // For Room
}

dependencies {
    // Room Database
    implementation "androidx.room:room-runtime:2.6.1"
    implementation "androidx.room:room-ktx:2.6.1"
    ksp "androidx.room:room-compiler:2.6.1"

    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-storage-ktx'

    // Coroutines
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3"

    // Gson (already have)
    implementation 'com.google.code.gson:gson:2.10.1'

    // Existing dependencies...
}
```

**Add to build.gradle (project level):**
```gradle
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.4.0'
    }
}
```

---

## Documentation Available

1. ✅ [WORKFLOW_ARCHITECTURE.md](WORKFLOW_ARCHITECTURE.md) - Overall design
2. ✅ [CLOUD_ARCHITECTURE.md](CLOUD_ARCHITECTURE.md) - Firebase details
3. ✅ [MANUAL_SYNC_IMPLEMENTATION.md](MANUAL_SYNC_IMPLEMENTATION.md) - Refresh button
4. ✅ [NAVIGATION_DRAWER_IMPLEMENTATION.md](NAVIGATION_DRAWER_IMPLEMENTATION.md) - Side menu
5. ✅ [AUTO_SYNC_IMPLEMENTATION.md](AUTO_SYNC_IMPLEMENTATION.md) - Alternative approach
6. ✅ [SETUP_GUIDE.md](SETUP_GUIDE.md) - How to run the app
7. ✅ [ADDING_EXERCISES.md](ADDING_EXERCISES.md) - Current manual method
8. ✅ [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) - Complete overview

---

## Next Steps

### Option A: Start Implementation Now
**I can start coding Phase 1 (Database + Firebase + Sync)**

Benefits:
- Get the core working
- You can test with real data
- Foundation for everything else

### Option B: Review Architecture First
**Review the docs, ask questions, make adjustments**

Benefits:
- Ensure everything makes sense
- Catch issues early
- Confirm Firebase is the right choice

### Option C: Test Current Skeleton First
**Use the working app with sample exercises**

Benefits:
- Understand current functionality
- Identify what's most important
- Prioritize features

---

## Your Mobile Workflow (Final Vision)

```
┌────────────────────────────────────────┐
│ 1. Create exercises in Claude app     │
│    Save as JSON                        │
└───────────────┬────────────────────────┘
                │
                ▼
┌────────────────────────────────────────┐
│ 2. Open Firebase Console in browser   │
│    Upload JSON file (drag & drop)     │
└───────────────┬────────────────────────┘
                │
                ▼
┌────────────────────────────────────────┐
│ 3. Open "Dansk til Luis" app          │
│    Tap 🔄 refresh button               │
└───────────────┬────────────────────────┘
                │
                ▼
┌────────────────────────────────────────┐
│ 4. ✅ "15 new exercises downloaded!"   │
│    Start practicing                    │
└────────────────────────────────────────┘
```

**No computer needed! Everything on phone!** 📱

---

## Ready to Proceed?

What would you like to do next?

1. **Start Phase 1** - Implement database and Firebase
2. **Ask questions** - Clarify anything
3. **Test current app** - Make sure skeleton works first
4. **Something else** - Your choice!

Let me know! 🚀
