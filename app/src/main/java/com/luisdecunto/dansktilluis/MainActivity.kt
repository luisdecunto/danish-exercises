package com.luisdecunto.dansktilluis

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.luisdecunto.dansktilluis.databinding.ActivityMainBinding
import com.luisdecunto.dansktilluis.models.ExerciseSet
import com.luisdecunto.dansktilluis.models.FillInTheBlankExercise
import com.luisdecunto.dansktilluis.models.MatchPairsExercise
import com.luisdecunto.dansktilluis.models.MultipleChoiceExercise
import com.luisdecunto.dansktilluis.storage.ProgressManager
import com.luisdecunto.dansktilluis.ui.ExerciseSetAdapter

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var progressManager: ProgressManager
    private val exerciseSets = mutableListOf<ExerciseSet>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressManager = ProgressManager(this)

        setupToolbar()
        setupNavigationDrawer()
        setupRecyclerView()
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
                // Already on dashboard
                Toast.makeText(this, "Dashboard", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_random_exercise -> {
                startRandomExercise()
            }
            R.id.nav_browse -> {
                // TODO: Navigate to browse screen
                Toast.makeText(this, "Browse (Coming Soon)", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_statistics -> {
                // TODO: Navigate to statistics screen
                Toast.makeText(this, "Statistics (Coming Soon)", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_settings -> {
                // TODO: Navigate to settings screen
                Toast.makeText(this, "Settings (Coming Soon)", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_about -> {
                // TODO: Show about dialog
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
        // TODO: Implement sync with Firebase when database is ready
        Snackbar.make(
            binding.root,
            "Refresh functionality will be added with database integration",
            Snackbar.LENGTH_LONG
        ).show()
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
        var totalExercises = 0
        var completedExercises = 0

        exerciseSets.forEach { set ->
            totalExercises += set.exercises.size
            completedExercises += set.exercises.count { it.isCompleted }
        }

        val percentage = if (totalExercises > 0) {
            (completedExercises * 100) / totalExercises
        } else {
            0
        }

        binding.statsTextView.text = "Completed: $completedExercises / $totalExercises"
        binding.statsProgressBar.progress = percentage
    }

    private fun setupRecyclerView() {
        binding.exerciseSetsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadExerciseSets() {
        // TODO: Load exercise sets from JSON files or database
        // For now, create sample exercise sets
        exerciseSets.clear()
        exerciseSets.addAll(createSampleExerciseSets())

        // Load progress for each set
        exerciseSets.forEach { progressManager.loadProgressForSet(it) }

        if (exerciseSets.isEmpty()) {
            binding.emptyStateTextView.visibility = View.VISIBLE
            binding.exerciseSetsRecyclerView.visibility = View.GONE
        } else {
            binding.emptyStateTextView.visibility = View.GONE
            binding.exerciseSetsRecyclerView.visibility = View.VISIBLE

            val adapter = ExerciseSetAdapter(exerciseSets) { exerciseSet ->
                startExerciseSet(exerciseSet)
            }
            binding.exerciseSetsRecyclerView.adapter = adapter
        }
    }

    private fun startExerciseSet(exerciseSet: ExerciseSet) {
        // TODO: Pass exercise set to ExerciseActivity
        val intent = Intent(this, ExerciseActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Reload progress when returning to this activity
        loadExerciseSets()
        updateStatistics()
    }

    private fun createSampleExerciseSets(): List<ExerciseSet> {
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
                        correctAnswer = "Tak"
                    ),
                    MultipleChoiceExercise(
                        id = "set2_mc1",
                        question = "What is 'Yes' in Danish?",
                        options = listOf("Nej", "Ja", "Måske", "Ok"),
                        correctAnswerIndex = 1
                    ),
                    MatchPairsExercise(
                        id = "set2_mp1",
                        question = "Match the Danish words with their English translations",
                        leftItems = listOf("Ja", "Nej", "Tak"),
                        rightItems = listOf("No", "Thank you", "Yes"),
                        correctPairs = mapOf(0 to 2, 1 to 0, 2 to 1)
                    )
                )
            ),
            ExerciseSet(
                id = "set3",
                title = "Numbers",
                description = "Learn to count in Danish",
                exercises = listOf(
                    MatchPairsExercise(
                        id = "set3_mp1",
                        question = "Match the numbers",
                        leftItems = listOf("En", "To", "Tre"),
                        rightItems = listOf("Three", "One", "Two"),
                        correctPairs = mapOf(0 to 1, 1 to 2, 2 to 0)
                    ),
                    MultipleChoiceExercise(
                        id = "set3_mc1",
                        question = "What is 'Five' in Danish?",
                        options = listOf("Fire", "Fem", "Seks", "Syv"),
                        correctAnswerIndex = 1
                    ),
                    FillInTheBlankExercise(
                        id = "set3_fb1",
                        question = "Write the Danish word for 'Ten'",
                        correctAnswer = "Ti"
                    )
                )
            )
        )
    }
}
