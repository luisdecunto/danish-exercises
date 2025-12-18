package com.luisdecunto.dansktilluis.models

/**
 * A set of exercises grouped together (e.g., a lesson or practice session)
 */
data class ExerciseSet(
    val id: String,
    val title: String,
    val description: String,
    val exercises: List<Exercise>
) {
    fun getProgress(): Int {
        if (exercises.isEmpty()) return 0
        val completed = exercises.count { it.isCompleted }
        return (completed * 100) / exercises.size
    }

    fun getTotalExercises(): Int = exercises.size

    fun getCompletedExercises(): Int = exercises.count { it.isCompleted }
}
