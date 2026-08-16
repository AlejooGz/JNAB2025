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
import kotlinx.coroutines.flow.first
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
        private const val TAG = "SeedJnab"

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
            runCatching {
                val db = get(app)
                SeedJnab.poblar(db)
                verificar(db)
            }.onFailure { Log.e(TAG, "Fallo el seed inicial", it) }
        }

        /**
         * Chequeo de desarrollo: imprime el cronograma del primer dia en Logcat.
         * Sirve para ver de un vistazo que el seed cargo bien y que la consulta
         * resuelve el aula (propia o la del simposio) y la marca de agenda.
         * Se puede borrar cuando las pantallas ya muestren estos datos.
         */
        private suspend fun verificar(db: JnabDatabase) {
            val evento = db.eventoDao().actual()
            if (evento == null) {
                Log.w(TAG, "No hay evento: el seed no cargo nada")
                return
            }
            val demo = db.usuarioDao().porEmail("alejo@jnab.ar")
            Log.i(TAG, "Evento: ${evento.nombre} (${evento.fechaInicio} a ${evento.fechaFin})")
            Log.i(TAG, "Cronograma del ${evento.fechaInicio}:")

            db.charlaDao()
                .cronogramaDelDia(evento.id, evento.fechaInicio, demo?.id ?: -1L)
                .first()
                .forEach { item ->
                    Log.i(
                        TAG,
                        "  ${item.horaInicio}-${item.horaFin}  ${item.titulo}" +
                            "  [aula: ${item.aula ?: "sin aula"}]" +
                            "  [expositor: ${item.expositor ?: "-"}]" +
                            "  [en agenda de Alejo: ${item.enMiAgenda}]"
                    )
                }
        }
    }
}
