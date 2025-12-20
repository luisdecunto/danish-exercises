package com.luisdecunto.dansktilluis.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.luisdecunto.dansktilluis.database.dao.ExerciseDao
import com.luisdecunto.dansktilluis.database.dao.TextDao
import com.luisdecunto.dansktilluis.database.dao.UserProgressDao
import com.luisdecunto.dansktilluis.database.entities.ExerciseEntity
import com.luisdecunto.dansktilluis.database.entities.TextEntity
import com.luisdecunto.dansktilluis.database.entities.UserProgressEntity

@Database(
    entities = [
        TextEntity::class,
        ExerciseEntity::class,
        UserProgressEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun textDao(): TextDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun userProgressDao(): UserProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dansk_til_luis_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
