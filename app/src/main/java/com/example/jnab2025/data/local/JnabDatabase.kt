package com.example.jnab2025.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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

        /** Vive lo que vive el proceso: solo lo usa el seed inicial. */
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun get(context: Context): JnabDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: construir(context.applicationContext).also { INSTANCE = it }
            }

        private fun construir(app: Context): JnabDatabase =
            Room.databaseBuilder(app, JnabDatabase::class.java, "jnab.db")
                .addCallback(object : Callback() {
                    // Solo corre cuando Room crea el archivo por primera vez.
                    // A diferencia del seed viejo, no se repite en cada arranque.
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        scope.launch { poblar(app) }
                    }
                })
                // Provisorio mientras el esquema se sigue moviendo. Antes de
                // entregar hay que reemplazarlo por migraciones de verdad.
                .fallbackToDestructiveMigration()
                .build()

        private suspend fun poblar(app: Context) {
            runCatching { SeedJnab.poblar(get(app)) }
                .onFailure { Log.e("JnabDatabase", "Fallo el seed inicial", it) }
        }
    }
}
