# Auto-Sync Implementation Guide

## Automatic Update Strategies

The app will check for new exercises automatically in three scenarios:

---

## Strategy 1: On App Launch (Always)

### Implementation

**MainActivity.kt**
```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var syncManager: SyncManager
    private var isSyncing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        syncManager = SyncManager(this)

        // Auto-check for updates on launch
        checkForUpdates()
    }

    private fun checkForUpdates() {
        if (isSyncing) return // Prevent duplicate syncs

        isSyncing = true
        showSyncIndicator(true) // Optional: Show loading indicator

        lifecycleScope.launch {
            try {
                val newExercises = syncManager.checkForUpdates()

                if (newExercises > 0) {
                    // Show success message
                    Snackbar.make(
                        binding.root,
                        "Downloaded $newExercises new exercises!",
                        Snackbar.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                // Fail silently or show error if no internet
                Log.e("MainActivity", "Sync failed", e)
            } finally {
                isSyncing = false
                showSyncIndicator(false)
                refreshExerciseList() // Reload UI with new exercises
            }
        }
    }

    private fun showSyncIndicator(show: Boolean) {
        // Optional: Show/hide a small loading indicator
        binding.syncProgressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}
```

**User Experience:**
```
Open App
    ↓
[Background] Checking for updates... (1-2 seconds)
    ↓
If new exercises found:
    Show: "Downloaded 15 new exercises!" (green snackbar)

If no updates:
    Silent, nothing shown

If no internet:
    Silent, uses cached exercises
```

---

## Strategy 2: Background Periodic Sync

### Implementation

**SyncWorker.kt**
```kotlin
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val syncManager = SyncManager(applicationContext)
            val newCount = syncManager.checkForUpdates()

            // Show notification if new exercises found
            if (newCount > 0) {
                showNotification(newCount)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Background sync failed", e)
            Result.retry() // Retry later
        }
    }

    private fun showNotification(count: Int) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("New Danish Exercises!")
            .setContentText("$count new exercises are ready to practice")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(createPendingIntent())
            .build()

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(applicationContext, MainActivity::class.java)
        return PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_ID = "exercise_sync"
        const val NOTIFICATION_ID = 1001
    }
}
```

**Schedule the Worker in Application.onCreate()**
```kotlin
class DanskApp : Application() {

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        schedulePeriodicSync()
    }

    private fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Only when online
            .setRequiresBatteryNotLow(true) // Don't drain battery
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            6, TimeUnit.HOURS, // Check every 6 hours
            15, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "exercise_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SyncWorker.CHANNEL_ID,
                "Exercise Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new exercises"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
```

**User Experience:**
```
[Background, every 6 hours]
    ↓
Check for new exercises
    ↓
If found:
    📱 Notification: "10 new Danish exercises available!"
    User taps → Opens app with new exercises

If not found:
    Silent, no notification
```

---

## Strategy 3: Manual Refresh Button

**Add to MainActivity**
```kotlin
// In MainActivity layout, add a refresh button
binding.refreshButton.setOnClickListener {
    checkForUpdates()
}

// Or swipe-to-refresh
binding.swipeRefreshLayout.setOnRefreshListener {
    checkForUpdates()
    binding.swipeRefreshLayout.isRefreshing = false
}
```

---

## Complete SyncManager Implementation

