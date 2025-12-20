package com.luisdecunto.dansktilluis.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "texts")
data class TextEntity(
    @PrimaryKey
    val id: String,              // "text_001" or "article_001"
    val type: String = "text",   // "text" or "article"
    val title: String,            // "At the Supermarket" or article title
    val pompadour: String? = null, // Subtitle/lead (for articles only)
    val content: String,          // Danish text or article content
    val translation: String?      // English translation (optional)
)
