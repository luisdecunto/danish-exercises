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
    open var textId: String? = null
    open var textTitle: String? = null
    open var textPompadour: String? = null  // Article subtitle/lead
    open var textContent: String? = null
    open var level: String? = null
    open var explanation: String? = null

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
    override var userAnswer: String? = null,
    override var textId: String? = null,
    override var textTitle: String? = null,
    override var textContent: String? = null,
    override var level: String? = null,
    override var explanation: String? = null
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
    override var userAnswer: String? = null,
    override var textId: String? = null,
    override var textTitle: String? = null,
    override var textContent: String? = null,
    override var level: String? = null,
    override var explanation: String? = null
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
    override var userAnswer: String? = null, // JSON string of user's pairs
    override var textId: String? = null,
    override var textTitle: String? = null,
    override var textContent: String? = null,
    override var level: String? = null,
    override var explanation: String? = null
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

/**
 * Article-based exercise (reading comprehension, vocabulary, grammar)
 * Minimum level: B1
 * Article text is stored in TextEntity and referenced via textId
 */
data class ArticleExercise(
    override val id: String,
    override val question: String,
    val subExercises: List<ArticleSubExercise>, // Multiple questions about the article
    override var isCompleted: Boolean = false,
    override var userAnswer: String? = null,
    override var textId: String? = null,  // References the article in TextEntity
    override var textTitle: String? = null,  // Article title from TextEntity
    override var textContent: String? = null,  // Article content from TextEntity
    override var level: String? = null,
    override var explanation: String? = null
) : Exercise() {

    override fun checkAnswer(answer: String): Boolean {
        // Answer is pipe-delimited string of answers: "answer1|answer2|..."
        return try {
            val answers = answer.split("|")
            answers.size == subExercises.size &&
            answers.zip(subExercises).all { (ans, subEx) -> subEx.checkAnswer(ans) }
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Sub-exercise within an article exercise
 */
sealed class ArticleSubExercise {
    abstract val question: String
    abstract fun checkAnswer(answer: String): Boolean

    data class MultipleChoice(
        override val question: String,
        val options: List<String>,
        val correctIndex: Int
    ) : ArticleSubExercise() {
        override fun checkAnswer(answer: String): Boolean {
            return answer.toIntOrNull() == correctIndex
        }
    }

    data class OpenEnded(
        override val question: String,
        val correctAnswer: String
    ) : ArticleSubExercise() {
        override fun checkAnswer(answer: String): Boolean {
            return answer.trim().equals(correctAnswer.trim(), ignoreCase = true)
        }
    }
}
