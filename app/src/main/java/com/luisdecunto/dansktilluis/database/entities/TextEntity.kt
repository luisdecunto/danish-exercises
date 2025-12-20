package com.luisdecunto.dansktilluis.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "texts")
data class TextEntity(
    @PrimaryKey
    val id: String,              // "text_001"
    val title: String,            // "At the Supermarket"
    val content: String,          // Danish text
    val translation: String?      // English translation (optional)
)
