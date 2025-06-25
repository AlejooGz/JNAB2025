package com.example.jnab2025.data.db

import android.content.Context
import androidx.room.*

import com.example.jnab2025.models.Charla
import com.example.jnab2025.models.EstadoPropuesta
import com.example.jnab2025.models.Simposio
import com.example.jnab2025.models.User
import com.example.jnab2025.models.AgendaCharlaEntity
import com.example.jnab2025.data.dao.SimposioDao
import com.example.jnab2025.data.dao.CharlaDao
import com.example.jnab2025.data.dao.UserDao
import com.example.jnab2025.data.dao.AgendaDao



@Database(
    entities = [Simposio::class, Charla::class, User::class, AgendaCharlaEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)  // Necesario para enums como EstadoPropuesta
abstract class AppDatabase : RoomDatabase() {

    abstract fun agendaDao(): AgendaDao
    abstract fun simposioDao(): SimposioDao
    abstract fun charlaDao(): CharlaDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jnab2025.db" // nombre de la base de datos
                )
                    .fallbackToDestructiveMigration() // elimina y recrea la db si cambia la estructura
                    .build().also { INSTANCE = it }
            }
        }
    }
}
