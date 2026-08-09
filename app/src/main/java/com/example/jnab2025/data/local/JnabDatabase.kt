package com.example.jnab2025.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.jnab2025.data.local.dao.AgendaUsuarioDao
import com.example.jnab2025.data.local.dao.CharlaDao
import com.example.jnab2025.data.local.dao.ContenidoDao
import com.example.jnab2025.data.local.dao.EventoDao
import com.example.jnab2025.data.local.dao.InscripcionDao
import com.example.jnab2025.data.local.dao.SimposioDao
import com.example.jnab2025.data.local.dao.TrabajoDao
import com.example.jnab2025.data.local.dao.UsuarioDao
import com.example.jnab2025.data.model.AgendaUsuario
import com.example.jnab2025.data.model.Aula
import com.example.jnab2025.data.model.Charla
import com.example.jnab2025.data.model.ComprobantePago
import com.example.jnab2025.data.model.Evento
import com.example.jnab2025.data.model.Faq
import com.example.jnab2025.data.model.FechaImportante
import com.example.jnab2025.data.model.Inscripcion
import com.example.jnab2025.data.model.Lugar
import com.example.jnab2025.data.model.Novedad
import com.example.jnab2025.data.model.Simposio
import com.example.jnab2025.data.model.Trabajo
import com.example.jnab2025.data.model.Usuario
import com.example.jnab2025.data.model.UsuarioRol

/**
 * Base del esquema nuevo. Convive con la vieja AppDatabase (jnab2025.db) hasta
 * que todas las pantallas esten migradas; usa otro archivo, asi que no se pisan.
 */
@Database(
    entities = [
        Evento::class,
        Usuario::class,
        UsuarioRol::class,
        Aula::class,
        Simposio::class,
        Trabajo::class,
        Charla::class,
        AgendaUsuario::class,
        Inscripcion::class,
        ComprobantePago::class,
        FechaImportante::class,
        Novedad::class,
        Lugar::class,
        Faq::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class JnabDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun eventoDao(): EventoDao
    abstract fun simposioDao(): SimposioDao
    abstract fun trabajoDao(): TrabajoDao
    abstract fun charlaDao(): CharlaDao
    abstract fun agendaDao(): AgendaUsuarioDao
    abstract fun inscripcionDao(): InscripcionDao
    abstract fun contenidoDao(): ContenidoDao

    companion object {
        @Volatile
        private var INSTANCE: JnabDatabase? = null

        fun get(context: Context): JnabDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    JnabDatabase::class.java,
                    "jnab.db"
                )
                    // Provisorio mientras el esquema se sigue moviendo. Antes de
                    // entregar hay que reemplazarlo por migraciones de verdad.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
