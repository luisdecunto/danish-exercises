package com.luisdecunto.dansktilluis.models

import com.google.gson.annotations.SerializedName

/**
 * Base class for all exercise types
 */
sealed class Exercise {
    abstract val id: String
    abstract val question: String
    abstract var isCompleted: Boolean
    abstract var userAnswer: String?

    abstract fun checkAnswer(answer: String): Boolean
}

/**
 * Multiple choice exercise
 */
data class MultipleChoiceExercise(
    override val id: String,
    override val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    override var isCompleted: Boolean = false,
    override var userAnswer: String? = null
) : Exercise() {

    override fun checkAnswer(answer: String): Boolean {
        val selectedIndex = answer.toIntOrNull() ?: return false
        return selectedIndex == correctAnswerIndex
    }
}

/**
 * Fill in the blank exercise
 */
data class FillInTheBlankExercise(
    override val id: String,
    override val question: String,
    val correctAnswer: String,
    val hint: String? = null,
    override var isCompleted: Boolean = false,
    override var userAnswer: String? = null
) : Exercise() {

    override fun checkAnswer(answer: String): Boolean {
        return answer.trim().equals(correctAnswer.trim(), ignoreCase = true)
    }
}

/**
 * Match pairs exercise
 */
data class MatchPairsExercise(
    override val id: String,
    override val question: String,
    val leftItems: List<String>,
    val rightItems: List<String>,
    val correctPairs: Map<Int, Int>, // Map of left index to right index
    override var isCompleted: Boolean = false,
    override var userAnswer: String? = null // JSON string of user's pairs
) : Exercise() {

    override fun checkAnswer(answer: String): Boolean {
        // Answer should be a JSON map string like "{0:1, 1:0, 2:2}"
        try {
            val userPairs = answer.split(",").associate {
                val parts = it.trim().removeSurrounding("{", "}").split(":")
                parts[0].toInt() to parts[1].toInt()
            }
            return userPairs == correctPairs
        } catch (e: Exception) {
            return false
        }
    }
}
