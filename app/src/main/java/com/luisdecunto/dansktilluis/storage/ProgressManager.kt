package com.luisdecunto.dansktilluis.storage

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.luisdecunto.dansktilluis.models.Exercise
import com.luisdecunto.dansktilluis.models.ExerciseSet

/**
 * Manages saving and loading exercise progress
 */
class ProgressManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "DanskTilLuisProgress"
        private const val KEY_EXERCISE_PREFIX = "exercise_"
        private const val KEY_SET_PREFIX = "set_"
    }

    /**
     * Save progress for a specific exercise
     */
    fun saveExerciseProgress(exercise: Exercise) {
        val key = "$KEY_EXERCISE_PREFIX${exercise.id}"
        val json = gson.toJson(ExerciseProgress(
            isCompleted = exercise.isCompleted,
            userAnswer = exercise.userAnswer
        ))
        sharedPreferences.edit().putString(key, json).apply()
    }

    /**
     * Load progress for a specific exercise
     */
    fun loadExerciseProgress(exerciseId: String): ExerciseProgress? {
        val key = "$KEY_EXERCISE_PREFIX$exerciseId"
        val json = sharedPreferences.getString(key, null) ?: return null
        return try {
            gson.fromJson(json, ExerciseProgress::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Save progress for an entire exercise set
     */
    fun saveExerciseSet(exerciseSet: ExerciseSet) {
        exerciseSet.exercises.forEach { exercise ->
            saveExerciseProgress(exercise)
        }
    }

    /**
     * Load progress and apply to exercise set
     */
    fun loadProgressForSet(exerciseSet: ExerciseSet) {
        exerciseSet.exercises.forEach { exercise ->
            val progress = loadExerciseProgress(exercise.id)
            progress?.let {
                exercise.isCompleted = it.isCompleted
                exercise.userAnswer = it.userAnswer
            }
        }
    }

    /**
     * Clear all progress
     */
    fun clearAllProgress() {
        sharedPreferences.edit().clear().apply()
    }

    /**
     * Clear progress for a specific exercise set
     */
    fun clearSetProgress(exerciseSet: ExerciseSet) {
        val editor = sharedPreferences.edit()
        exerciseSet.exercises.forEach { exercise ->
            editor.remove("$KEY_EXERCISE_PREFIX${exercise.id}")
        }
        editor.apply()
    }

    data class ExerciseProgress(
        val isCompleted: Boolean,
        val userAnswer: String?
    )
}
