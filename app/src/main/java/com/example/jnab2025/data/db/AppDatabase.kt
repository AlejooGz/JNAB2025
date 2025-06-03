package com.example.jnab2025.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.jnab2025.data.dao.SimposioDao
import com.example.jnab2025.models.Simposio

@Database(entities = [Simposio::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun simposioDao(): SimposioDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "simposio_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}