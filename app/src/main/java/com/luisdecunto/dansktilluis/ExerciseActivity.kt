package com.luisdecunto.dansktilluis

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.luisdecunto.dansktilluis.database.AppDatabase
import com.luisdecunto.dansktilluis.database.entities.ExerciseEntity
import com.luisdecunto.dansktilluis.databinding.ActivityExerciseBinding
import com.luisdecunto.dansktilluis.models.ArticleExercise
import com.luisdecunto.dansktilluis.models.Exercise
import com.luisdecunto.dansktilluis.models.ExerciseSet
import com.luisdecunto.dansktilluis.models.FillInTheBlankExercise
import com.luisdecunto.dansktilluis.models.MatchPairsExercise
import com.luisdecunto.dansktilluis.models.MultipleChoiceExercise
import com.luisdecunto.dansktilluis.storage.ProgressManager
import com.luisdecunto.dansktilluis.ui.ArticleFragment
import com.luisdecunto.dansktilluis.ui.FillInTheBlankFragment
import com.luisdecunto.dansktilluis.ui.MatchPairsFragment
import com.luisdecunto.dansktilluis.ui.MultipleChoiceFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ExerciseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExerciseBinding
    private lateinit var exerciseSet: ExerciseSet
    private lateinit var progressManager: ProgressManager
    private lateinit var database: AppDatabase
    private var currentExerciseIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressManager = ProgressManager(this)
        database = AppDatabase.getDatabase(this)

        // Setup Next button
        binding.nextButton.setOnClickListener {
            loadNextExercise()
        }

        // Load exercise set from intent extras
        val exerciseSetId = intent.getStringExtra("EXERCISE_SET_ID")
        val specificExerciseId = intent.getStringExtra("EXERCISE_ID")
        val exerciseSetTitle = intent.getStringExtra("EXERCISE_SET_TITLE") ?: "Exercises"
        val exerciseSetDescription = intent.getStringExtra("EXERCISE_SET_DESCRIPTION") ?: ""

        if (specificExerciseId != null) {
            // Load a specific exercise by ID
            lifecycleScope.launch {
                loadSpecificExercise(specificExerciseId, exerciseSetTitle, exerciseSetDescription)
            }
        } else if (exerciseSetId != null) {
            // Load exercises from database for this set
            lifecycleScope.launch {
                loadExerciseSetFromDatabase(exerciseSetId, exerciseSetTitle, exerciseSetDescription)
            }
        } else {
            // Fallback to sample exercises
            exerciseSet = createSampleExerciseSet()
            progressManager.loadProgressForSet(exerciseSet)
            loadCurrentExercise()
        }
    }

    private suspend fun loadSpecificExercise(exerciseId: String, title: String, description: String) {
        val dbExercises = database.exerciseDao().getAllExercises().first()
        val dbTexts = database.textDao().getAllTexts().first()

        val exerciseEntity = dbExercises.find { it.id == exerciseId }
        if (exerciseEntity != null) {
            val exercise = convertEntityToExercise(exerciseEntity)

            // Attach text if exercise has a textId
            if (exercise != null && exerciseEntity.textId != null) {
                val text = dbTexts.find { it.id == exerciseEntity.textId }
                if (text != null) {
                    exercise.textId = text.id
                    exercise.textTitle = text.title
                    exercise.textPompadour = text.pompadour
                    exercise.textContent = text.content
                }
            }

            if (exercise != null) {
                exerciseSet = ExerciseSet(
                    id = "single_$exerciseId",
                    title = title,
                    description = description,
                    exercises = listOf(exercise)
                )
                progressManager.loadProgressForSet(exerciseSet)
                loadCurrentExercise()
            }
        }
    }

    private suspend fun loadExerciseSetFromDatabase(setId: String, title: String, description: String) {
        val dbExercises = database.exerciseDao().getAllExercises().first()
        val dbTexts = database.textDao().getAllTexts().first()

        // Convert all exercises from database
        val allExercises = dbExercises.mapNotNull { entity ->
            val exercise = convertEntityToExercise(entity)

            // Attach text if exercise has a textId
            if (exercise != null && entity.textId != null) {
                val text = dbTexts.find { it.id == entity.textId }
                if (text != null) {
                    exercise.textId = text.id
                    exercise.textTitle = text.title
                    exercise.textPompadour = text.pompadour
                    exercise.textContent = text.content
                }
            }

            exercise
        }

        // Pick 1 random exercise
        val selectedExercises = if (allExercises.isNotEmpty()) {
            listOf(allExercises.random())
        } else {
            emptyList()
        }

        exerciseSet = ExerciseSet(
            id = setId,
            title = title,
            description = description,
            exercises = selectedExercises
        )
        progressManager.loadProgressForSet(exerciseSet)
        loadCurrentExercise()
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
                        level = entity.level,
                        explanation = explanation
                    )
                }
                "match_pairs" -> {
                    @Suppress("UNCHECKED_CAST")
                    val pairsData = dataMap["pairs"] as? List<Map<String, String>> ?: return null

                    val leftItems = pairsData.map { it["left"] ?: "" }
                    val rightItems = pairsData.map { it["right"] ?: "" }

                    // Create correct pairs mapping (index to index)
                    val correctPairs = leftItems.indices.associateWith { it }
                    val explanation = dataMap["explanation"] as? String

                    MatchPairsExercise(
                        id = entity.id,
                        question = entity.question,
                        leftItems = leftItems,
                        rightItems = rightItems,
                        correctPairs = correctPairs,
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

    private fun loadCurrentExercise() {
        // Hide explanation and Next button when loading a new exercise
        binding.explanationCard.visibility = android.view.View.GONE
        binding.nextButton.visibility = android.view.View.GONE

        if (currentExerciseIndex < exerciseSet.exercises.size) {
            val exercise = exerciseSet.exercises[currentExerciseIndex]
            updateProgress()
            updateLevelBadge(exercise)
            displayExercise(exercise)
        } else {
            finishExerciseSet()
        }
    }

    private fun updateLevelBadge(exercise: Exercise) {
        if (exercise.level != null) {
            binding.levelBadge.text = exercise.level
            binding.levelBadge.visibility = android.view.View.VISIBLE
        } else {
            binding.levelBadge.visibility = android.view.View.GONE
        }
    }

    private fun displayExercise(exercise: Exercise) {
        // Setup article drawer if exercise has text content
        if (exercise.textContent != null && exercise.textContent!!.isNotEmpty()) {
            setupArticleDrawer(exercise)
        } else {
            // Hide drawer if no article text
            binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }

        val fragment = when (exercise) {
            is MultipleChoiceExercise -> MultipleChoiceFragment.newInstance(exercise) { isCorrect ->
                onExerciseCompleted(exercise, isCorrect)
            }
            is FillInTheBlankExercise -> FillInTheBlankFragment.newInstance(exercise) { isCorrect ->
                onExerciseCompleted(exercise, isCorrect)
            }
            is MatchPairsExercise -> MatchPairsFragment.newInstance(exercise) { isCorrect ->
                onExerciseCompleted(exercise, isCorrect)
            }
            is ArticleExercise -> ArticleFragment.newInstance(exercise) { isCorrect ->
                onExerciseCompleted(exercise, isCorrect)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.exerciseContainer, fragment)
            .commit()
    }

    private fun setupArticleDrawer(exercise: Exercise) {
        // Enable the drawer
        binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED)

        // Populate drawer with article content
        binding.drawerArticleTitleTextView.text = exercise.textTitle ?: ""
        binding.drawerArticleContentTextView.text = exercise.textContent ?: ""

        // Show pompadour if available
        if (!exercise.textPompadour.isNullOrEmpty()) {
            binding.drawerArticlePompadourTextView.text = exercise.textPompadour
            binding.drawerArticlePompadourTextView.visibility = android.view.View.VISIBLE
        } else {
            binding.drawerArticlePompadourTextView.visibility = android.view.View.GONE
        }

        // Enable swipe from right edge to open drawer
        binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED, androidx.core.view.GravityCompat.END)
    }

    private fun onExerciseCompleted(exercise: Exercise, isCorrect: Boolean) {
        progressManager.saveExerciseProgress(exercise)

        // Save to database
        lifecycleScope.launch {
            val progressEntity = com.luisdecunto.dansktilluis.database.entities.UserProgressEntity(
                exerciseId = exercise.id,
                isCompleted = true,
                isCorrect = isCorrect,
                attempts = 1,
                lastAnswer = exercise.userAnswer,
                completedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            database.userProgressDao().insert(progressEntity)
        }

        // Show explanation and Next button
        if (exercise.explanation != null && exercise.explanation!!.isNotEmpty()) {
            binding.explanationText.text = exercise.explanation
            binding.explanationCard.visibility = android.view.View.VISIBLE
        }
        binding.nextButton.visibility = android.view.View.VISIBLE
    }

    private fun loadNextExercise() {
        // Check if we should return to progress overview
        val returnToProgress = intent.getBooleanExtra("RETURN_TO_PROGRESS", false)

        if (returnToProgress) {
            // Just finish and return to progress overview
            finish()
            return
        }

        // Hide explanation and Next button
        binding.explanationCard.visibility = android.view.View.GONE
        binding.nextButton.visibility = android.view.View.GONE

        // Load a new random unsolved exercise
        lifecycleScope.launch {
            val allExercises = database.exerciseDao().getAllExercises().first()
            val allProgress = database.userProgressDao().getAllProgress().first()
            val solvedExerciseIds = allProgress.filter { it.isCorrect }.map { it.exerciseId }.toSet()
            val unsolvedExercises = allExercises.filter { it.id !in solvedExerciseIds }

            if (unsolvedExercises.isNotEmpty()) {
                val randomExercise = unsolvedExercises.random()
                val exercise = convertEntityToExercise(randomExercise)

                if (exercise != null) {
                    // Attach text if needed
                    if (randomExercise.textId != null) {
                        val texts = database.textDao().getAllTexts().first()
                        val text = texts.find { it.id == randomExercise.textId }
                        if (text != null) {
                            exercise.textId = text.id
                            exercise.textTitle = text.title
                            exercise.textPompadour = text.pompadour
                            exercise.textContent = text.content
                        }
                    }

                    exerciseSet = ExerciseSet(
                        id = "random_${exercise.id}",
                        title = "Random Exercise",
                        description = "Level: ${exercise.level ?: "Unknown"}",
                        exercises = listOf(exercise)
                    )
                    currentExerciseIndex = 0
                    progressManager.loadProgressForSet(exerciseSet)
                    loadCurrentExercise()
                }
            } else {
                Toast.makeText(this@ExerciseActivity, "All exercises completed! Great job!", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun updateProgress() {
        binding.progressTextView.text = "${currentExerciseIndex + 1}/${exerciseSet.exercises.size}"
    }

    private fun finishExerciseSet() {
        val completed = exerciseSet.getCompletedExercises()
        val total = exerciseSet.getTotalExercises()
        Toast.makeText(
            this,
            "Exercise set completed! Score: $completed/$total",
            Toast.LENGTH_LONG
        ).show()
        finish()
    }

    // Sample exercise set for testing
    private fun createSampleExerciseSet(): ExerciseSet {
        val exercises = listOf(
            MultipleChoiceExercise(
                id = "mc1",
                question = "What is 'Hello' in Danish?",
                options = listOf("Hej", "Tak", "Farvel", "Undskyld"),
                correctAnswerIndex = 0
            ),
            FillInTheBlankExercise(
                id = "fb1",
                question = "How do you say 'Thank you' in Danish?",
                correctAnswer = "Tak",
                hint = "It starts with T"
            ),
            MatchPairsExercise(
                id = "mp1",
                question = "Match the Danish words with their English translations",
                leftItems = listOf("Hej", "Tak", "Ja"),
                rightItems = listOf("Yes", "Hello", "Thank you"),
                correctPairs = mapOf(0 to 1, 1 to 2, 2 to 0)
            )
        )

        return ExerciseSet(
            id = "sample1",
            title = "Basic Greetings",
            description = "Learn basic Danish greetings",
            exercises = exercises
        )
    }
}
