# Manual Sync Implementation - Refresh Button

## Overview

User-controlled sync via refresh button in the app. You upload exercises, then tap the refresh button to download them.

---

## User Flow

```
1. Create exercises in Claude app
2. Upload JSON to Firebase (or GitHub)
3. Open "Dansk til Luis" app
4. Tap refresh icon 🔄
5. App downloads new exercises
6. Toast: "15 new exercises downloaded!"
7. Start practicing
```

---

## UI Design

### MainActivity with Refresh Button

```
┌─────────────────────────────────────────┐
│  Dansk til Luis              🔄         │  ← Refresh icon in toolbar
├─────────────────────────────────────────┤
│                                         │
│  📊 Statistics                          │
│  ┌─────────────────────────────────┐   │
│  │ Total: 150                      │   │
│  │ Completed: 45 (30%)             │   │
│  │ [Progress Bar ========>    ]   │   │
│  └─────────────────────────────────┘   │
│                                         │
│  [Start Random Exercise]                │
│                                         │
│  [Browse Exercises]                     │
│                                         │
└─────────────────────────────────────────┘
```

When user taps 🔄:
```
┌─────────────────────────────────────────┐
│  Dansk til Luis              ⟳         │  ← Animating spinner
├─────────────────────────────────────────┤
│                                         │
│  Checking for updates...                │
│  [Progress indicator]                   │
│                                         │
```

After download:
```
┌─────────────────────────────────────────┐
│  ✅ 15 new exercises downloaded!        │  ← Green snackbar
├─────────────────────────────────────────┤
```

---

## Implementation

### Step 1: Add Refresh Menu Item

**res/menu/main_menu.xml**
```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <item
        android:id="@+id/action_refresh"
        android:icon="@drawable/ic_refresh"
        android:title="Refresh"
        app:showAsAction="always" />

</menu>
```

**Create refresh icon drawable:**