**SyncManager.kt**
```kotlin
class SyncManager(private val context: Context) {

    private val firebaseStorage = Firebase.storage
    private val database = AppDatabase.getInstance(context)
    private val gson = Gson()

    /**
     * Check for new exercises and download them
     * @return Number of new exercises downloaded
     */
    suspend fun checkForUpdates(): Int = withContext(Dispatchers.IO) {
        var totalNew = 0

        try {
            // 1. List all exercise batch files from Firebase
            val exerciseFiles = firebaseStorage.reference
                .child("exercises")
                .listAll()
                .await()

            // 2. Check which batches we already have
            val existingBatches = database.syncStatusDao()
                .getAllBatchIds()
                .toSet()

            // 3. Download only new batches
            exerciseFiles.items
                .filter { !existingBatches.contains(it.name.removeSuffix(".json")) }
                .forEach { file ->
                    val count = downloadAndImportExercises(file)
                    totalNew += count
                }

            // 4. Same for texts
            val textFiles = firebaseStorage.reference
                .child("texts")
                .listAll()
                .await()

            textFiles.items
                .filter { !existingBatches.contains(it.name.removeSuffix(".json")) }
                .forEach { file ->
                    downloadAndImportTexts(file)
                }

        } catch (e: Exception) {
            Log.e("SyncManager", "Sync failed", e)
            throw e
        }

        totalNew
    }

    private suspend fun downloadAndImportExercises(file: StorageReference): Int {
        // Download JSON
        val bytes = file.getBytes(10_000_000).await()
        val json = bytes.decodeToString()

        // Parse batch
        val batch = gson.fromJson(json, ExerciseBatch::class.java)

        // Import to database
        database.exerciseDao().insertAll(batch.exercises.map { it.toEntity() })

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

        return batch.exercises.size
    }

    private suspend fun downloadAndImportTexts(file: StorageReference) {
        // Similar to exercises
        val bytes = file.getBytes(10_000_000).await()
        val json = bytes.decodeToString()
        val batch = gson.fromJson(json, TextBatch::class.java)

        database.textDao().insertAll(batch.texts.map { it.toEntity() })

        database.syncStatusDao().insert(
            SyncStatusEntity(
                batchId = batch.batch_id,
                type = "texts",
                filename = file.name,
                itemCount = batch.texts.size,
                downloadedAt = System.currentTimeMillis(),
                version = 1
            )
        )
    }

    /**
     * Force a full re-sync (for debugging or resetting)
     */
    suspend fun forceFullSync() {
        database.syncStatusDao().clearAll()
        checkForUpdates()
    }
}
```

---

## Dependencies Needed

**app/build.gradle**
```gradle
dependencies {
    // WorkManager for background sync
    implementation "androidx.work:work-runtime-ktx:2.9.0"

    // Notifications
    implementation "androidx.core:core-ktx:1.12.0"
}
```

**AndroidManifest.xml**
```xml
<!-- Permissions -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<application>
    <!-- Register the Application class -->
    android:name=".DanskApp"
</application>
```

---

## Sync Timing Options

### Conservative (Saves Battery)
```kotlin
PeriodicWorkRequestBuilder<SyncWorker>(12, TimeUnit.HOURS)
```
- Checks twice a day
- Minimal battery usage

### Moderate (Recommended)
```kotlin
PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
```
- Checks 4 times a day
- Balanced

### Aggressive (Always Fresh)
```kotlin
PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
```
- Checks every hour
- More battery usage

---

## Smart Sync Strategy

**Best approach:** Combine all three!

```kotlin
// 1. On app launch - Always
onCreate() → checkForUpdates()

// 2. Background - Every 6 hours
WorkManager → Periodic check

// 3. Manual - Pull to refresh
SwipeRefreshLayout → User can force sync

// 4. Smart detection
if (lastSyncTime > 24.hours) {
    // Force sync if haven't checked in a day
    checkForUpdates()
}
```

---

## User Settings (Optional)

Let users control sync behavior:

```kotlin
// In Settings screen
preferences:
- Auto-sync on launch: ON/OFF
- Background sync: ON/OFF
- Sync frequency: 1h / 6h / 12h / 24h
- WiFi only: ON/OFF
- Show notifications: ON/OFF
```

---

## Error Handling

```kotlin
try {
    syncManager.checkForUpdates()
} catch (e: IOException) {
    // No internet - fail silently
    Log.d("Sync", "No internet, using cached exercises")
} catch (e: Exception) {
    // Other error - show message
    Toast.makeText(context, "Sync failed, try again later", Toast.LENGTH_SHORT).show()
}
```

---

## Summary

### Automatic Updates Happen:
1. ✅ **Every app launch** - Quick check (1-2 sec)
2. ✅ **Every 6 hours in background** - Even when app closed
3. ✅ **User can force refresh** - Pull to refresh or button
4. ✅ **Silent if no updates** - Doesn't bother user
5. ✅ **Notification if new exercises** - "10 new exercises!"
6. ✅ **Works offline** - Uses cached exercises

### Battery Friendly:
- Only downloads when on WiFi (optional)
- Waits for battery not low
- Smart caching prevents re-downloading
- Minimal network usage

### You Never Think About It:
```
Upload JSON to Firebase
    ↓
[App auto-detects within 6 hours]
    ↓
Downloads in background
    ↓
📱 Notification: "New exercises ready!"
    ↓
Open app → Start practicing!
```

**No manual sync needed!** 🎉
