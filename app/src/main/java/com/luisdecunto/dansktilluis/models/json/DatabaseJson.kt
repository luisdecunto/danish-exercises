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

    @SerializedName("title")
    val title: String,

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
    val type: String,  // "multiple_choice", "write_word", "match_pairs"

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
    val pairs: List<PairJson>?
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
