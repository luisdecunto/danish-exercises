package com.luisdecunto.dansktilluis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.luisdecunto.dansktilluis.database.AppDatabase
import com.luisdecunto.dansktilluis.database.entities.ExerciseEntity
import com.luisdecunto.dansktilluis.databinding.ActivityMainBinding
import com.luisdecunto.dansktilluis.models.ArticleExercise
import com.luisdecunto.dansktilluis.models.ArticleSubExercise
import com.luisdecunto.dansktilluis.models.Exercise
import com.luisdecunto.dansktilluis.models.ExerciseSet
import com.luisdecunto.dansktilluis.models.FillInTheBlankExercise
import com.luisdecunto.dansktilluis.models.MatchPairsExercise
import com.luisdecunto.dansktilluis.models.MultipleChoiceExercise
import com.luisdecunto.dansktilluis.sync.SyncManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase
    private lateinit var syncManager: SyncManager
    private var isSyncing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        syncManager = SyncManager(this)

        setupToolbar()
        setupNavigationDrawer()
        setupContinueButton()
        updateStatistics()
    }

    private fun setupContinueButton() {
        binding.continueButton.setOnClickListener {
            startRandomExercise()
        }
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
            R.id.nav_articles -> {
                startActivity(Intent(this, ArticleBrowseActivity::class.java))
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
        lifecycleScope.launch {
            val allExercises = database.exerciseDao().getAllExercises().first()
            val allProgress = database.userProgressDao().getAllProgress().first()

            // Create a set of solved exercise IDs (where isCorrect = true)
            val solvedExerciseIds = allProgress.filter { it.isCorrect }.map { it.exerciseId }.toSet()

            // Filter to only unsolved exercises
            val unsolvedExercises = allExercises.filter { it.id !in solvedExerciseIds }

            if (unsolvedExercises.isNotEmpty()) {
                val randomExercise = unsolvedExercises.random()
                // Create a temporary exercise set with 1 exercise
                val tempSet = ExerciseSet(
                    id = "random_${randomExercise.id}",
                    title = "Random Exercise",
                    description = "Level: ${randomExercise.level ?: "Unknown"}",
                    exercises = listOfNotNull(convertEntityToExercise(randomExercise))
                )
                startExerciseSet(tempSet)
            } else {
                Toast.makeText(this@MainActivity, "All exercises completed! Great job!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startRandomArticleExercise() {
        lifecycleScope.launch {
            val allExercises = database.exerciseDao().getAllExercises().first()
            val allProgress = database.userProgressDao().getAllProgress().first()

            // Create a set of solved exercise IDs (where isCorrect = true)
            val solvedExerciseIds = allProgress.filter { it.isCorrect }.map { it.exerciseId }.toSet()

            // Filter to only unsolved article exercises (B1+ level)
            val unsolvedArticles = allExercises.filter {
                it.id !in solvedExerciseIds &&
                it.type == "article" &&
                (it.level == "B1" || it.level == "B2" || it.level == "C1" || it.level == "C2")
            }

            if (unsolvedArticles.isNotEmpty()) {
                val randomArticle = unsolvedArticles.random()
                // Create a temporary exercise set with 1 article exercise
                val tempSet = ExerciseSet(
                    id = "article_${randomArticle.id}",
                    title = "Article Exercise",
                    description = "Level: ${randomArticle.level ?: "B1+"}",
                    exercises = listOfNotNull(convertEntityToExercise(randomArticle))
                )
                startExerciseSet(tempSet)
            } else {
                Toast.makeText(this@MainActivity, "No article exercises available yet!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateStatistics() {
        lifecycleScope.launch {
            val totalExercises = database.exerciseDao().getExerciseCount().first()
            val correctExercises = database.userProgressDao().getCorrectCount().first()

            val percentage = if (totalExercises > 0) {
                (correctExercises * 100) / totalExercises
            } else {
                0
            }

            binding.statsTextView.text = "Solved: $correctExercises / $totalExercises"
            binding.statsProgressBar.progress = percentage
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
                    val explanation = dataMap["explanation"] as? String
                    MultipleChoiceExercise(
                        id = entity.id,
                        question = entity.question,
                        options = options,
                        correctAnswerIndex = correct,
                        textId = entity.textId,
                        level = entity.level,
                        explanation = explanation
                    )
                }
                "write_word" -> {
                    val correct = dataMap["correct"] as? String ?: return null
                    val hint = dataMap["hint"] as? String
                    val explanation = dataMap["explanation"] as? String
                    FillInTheBlankExercise(
                        id = entity.id,
                        question = entity.question,
                        correctAnswer = correct,
                        hint = hint,
                        textId = entity.textId,
                        level = entity.level,
                        explanation = explanation
                    )
                }
                "match_pairs" -> {
                    @Suppress("UNCHECKED_CAST")
                    val pairsData = dataMap["pairs"] as? List<Map<String, String>> ?: return null
                    val leftItems = pairsData.map { it["left"] ?: "" }
                    val rightItems = pairsData.map { it["right"] ?: "" }
                    val correctPairs = leftItems.indices.associateWith { it }
                    val explanation = dataMap["explanation"] as? String
                    MatchPairsExercise(
                        id = entity.id,
                        question = entity.question,
                        leftItems = leftItems,
                        rightItems = rightItems,
                        correctPairs = correctPairs,
                        textId = entity.textId,
                        level = entity.level,
                        explanation = explanation
                    )
                }
                "article" -> {
                    @Suppress("UNCHECKED_CAST")
                    val subExercisesData = dataMap["subExercises"] as? List<Map<String, Any>> ?: return null

                    val subExercises = subExercisesData.mapNotNull { subData ->
                        val subType = subData["type"] as? String
                        val subQuestion = subData["question"] as? String ?: return@mapNotNull null

                        when (subType) {
                            "multiple_choice" -> {
                                @Suppress("UNCHECKED_CAST")
                                val options = subData["options"] as? List<String> ?: return@mapNotNull null
                                val correctIndex = when (val c = subData["correctIndex"]) {
                                    is Double -> c.toInt()
                                    is Int -> c
                                    else -> return@mapNotNull null
                                }
                                ArticleSubExercise.MultipleChoice(
                                    question = subQuestion,
                                    options = options,
                                    correctIndex = correctIndex
                                )
                            }
                            "open_ended" -> {
                                val correctAnswer = subData["correctAnswer"] as? String ?: return@mapNotNull null
                                ArticleSubExercise.OpenEnded(
                                    question = subQuestion,
                                    correctAnswer = correctAnswer
                                )
                            }
                            else -> null
                        }
                    }

                    val explanation = dataMap["explanation"] as? String
                    ArticleExercise(
                        id = entity.id,
                        question = entity.question,
                        subExercises = subExercises,
                        textId = entity.textId,
                        level = entity.level,
                        explanation = explanation
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
        updateStatistics()
    }
}
