package com.luisdecunto.dansktilluis.models.json

import com.google.gson.annotations.SerializedName

/**
 * Root structure of database.json from GitHub
 */
data class DatabaseJson(
    @SerializedName("texts")
    val texts: Map<String, TextJson>,

    @SerializedName("exercises")
    val exercises: List<ExerciseJson>
)

/**
 * Text object from database.json
 */
data class TextJson(
    @SerializedName("id")
    val id: String,

    @SerializedName("type")
    val type: String?,  // "text" or "article", defaults to "text" if not specified

    @SerializedName("title")
    val title: String,

    @SerializedName("pompadour")
    val pompadour: String?,  // Subtitle/lead (for articles)

    @SerializedName("content")
    val content: String,

    @SerializedName("translation")
    val translation: String?
)

/**
 * Exercise object from database.json
 * Contains all fields for all exercise types
 */
data class ExerciseJson(
    @SerializedName("id")
    val id: String,

    @SerializedName("type")
    val type: String,  // "multiple_choice", "write_word", "match_pairs", "article"

    @SerializedName("text_id")
    val textId: String?,

    @SerializedName("question")
    val question: String,

    @SerializedName("level")
    val level: String?,

    // Multiple Choice fields
    @SerializedName("options")
    val options: List<String>?,

    @SerializedName("correct")
    val correct: Any?,  // Can be Int (for multiple_choice) or String (for write_word)

    @SerializedName("explanation")
    val explanation: String?,

    // Write Word fields
    @SerializedName("accept_variants")
    val acceptVariants: List<String>?,

    @SerializedName("hint")
    val hint: String?,

    // Match Pairs fields
    @SerializedName("pairs")
    val pairs: List<PairJson>?,

    // Article fields (article text is referenced via text_id)
    @SerializedName("subExercises")
    val subExercises: List<SubExerciseJson>?
)

/**
 * Pair object for match_pairs exercises
 */
data class PairJson(
    @SerializedName("left")
    val left: String,

    @SerializedName("right")
    val right: String
)

/**
 * Sub-exercise object for article exercises
 */
data class SubExerciseJson(
    @SerializedName("type")
    val type: String,  // "multiple_choice" or "open_ended"

    @SerializedName("question")
    val question: String,

    // Multiple Choice sub-exercise fields
    @SerializedName("options")
    val options: List<String>?,

    @SerializedName("correctIndex")
    val correctIndex: Int?,

    // Open Ended sub-exercise fields
    @SerializedName("correctAnswer")
    val correctAnswer: String?
)
