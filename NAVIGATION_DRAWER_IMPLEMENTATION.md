# Navigation Drawer Implementation

## Overview

Slide-out menu from the left side with app sections and settings.

---

## UI Design

### Closed State
```
┌─────────────────────────────────────┐
│ ☰ Dansk til Luis            🔄     │  ← Hamburger icon to open
├─────────────────────────────────────┤
│  📊 Stats: 45/150                   │
│  [Start Random Exercise]            │
└─────────────────────────────────────┘
```

### Open State (Swipe from left or tap ☰)
```
┌──────────────────┐────────────────────┐
│                  │ Dansk til Luis  🔄 │
│  📚 Dashboard    │                    │
│                  │ Stats: 45/150      │
│  🎲 Random       │ [Start Random]     │
│     Exercise     │                    │
│                  │                    │
│  📖 Browse       │                    │
│     Exercises    │                    │
│                  │                    │
│  📊 Statistics   │                    │
│                  │                    │
│  ⚙️  Settings    │                    │
│                  │                    │
│  ℹ️  About       │                    │
│                  │                    │
└──────────────────┘────────────────────┘
     ↑ Menu items       ↑ Main content
```

---

## Implementation

### Step 1: Add DrawerLayout to MainActivity

**res/layout/activity_main.xml**
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawerLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <!-- Main content -->
    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <!-- Toolbar -->
        <com.google.android.material.appbar.MaterialToolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:background="?attr/colorPrimary"
            android:elevation="4dp"
            app:layout_constraintTop_toTopOf="parent"
            app:navigationIcon="@drawable/ic_menu"
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

        <!-- Main button -->
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

    <!-- Navigation Drawer -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navigationView"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        android:fitsSystemWindows="true"
        app:headerLayout="@layout/nav_header"
        app:menu="@menu/drawer_menu" />

</androidx.drawerlayout.widget.DrawerLayout>
```

### Step 2: Create Drawer Menu

**res/menu/drawer_menu.xml**
```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">

    <group android:checkableBehavior="single">
        <item
            android:id="@+id/nav_dashboard"
            android:icon="@drawable/ic_home"
            android:title="Dashboard" />

        <item
            android:id="@+id/nav_random_exercise"
            android:icon="@drawable/ic_shuffle"
            android:title="Random Exercise" />

        <item
            android:id="@+id/nav_browse"
            android:icon="@drawable/ic_list"
            android:title="Browse Exercises" />

        <item
            android:id="@+id/nav_statistics"
            android:icon="@drawable/ic_chart"
            android:title="Statistics" />
    </group>

    <item android:title="Settings">
        <menu>
            <item
                android:id="@+id/nav_settings"
                android:icon="@drawable/ic_settings"
                android:title="Settings" />

            <item
                android:id="@+id/nav_about"
                android:icon="@drawable/ic_info"
                android:title="About" />
        </menu>
    </item>

</menu>
```

### Step 3: Create Drawer Header

**res/layout/nav_header.xml**
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="176dp"
    android:background="@color/purple_500"
    android:gravity="bottom"
    android:orientation="vertical"
    android:padding="16dp"
    android:theme="@style/ThemeOverlay.AppCompat.Dark">

    <ImageView
        android:layout_width="64dp"
        android:layout_height="64dp"
        android:layout_marginBottom="8dp"
        android:contentDescription="App icon"
        android:src="@drawable/ic_launcher" />

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Dansk til Luis"
        android:textAppearance="@style/TextAppearance.AppCompat.Body1"
        android:textColor="@android:color/white"
        android:textSize="20sp"
        android:textStyle="bold" />

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Learn Danish with practice"
        android:textAppearance="@style/TextAppearance.AppCompat.Body2"
        android:textColor="@android:color/white" />

</LinearLayout>
```

### Step 4: Create Menu Icons

**res/drawable/ic_menu.xml** (Hamburger icon)
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorOnPrimary">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M3,18h18v-2H3V18zM3,13h18v-2H3V13zM3,6v2h18V6H3z"/>
</vector>
```

**res/drawable/ic_home.xml**
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/darker_gray"
        android:pathData="M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z"/>
</vector>
```

**res/drawable/ic_shuffle.xml**
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/darker_gray"
        android:pathData="M10.59,9.17L5.41,4 4,5.41l5.17,5.17 1.42,-1.41zM14.5,4l2.04,2.04L4,18.59 5.41,20 17.96,7.46 20,9.5L20,4h-5.5zM14.83,13.41l-1.41,1.41 3.13,3.13L14.5,20L20,20v-5.5l-2.04,2.04 -3.13,-3.13z"/>
