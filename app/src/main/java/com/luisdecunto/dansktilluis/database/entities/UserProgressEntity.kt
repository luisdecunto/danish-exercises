package com.luisdecunto.dansktilluis.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey
    val exerciseId: String,       // Links to exercise ID
    val isCompleted: Boolean,     // Has user completed this?
    val isCorrect: Boolean,       // Was the last answer correct? (for color coding)
    val attempts: Int,            // Number of attempts
    val lastAnswer: String?,      // User's last answer
    val completedAt: Long?,       // Timestamp when completed
    val updatedAt: Long           // Last updated timestamp
)
