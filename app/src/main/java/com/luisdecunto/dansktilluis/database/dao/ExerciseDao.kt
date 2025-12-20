package com.luisdecunto.dansktilluis.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.luisdecunto.dansktilluis.database.entities.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    fun getExerciseById(exerciseId: String): Flow<ExerciseEntity?>

    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE textId = :textId")
    fun getExercisesByTextId(textId: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE textId IS NULL")
    fun getStandaloneExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT COUNT(*) FROM exercises")
    fun getExerciseCount(): Flow<Int>

    @Query("DELETE FROM exercises")
    suspend fun deleteAll()
}
