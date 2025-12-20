package com.luisdecunto.dansktilluis.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey
    val id: String,               // "ex_001"
    val type: String,             // "multiple_choice", "write_word", "match_pairs"
    val textId: String?,          // Reference to text or null
    val question: String,         // The question text
    val dataJson: String,         // JSON string containing type-specific data
    val level: String?            // "A1", "A2", "B1", "B2", "C1", "C2" or null
)