**res/drawable/ic_refresh.xml**
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorOnPrimary">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M17.65,6.35C16.2,4.9 14.21,4 12,4c-4.42,0 -7.99,3.58 -7.99,8s3.57,8 7.99,8c3.73,0 6.84,-2.55 7.73,-6h-2.08c-0.82,2.33 -3.04,4 -5.65,4 -3.31,0 -6,-2.69 -6,-6s2.69,-6 6,-6c1.66,0 3.14,0.69 4.22,1.78L13,11h7V4l-2.35,2.35z"/>
</vector>
```

### Step 2: Handle Menu Click in MainActivity

**MainActivity.kt**
```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var syncManager: SyncManager
    private var isSyncing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        syncManager = SyncManager(this)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                refreshExercises(item)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun refreshExercises(menuItem: MenuItem) {
        if (isSyncing) {
            Toast.makeText(this, "Already syncing...", Toast.LENGTH_SHORT).show()
            return
        }

        isSyncing = true

        // Start rotation animation on icon
        startRefreshAnimation(menuItem)

        lifecycleScope.launch {
            try {
                val newCount = syncManager.checkForUpdates()

                // Show result
                if (newCount > 0) {
                    Snackbar.make(
                        binding.root,
                        "✅ Downloaded $newCount new exercises!",
                        Snackbar.LENGTH_LONG
                    ).setBackgroundTint(getColor(R.color.correct_green))
                        .show()

                    // Refresh the UI
                    refreshExerciseList()
                } else {
                    Snackbar.make(
                        binding.root,
                        "You're up to date! No new exercises.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

            } catch (e: IOException) {
                // No internet connection
                Snackbar.make(
                    binding.root,
                    "❌ No internet connection",
                    Snackbar.LENGTH_LONG
                ).setBackgroundTint(getColor(R.color.incorrect_red))
                    .show()

            } catch (e: Exception) {
                // Other error
                Snackbar.make(
                    binding.root,
                    "❌ Sync failed: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).setBackgroundTint(getColor(R.color.incorrect_red))
                    .show()

                Log.e("MainActivity", "Sync error", e)

            } finally {
                isSyncing = false
                stopRefreshAnimation(menuItem)
            }
        }
    }

    private fun startRefreshAnimation(menuItem: MenuItem) {
        val refreshView = findViewById<View>(R.id.action_refresh)
        refreshView?.let {
            val rotate = ObjectAnimator.ofFloat(it, "rotation", 0f, 360f).apply {
                duration = 1000
                repeatCount = ObjectAnimator.INFINITE
                interpolator = LinearInterpolator()
            }
            it.setTag(R.id.action_refresh, rotate)
            rotate.start()
        }
    }

    private fun stopRefreshAnimation(menuItem: MenuItem) {
        val refreshView = findViewById<View>(R.id.action_refresh)
        refreshView?.let {
            val animator = it.getTag(R.id.action_refresh) as? ObjectAnimator
            animator?.cancel()
            it.rotation = 0f
        }
    }

    private fun refreshExerciseList() {
        // Reload exercises from database and update UI
        lifecycleScope.launch {
            val exercises = database.exerciseDao().getAllExercises()
            // Update RecyclerView or stats display
            updateStatistics(exercises)
        }
    }

    private fun updateStatistics(exercises: List<ExerciseEntity>) {
        val total = exercises.size
        val completed = database.progressDao().getCompletedCount()
        val percentage = if (total > 0) (completed * 100) / total else 0

        binding.statsTextView.text = "Completed: $completed / $total ($percentage%)"
    }
}
```

### Step 3: Add Toolbar to Layout

**res/layout/activity_main.xml**
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Toolbar with refresh button -->
    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="?attr/colorPrimary"
        android:elevation="4dp"
        app:layout_constraintTop_toTopOf="parent"
        app:popupTheme="@style/ThemeOverlay.AppCompat.Light"
        app:title="Dansk til Luis"
        app:titleTextColor="@android:color/white" />

    <!-- Statistics Card -->
    <com.google.android.material.card.MaterialCardView
        android:id="@+id/statsCard"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        app:cardCornerRadius="8dp"
        app:cardElevation="4dp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/toolbar">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <TextView
                android:id="@+id/statsTextView"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Completed: 0 / 0"
                android:textSize="18sp"
                android:textStyle="bold" />

            <ProgressBar
                android:id="@+id/statsProgressBar"
                style="?android:attr/progressBarStyleHorizontal"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:max="100"
                android:progress="0" />

        </LinearLayout>

    </com.google.android.material.card.MaterialCardView>

    <!-- Main content -->
    <Button
        android:id="@+id/startRandomButton"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:text="Start Random Exercise"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/statsCard" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### Step 4: Update Theme to Support Toolbar

**res/values/themes.xml**
```xml
<style name="Theme.DanskTilLuis" parent="Theme.MaterialComponents.DayNight.NoActionBar">
    <item name="colorPrimary">@color/purple_500</item>
    <item name="colorPrimaryVariant">@color/purple_700</item>
    <item name="colorOnPrimary">@color/white</item>
    <!-- ... other colors ... -->
</style>
```

---

## Alternative: Floating Action Button

If you prefer a FAB instead of toolbar button:

**activity_main.xml**
```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:id="@+id/fabRefresh"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_margin="16dp"
    android:contentDescription="Refresh exercises"
    android:src="@drawable/ic_refresh"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent" />
```

**MainActivity.kt**
```kotlin
binding.fabRefresh.setOnClickListener {
    refreshExercises()
}

private fun refreshExercises() {
    // Same logic as menu item
    // Show loading on FAB
    binding.fabRefresh.isEnabled = false

    lifecycleScope.launch {
        try {
            val newCount = syncManager.checkForUpdates()
            // ... show result
        } finally {
            binding.fabRefresh.isEnabled = true
        }
    }
}
```

---

## Alternative: Pull-to-Refresh

**activity_main.xml**
```xml
<androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    android:id="@+id/swipeRefreshLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Your content here -->

</androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
```

**MainActivity.kt**
```kotlin
binding.swipeRefreshLayout.setOnRefreshListener {
    lifecycleScope.launch {
        try {
            syncManager.checkForUpdates()
        } finally {
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
}
```

---

## SyncManager (Same as Before)

**SyncManager.kt**
```kotlin
class SyncManager(private val context: Context) {

    private val firebaseStorage = Firebase.storage
    private val database = AppDatabase.getInstance(context)

    suspend fun checkForUpdates(): Int = withContext(Dispatchers.IO) {
        var totalNew = 0

        // List files from Firebase
        val exerciseFiles = firebaseStorage.reference
            .child("exercises")
            .listAll()
            .await()

        // Check which are new
        val existingBatches = database.syncStatusDao().getAllBatchIds().toSet()

        // Download new ones
        exerciseFiles.items
            .filter { !existingBatches.contains(it.name.removeSuffix(".json")) }
            .forEach { file ->
                val count = downloadAndImportExercises(file)
                totalNew += count
            }

        totalNew
    }

    private suspend fun downloadAndImportExercises(file: StorageReference): Int {
        val bytes = file.getBytes(10_000_000).await()
        val json = bytes.decodeToString()
        val batch = Gson().fromJson(json, ExerciseBatch::class.java)

        database.exerciseDao().insertAll(batch.exercises.map { it.toEntity() })

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

        return batch.exercises.size
    }
}
```

---

## Summary

### User Experience:

```
Upload exercises to Firebase
    ↓
Open app
    ↓
Tap refresh icon 🔄 in toolbar
    ↓
[Icon spins while downloading]
    ↓
✅ "15 new exercises downloaded!"
    ↓
Start practicing!
```

### Features:
- ✅ **Manual control** - User decides when to sync
- ✅ **Visual feedback** - Spinning icon + snackbar messages
- ✅ **Error handling** - Shows if no internet or sync fails
- ✅ **No background battery drain** - Only syncs when requested
- ✅ **Simple UX** - One tap to update

### Benefits:
- 📱 **No permissions needed** - No background sync permissions
- 🔋 **Battery friendly** - Only runs when user wants
- 🎯 **Predictable** - User knows exactly when updates happen
- 🚀 **Fast** - Instant feedback when tapping button

This is simpler and gives you full control! 🎉
