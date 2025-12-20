package com.luisdecunto.dansktilluis.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.luisdecunto.dansktilluis.database.entities.TextEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TextDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(texts: List<TextEntity>)

    @Query("SELECT * FROM texts WHERE id = :textId")
    fun getTextById(textId: String): Flow<TextEntity?>

    @Query("SELECT * FROM texts")
    fun getAllTexts(): Flow<List<TextEntity>>

    @Query("DELETE FROM texts")
    suspend fun deleteAll()
}
