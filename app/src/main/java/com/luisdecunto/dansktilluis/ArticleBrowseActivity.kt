package com.luisdecunto.dansktilluis

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class ArticleBrowseActivity : AppCompatActivity() {

    private lateinit var database: com.luisdecunto.dansktilluis.database.AppDatabase
    private lateinit var articlesContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_browse)

        database = com.luisdecunto.dansktilluis.database.AppDatabase.getDatabase(this)
        articlesContainer = findViewById(R.id.articlesContainer)

        // Setup toolbar
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }

        loadArticles()
    }

    private fun loadArticles() {
        lifecycleScope.launch {
            // Get all articles (texts with type="article")
            val allTexts = database.textDao().getAllTexts().collect { textsList ->
                val articles = textsList.filter { it.type == "article" }.sortedBy { it.id }

                // Get all exercises
                database.exerciseDao().getAllExercises().collect { exercisesList ->
                    val userProgress = database.userProgressDao().getAllProgress().collect { progressList ->
                        // Group exercises by article
                        articles.forEach { article ->
                            val articleExercises = exercisesList
                                .filter { it.textId == article.id }
                                .sortedBy { it.id }

                            if (articleExercises.isNotEmpty()) {
                                createArticleSection(article, articleExercises, progressList)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createArticleSection(
        article: com.luisdecunto.dansktilluis.database.entities.TextEntity,
        exercises: List<com.luisdecunto.dansktilluis.database.entities.ExerciseEntity>,
        progress: List<com.luisdecunto.dansktilluis.database.entities.UserProgressEntity>
    ) {
        // Article card container
        val articleCard = MaterialCardView(this).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
            cardElevation = 4f
            radius = 12f
            setCardBackgroundColor(Color.WHITE)
        }

        val articleContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        // Article title
        val titleText = TextView(this).apply {
            text = article.title
            textSize = 18f
            setTextColor(Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 8)
        }
        articleContainer.addView(titleText)

        // Article pompadour (if available)
        if (!article.pompadour.isNullOrEmpty()) {
            val pompadourText = TextView(this).apply {
                text = article.pompadour
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@ArticleBrowseActivity, R.color.dark_gray))
                setTypeface(null, android.graphics.Typeface.ITALIC)
                setPadding(0, 0, 0, 16)
            }
            articleContainer.addView(pompadourText)
        }

        // Progress count
        val completedCount = exercises.count { exercise ->
            progress.any { it.exerciseId == exercise.id && it.isCompleted }
        }
        val progressText = TextView(this).apply {
            text = "Progress: $completedCount/${exercises.size}"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@ArticleBrowseActivity, R.color.primary_blue))
            setPadding(0, 0, 0, 12)
        }
        articleContainer.addView(progressText)

        // Exercise grid (like progress overview)
        val exerciseGrid = GridLayout(this).apply {
            columnCount = 5
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        exercises.forEachIndexed { index, exercise ->
            val isCompleted = progress.any { it.exerciseId == exercise.id && it.isCompleted }

            val exerciseBox = TextView(this).apply {
                text = "${index + 1}"
                textSize = 16f
                gravity = Gravity.CENTER
                setPadding(16, 16, 16, 16)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(4, 4, 4, 4)
                }

                // Set colors based on completion status
                if (isCompleted) {
                    setBackgroundColor(ContextCompat.getColor(this@ArticleBrowseActivity, R.color.completed_green))
                    setTextColor(Color.WHITE)
                } else {
                    setBackgroundColor(ContextCompat.getColor(this@ArticleBrowseActivity, R.color.light_gray))
                    setTextColor(Color.BLACK)
                }

                // Click to start exercise
                setOnClickListener {
                    startExercise(exercise)
                }
            }

            exerciseGrid.addView(exerciseBox)
        }

        articleContainer.addView(exerciseGrid)
        articleCard.addView(articleContainer)
        articlesContainer.addView(articleCard)
    }

    private fun startExercise(exercise: com.luisdecunto.dansktilluis.database.entities.ExerciseEntity) {
        // Create a temporary exercise set with just this one exercise
        lifecycleScope.launch {
            val exerciseSet = com.luisdecunto.dansktilluis.database.entities.ExerciseSetEntity(
                id = "temp_${exercise.id}",
                title = "Article Exercise",
                level = exercise.level ?: "B1",
                exerciseIds = listOf(exercise.id),
                isCompleted = false
            )

            // Start exercise activity
            val intent = Intent(this@ArticleBrowseActivity, ExerciseActivity::class.java).apply {
                putExtra("EXERCISE_SET_ID", exerciseSet.id)
                putExtra("EXERCISE_IDS", exerciseSet.exerciseIds.toTypedArray())
                putExtra("SET_TITLE", exerciseSet.title)
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh when returning to this activity
        articlesContainer.removeAllViews()
        loadArticles()
    }
}
