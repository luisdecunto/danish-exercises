package com.luisdecunto.dansktilluis

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luisdecunto.dansktilluis.database.AppDatabase
import com.luisdecunto.dansktilluis.database.entities.UserProgressEntity
import com.luisdecunto.dansktilluis.databinding.ActivityProgressOverviewBinding
import com.luisdecunto.dansktilluis.databinding.ItemExerciseCubeBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProgressOverviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProgressOverviewBinding
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressOverviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lifecycleScope.launch {
            loadProgressOverview()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private suspend fun loadProgressOverview() {
        val allExercises = database.exerciseDao().getAllExercises().first()
        val allProgress = database.userProgressDao().getAllProgress().first()

        // Create a map of exerciseId to progress
        val progressMap = allProgress.associateBy { it.exerciseId }

        // Separate articles from regular exercises
        val articles = allExercises.filter { it.type == "article" }
        val regularExercises = allExercises.filter { it.type != "article" }

        // Group regular exercises by level
        val exercisesByLevel = regularExercises.groupBy { it.level ?: "Unknown" }

        // Setup grids for each level
        setupLevelGrid(
            binding.levelA1Grid,
            exercisesByLevel["A1"] ?: emptyList(),
            progressMap
        )

        setupLevelGrid(
            binding.levelA2Grid,
            exercisesByLevel["A2"] ?: emptyList(),
            progressMap
        )

        setupLevelGrid(
            binding.levelB1Grid,
            exercisesByLevel["B1"] ?: emptyList(),
            progressMap
        )

        setupLevelGrid(
            binding.levelB2Grid,
            exercisesByLevel["B2"] ?: emptyList(),
            progressMap
        )

        setupLevelGrid(
            binding.levelC1Grid,
            exercisesByLevel["C1"] ?: emptyList(),
            progressMap
        )

        // Setup articles grid
        setupLevelGrid(
            binding.articlesGrid,
            articles,
            progressMap
        )
    }

    private fun setupLevelGrid(
        recyclerView: RecyclerView,
        exercises: List<com.luisdecunto.dansktilluis.database.entities.ExerciseEntity>,
        progressMap: Map<String, UserProgressEntity>
    ) {
        recyclerView.layoutManager = GridLayoutManager(this, 10) // 10 cubes per row
        recyclerView.adapter = ExerciseCubeAdapter(exercises, progressMap) { exerciseId ->
            openExercise(exerciseId)
        }
    }

    private fun openExercise(exerciseId: String) {
        val intent = android.content.Intent(this, ExerciseActivity::class.java)
        intent.putExtra("EXERCISE_SET_ID", "single_$exerciseId")
        intent.putExtra("EXERCISE_ID", exerciseId)
        intent.putExtra("EXERCISE_SET_TITLE", "Exercise")
        intent.putExtra("EXERCISE_SET_DESCRIPTION", "")
        intent.putExtra("RETURN_TO_PROGRESS", true)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        // Reload progress when returning from exercise
        lifecycleScope.launch {
            loadProgressOverview()
        }
    }

    // Adapter for the exercise cubes
    private class ExerciseCubeAdapter(
        private val exercises: List<com.luisdecunto.dansktilluis.database.entities.ExerciseEntity>,
        private val progressMap: Map<String, UserProgressEntity>,
        private val onExerciseClick: (String) -> Unit
    ) : RecyclerView.Adapter<ExerciseCubeAdapter.CubeViewHolder>() {

        class CubeViewHolder(val binding: ItemExerciseCubeBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CubeViewHolder {
            val binding = ItemExerciseCubeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return CubeViewHolder(binding)
        }

        override fun onBindViewHolder(holder: CubeViewHolder, position: Int) {
            val exercise = exercises[position]
            val progress = progressMap[exercise.id]

            // Extract exercise number from ID (e.g., "ex_001" -> "1")
            val exerciseNumber = exercise.id.replace("ex_", "").toIntOrNull() ?: (position + 1)
            holder.binding.exerciseNumber.text = exerciseNumber.toString()

            // Color code: green = correct, red = incorrect, white = unsolved
            val backgroundColor = when {
                progress == null -> Color.WHITE // Not attempted
                progress.isCorrect -> Color.parseColor("#4CAF50") // Green
                else -> Color.parseColor("#F44336") // Red
            }

            holder.binding.exerciseCube.setCardBackgroundColor(backgroundColor)

            // Set click listener
            holder.binding.exerciseCube.setOnClickListener {
                onExerciseClick(exercise.id)
            }
        }

        override fun getItemCount(): Int = exercises.size
    }
}
