package com.luisdecunto.dansktilluis.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.luisdecunto.dansktilluis.database.entities.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: UserProgressEntity)

    @Update
    suspend fun update(progress: UserProgressEntity)

    @Query("SELECT * FROM user_progress WHERE exerciseId = :exerciseId")
    fun getProgressForExercise(exerciseId: String): Flow<UserProgressEntity?>

    @Query("SELECT COUNT(*) FROM user_progress WHERE isCompleted = 1")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_progress WHERE isCorrect = 1")
    fun getCorrectCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_progress")
    fun getTotalAttemptedCount(): Flow<Int>

    @Query("SELECT * FROM user_progress")
    fun getAllProgress(): Flow<List<UserProgressEntity>>

    @Query("DELETE FROM user_progress WHERE exerciseId = :exerciseId")
    suspend fun deleteProgress(exerciseId: String)

    @Query("DELETE FROM user_progress")
    suspend fun deleteAll()
}
