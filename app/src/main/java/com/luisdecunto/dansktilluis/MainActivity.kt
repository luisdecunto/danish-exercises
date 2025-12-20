package com.luisdecunto.dansktilluis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.luisdecunto.dansktilluis.database.AppDatabase
import com.luisdecunto.dansktilluis.database.entities.ExerciseEntity
import com.luisdecunto.dansktilluis.databinding.ActivityMainBinding
import com.luisdecunto.dansktilluis.models.Exercise
import com.luisdecunto.dansktilluis.models.ExerciseSet
import com.luisdecunto.dansktilluis.models.FillInTheBlankExercise
import com.luisdecunto.dansktilluis.models.MatchPairsExercise
import com.luisdecunto.dansktilluis.models.MultipleChoiceExercise
import com.luisdecunto.dansktilluis.storage.ProgressManager
import com.luisdecunto.dansktilluis.sync.SyncManager
import com.luisdecunto.dansktilluis.ui.ExerciseSetAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var progressManager: ProgressManager
    private lateinit var database: AppDatabase
    private lateinit var syncManager: SyncManager
    private val exerciseSets = mutableListOf<ExerciseSet>()
    private var isSyncing = false
    private lateinit var adapter: ExerciseSetAdapter // Define adapter as a class property

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressManager = ProgressManager(this)
        database = AppDatabase.getDatabase(this)
        syncManager = SyncManager(this)

        setupToolbar()
        setupNavigationDrawer()
        setupRecyclerView() // This will now work correctly
        loadExerciseSets()
        updateStatistics()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun setupNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                refreshExercises()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> {
                Toast.makeText(this, "Dashboard", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_random_exercise -> {
                startRandomExercise()
            }
            R.id.nav_progress_overview -> {
                startActivity(Intent(this, ProgressOverviewActivity::class.java))
            }
            R.id.nav_statistics -> {
                Toast.makeText(this, "Statistics (Coming Soon)", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_settings -> {
                Toast.makeText(this, "Settings (Coming Soon)", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_about -> {
                Toast.makeText(this, "About (Coming Soon)", Toast.LENGTH_SHORT).show()
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun refreshExercises() {
        if (isSyncing) {
            Toast.makeText(this, "Sync already in progress...", Toast.LENGTH_SHORT).show()
            return
        }

        isSyncing = true
        Snackbar.make(binding.root, "Syncing from GitHub...", Snackbar.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val result = syncManager.syncFromGitHub()
            isSyncing = false

            if (result.success) {
                val message = "Successfully synced! " +
                        "${result.newExercisesCount} exercises, " +
                        "${result.newTextsCount} texts"
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                loadExerciseSets()
                updateStatistics()
            } else {
                Snackbar.make(
                    binding.root,
                    "Sync failed: ${result.errorMessage}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun startRandomExercise() {
        if (exerciseSets.isNotEmpty()) {
            val randomSet = exerciseSets.random()
            startExerciseSet(randomSet)
        } else {
            Toast.makeText(this, "No exercises available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateStatistics() {
        lifecycleScope.launch {
            val totalExercises = database.exerciseDao().getExerciseCount().first()
            val completedExercises = database.userProgressDao().getCompletedCount().first()

            val percentage = if (totalExercises > 0) {
                (completedExercises * 100) / totalExercises
            } else {
                0
            }

            binding.statsTextView.text = "Completed: $completedExercises / $totalExercises"
            binding.statsProgressBar.progress = percentage
        }
    }

    // STABLE VERSION of setupRecyclerView
    private fun setupRecyclerView() {
        adapter = ExerciseSetAdapter(exerciseSets) { exerciseSet ->
            startExerciseSet(exerciseSet)
        }
        binding.exerciseSetsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.exerciseSetsRecyclerView.adapter = adapter
    }

    private fun loadExerciseSets() {
        lifecycleScope.launch {
            exerciseSets.clear()

            val dbExerciseCount = database.exerciseDao().getExerciseCount().first()

            if (dbExerciseCount > 0) {
                val dbExercises = database.exerciseDao().getAllExercises().first()
                val exercises = dbExercises.mapNotNull { entity ->
                    convertEntityToExercise(entity)
                }

                if (exercises.isNotEmpty()) {
                    exerciseSets.add(ExerciseSet(
                        id = "all_exercises",
                        title = "Progress",
                        description = "${exercises.size} exercises",
                        exercises = exercises
                    ))
                }
            } else {
                exerciseSets.addAll(createSampleExerciseSets())
            }

            exerciseSets.forEach { progressManager.loadProgressForSet(it) }

            if (exerciseSets.isEmpty()) {
                binding.emptyStateTextView.visibility = View.VISIBLE
                binding.exerciseSetsRecyclerView.visibility = View.GONE
            } else {
                binding.emptyStateTextView.visibility = View.GONE
                binding.exerciseSetsRecyclerView.visibility = View.VISIBLE
            }

            adapter.notifyDataSetChanged()
        }
    }

    private fun convertEntityToExercise(entity: ExerciseEntity): Exercise? {
        return try {
            val gson = Gson()
            @Suppress("UNCHECKED_CAST")
            val dataMap = gson.fromJson(entity.dataJson, Map::class.java) as Map<String, Any>

            when (entity.type) {
                "multiple_choice" -> {
                    @Suppress("UNCHECKED_CAST")
                    val options = dataMap["options"] as? List<String> ?: return null
                    val correct = when (val c = dataMap["correct"]) {
                        is Double -> c.toInt()
                        is Int -> c
                        else -> return null
                    }
                    MultipleChoiceExercise(
                        id = entity.id,
                        question = entity.question,
                        options = options,
                        correctAnswerIndex = correct,
                        textId = entity.textId,
                        level = entity.level
                    )
                }
                "write_word" -> {
                    val correct = dataMap["correct"] as? String ?: return null
                    val hint = dataMap["hint"] as? String
                    FillInTheBlankExercise(
                        id = entity.id,
                        question = entity.question,
                        correctAnswer = correct,
                        hint = hint,
                        textId = entity.textId,
                        level = entity.level
                    )
                }
                "match_pairs" -> {
                    @Suppress("UNCHECKED_CAST")
                    val pairsData = dataMap["pairs"] as? List<Map<String, String>> ?: return null
                    val leftItems = pairsData.map { it["left"] ?: "" }
                    val rightItems = pairsData.map { it["right"] ?: "" }
                    val correctPairs = leftItems.indices.associateWith { it }
                    MatchPairsExercise(
                        id = entity.id,
                        question = entity.question,
                        leftItems = leftItems,
                        rightItems = rightItems,
                        correctPairs = correctPairs,
                        textId = entity.textId,
                        level = entity.level
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun startExerciseSet(exerciseSet: ExerciseSet) {
        val intent = Intent(this, ExerciseActivity::class.java)
        intent.putExtra("EXERCISE_SET_ID", exerciseSet.id)
        intent.putExtra("EXERCISE_SET_TITLE", exerciseSet.title)
        intent.putExtra("EXERCISE_SET_DESCRIPTION", exerciseSet.description)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadExerciseSets()
        updateStatistics()
    }

    private fun createSampleExerciseSets(): List<ExerciseSet> {
        // This function remains the same
        return listOf(
            ExerciseSet(
                id = "set1",
                title = "Basic Greetings",
                description = "Learn how to greet people in Danish",
                exercises = listOf(
                    MultipleChoiceExercise(
                        id = "set1_mc1",
                        question = "What is 'Hello' in Danish?",
                        options = listOf("Hej", "Tak", "Farvel", "Undskyld"),
                        correctAnswerIndex = 0
                    ),
                    FillInTheBlankExercise(
                        id = "set1_fb1",
                        question = "How do you say 'Good morning' in Danish?",
                        correctAnswer = "Godmorgen",
                        hint = "It's similar to English"
                    ),
                    MultipleChoiceExercise(
                        id = "set1_mc2",
                        question = "What is 'Goodbye' in Danish?",
                        options = listOf("Hej", "Tak", "Farvel", "Hej hej"),
                        correctAnswerIndex = 2
                    )
                )
            ),
            ExerciseSet(
                id = "set2",
                title = "Common Phrases",
                description = "Learn essential Danish phrases",
                exercises = listOf(
                    FillInTheBlankExercise(
                        id = "set2_fb1",
                        question = "How do you say 'Thank you' in Danish?",
                        correctAnswer = "Tak",
                        hint = "A short and common word"
                    )
                )
            )
        )
    }
}
