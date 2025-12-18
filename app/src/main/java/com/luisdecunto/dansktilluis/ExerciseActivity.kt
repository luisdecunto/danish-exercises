package com.luisdecunto.dansktilluis

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.luisdecunto.dansktilluis.databinding.ActivityExerciseBinding
import com.luisdecunto.dansktilluis.models.Exercise
import com.luisdecunto.dansktilluis.models.ExerciseSet
import com.luisdecunto.dansktilluis.models.FillInTheBlankExercise
import com.luisdecunto.dansktilluis.models.MatchPairsExercise
import com.luisdecunto.dansktilluis.models.MultipleChoiceExercise
import com.luisdecunto.dansktilluis.storage.ProgressManager
import com.luisdecunto.dansktilluis.ui.FillInTheBlankFragment
import com.luisdecunto.dansktilluis.ui.MatchPairsFragment
import com.luisdecunto.dansktilluis.ui.MultipleChoiceFragment

class ExerciseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExerciseBinding
    private lateinit var exerciseSet: ExerciseSet
    private lateinit var progressManager: ProgressManager
    private var currentExerciseIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressManager = ProgressManager(this)

        // TODO: Load exercise set from intent or database
        // For now, create a sample exercise set
        exerciseSet = createSampleExerciseSet()
        progressManager.loadProgressForSet(exerciseSet)

        loadCurrentExercise()
    }

    private fun loadCurrentExercise() {
        if (currentExerciseIndex < exerciseSet.exercises.size) {
            val exercise = exerciseSet.exercises[currentExerciseIndex]
            updateProgress()
            displayExercise(exercise)
        } else {
            finishExerciseSet()
        }
    }

    private fun displayExercise(exercise: Exercise) {
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
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.exerciseContainer, fragment)
            .commit()
    }

    private fun onExerciseCompleted(exercise: Exercise, isCorrect: Boolean) {
        progressManager.saveExerciseProgress(exercise)

        // Move to next exercise after a short delay
        binding.root.postDelayed({
            currentExerciseIndex++
            loadCurrentExercise()
        }, 1500)
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
