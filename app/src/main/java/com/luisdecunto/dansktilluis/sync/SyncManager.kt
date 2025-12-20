package com.luisdecunto.dansktilluis.sync

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.luisdecunto.dansktilluis.database.AppDatabase
import com.luisdecunto.dansktilluis.database.entities.ExerciseEntity
import com.luisdecunto.dansktilluis.database.entities.TextEntity
import com.luisdecunto.dansktilluis.models.json.DatabaseJson
import com.luisdecunto.dansktilluis.models.json.ExerciseJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Manages syncing exercise data from GitHub to local Room database
 */
class SyncManager(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val textDao = database.textDao()
    private val exerciseDao = database.exerciseDao()
    private val githubService = GitHubSyncService()
    private val gson = Gson()

    /**
     * Result of a sync operation
     */
    data class SyncResult(
        val success: Boolean,
        val newExercisesCount: Int = 0,
        val newTextsCount: Int = 0,
        val errorMessage: String? = null
    )

    /**
     * Performs full sync: downloads database.json and imports to Room database
     */
    suspend fun syncFromGitHub(): SyncResult = withContext(Dispatchers.IO) {
        try {
            // Step 1: Download JSON from GitHub
            val jsonString = githubService.downloadDatabase()
                ?: return@withContext SyncResult(
                    success = false,
                    errorMessage = "Failed to download database from GitHub"
                )

            // Step 2: Parse JSON
            val databaseJson = try {
                gson.fromJson(jsonString, DatabaseJson::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext SyncResult(
                    success = false,
                    errorMessage = "Failed to parse database JSON: ${e.message}"
                )
            }

            // Step 3: Get counts before import
            val oldExerciseCount = exerciseDao.getExerciseCount().first()
            val oldTextCount = textDao.getAllTexts().first().size
            Log.d("SyncManager", "Before sync: $oldExerciseCount exercises, $oldTextCount texts")

            // Step 4: Convert texts to entities
            val textEntities = databaseJson.texts.values.map { textJson ->
                TextEntity(
                    id = textJson.id,
                    type = textJson.type ?: "text",  // Default to "text" if not specified
                    title = textJson.title,
                    pompadour = textJson.pompadour,
                    content = textJson.content,
                    translation = textJson.translation
                )
            }

            // Step 5: Convert exercises to entities
            val exerciseEntities = databaseJson.exercises.map { exerciseJson ->
                ExerciseEntity(
                    id = exerciseJson.id,
                    type = exerciseJson.type,
                    textId = exerciseJson.textId,
                    question = exerciseJson.question,
                    dataJson = serializeExerciseData(exerciseJson),
                    level = exerciseJson.level
                )
            }

            // Step 6: Clear existing data and insert new data
            textDao.deleteAll()
            exerciseDao.deleteAll()
            textDao.insertAll(textEntities)
            exerciseDao.insertAll(exerciseEntities)

            // Step 7: Calculate new items
            val newExerciseCount = exerciseEntities.size
            val newTextCount = textEntities.size
            Log.d("SyncManager", "After sync: $newExerciseCount exercises, $newTextCount texts")

            SyncResult(
                success = true,
                newExercisesCount = newExerciseCount,
                newTextsCount = newTextCount
            )

        } catch (e: Exception) {
            e.printStackTrace()
            SyncResult(
                success = false,
                errorMessage = "Unexpected error during sync: ${e.message}"
            )
        }
    }

    /**
     * Serializes type-specific exercise data to JSON string
     */
    private fun serializeExerciseData(exerciseJson: ExerciseJson): String {
        val dataMap = mutableMapOf<String, Any?>()

        when (exerciseJson.type) {
            "multiple_choice" -> {
                dataMap["options"] = exerciseJson.options
                dataMap["correct"] = exerciseJson.correct
                dataMap["explanation"] = exerciseJson.explanation
            }
            "write_word" -> {
                dataMap["correct"] = exerciseJson.correct
                dataMap["accept_variants"] = exerciseJson.acceptVariants
                dataMap["hint"] = exerciseJson.hint
                dataMap["explanation"] = exerciseJson.explanation
            }
            "match_pairs" -> {
                dataMap["pairs"] = exerciseJson.pairs
                dataMap["explanation"] = exerciseJson.explanation
            }
            "article" -> {
                dataMap["explanation"] = exerciseJson.explanation

                // Convert SubExerciseJson to Map format for storage
                val subExercisesData = exerciseJson.subExercises?.map { subEx ->
                    mapOf(
                        "type" to subEx.type,
                        "question" to subEx.question,
                        "options" to subEx.options,
                        "correctIndex" to subEx.correctIndex,
                        "correctAnswer" to subEx.correctAnswer
                    )
                }
                dataMap["subExercises"] = subExercisesData
            }
        }

        return gson.toJson(dataMap)
    }
}