</vector>
```

**res/drawable/ic_list.xml**
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/darker_gray"
        android:pathData="M3,13h2v-2L3,11v2zM3,17h2v-2L3,15v2zM3,9h2L5,7L3,7v2zM7,13h14v-2L7,11v2zM7,17h14v-2L7,15v2zM7,7v2h14L21,7L7,7z"/>
</vector>
```

**res/drawable/ic_chart.xml**
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/darker_gray"
        android:pathData="M19,3L5,3c-1.1,0 -2,0.9 -2,2v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2L21,5c0,-1.1 -0.9,-2 -2,-2zM9,17L7,17v-7h2v7zM13,17h-2L11,7h2v10zM17,17h-2v-4h2v4z"/>
</vector>
```

**res/drawable/ic_settings.xml**
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/darker_gray"
        android:pathData="M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.07,-0.94l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48l2.03,1.58C4.84,11.36 4.8,11.69 4.8,12s0.02,0.64 0.07,0.94l-2.03,1.58c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22l1.92,-3.32c0.12,-0.22 0.07,-0.47 -0.12,-0.61L19.14,12.94zM12,15.6c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6S13.98,15.6 12,15.6z"/>
</vector>
```

**res/drawable/ic_info.xml**
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/darker_gray"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM13,17h-2v-6h2v6zM13,9h-2L11,7h2v2z"/>
</vector>
```

### Step 5: Handle Menu in MainActivity

**MainActivity.kt**
```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)

        // Setup drawer
        drawerLayout = binding.drawerLayout
        navigationView = binding.navigationView

        // Enable hamburger icon
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        // Handle navigation menu clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationItemSelected(menuItem)
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Handle hamburger icon click
        binding.toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun handleNavigationItemSelected(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.nav_dashboard -> {
                // Already on dashboard, do nothing
                Toast.makeText(this, "Dashboard", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_random_exercise -> {
                // Start random exercise
                startRandomExercise()
            }
            R.id.nav_browse -> {
                // Open browse screen
                Toast.makeText(this, "Browse (coming soon)", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_statistics -> {
                // Open statistics screen
                Toast.makeText(this, "Statistics (coming soon)", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_settings -> {
                // Open settings
                openSettings()
            }
            R.id.nav_about -> {
                // Show about dialog
                showAboutDialog()
            }
        }
    }

    private fun startRandomExercise() {
        // Your existing logic
        val intent = Intent(this, ExerciseActivity::class.java)
        startActivity(intent)
    }

    private fun openSettings() {
        // TODO: Create SettingsActivity
        Toast.makeText(this, "Settings (coming soon)", Toast.LENGTH_SHORT).show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About Dansk til Luis")
            .setMessage("Version 1.0\n\nA Danish learning app with interactive exercises.\n\nCreated with Claude Code")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onBackPressed() {
        // Close drawer if open
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
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
        // Your sync logic here
    }
}
```

---

## Gestures

The drawer automatically supports:

1. **Swipe from left edge** → Opens drawer
2. **Swipe right on drawer** → Closes drawer
3. **Tap outside drawer** → Closes drawer
4. **Tap hamburger icon ☰** → Opens drawer
5. **Back button when drawer open** → Closes drawer

---

## Menu Structure Suggestions

### Basic Version (Current)
```
Dashboard
Random Exercise
Browse Exercises
Statistics
─────────────────
Settings
About
```

### Extended Version (Future)
```
Dashboard
Random Exercise
Browse Exercises
─────────────────
My Progress
Statistics
Achievements
─────────────────
Settings
Help
About
```

---

## Dependencies Needed

**app/build.gradle**
```gradle
dependencies {
    // Navigation drawer
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.drawerlayout:drawerlayout:1.2.0'
}
```

---

## Summary

### User Experience:

**Open drawer:**
- Swipe from left edge →
- Or tap ☰ icon →

**Navigate:**
- Tap menu item →
- Drawer closes →
- Action happens

### Features:
- ✅ **Swipe gesture** - Natural mobile interaction
- ✅ **Hamburger icon** - Tap to open
- ✅ **Nice header** - App branding
- ✅ **Organized sections** - Main items + Settings group
- ✅ **Material Design** - Standard Android pattern
- ✅ **Auto-close** - Closes when item tapped

Perfect for organizing future features! 🎯
