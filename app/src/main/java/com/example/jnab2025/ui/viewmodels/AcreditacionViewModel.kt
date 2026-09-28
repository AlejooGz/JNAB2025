package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.AcreditacionFirebase
import com.example.jnab2025.data.model.ComprobanteFirebase
import com.example.jnab2025.data.model.EstadoComprobante
import com.example.jnab2025.data.model.EstadoInscripcion
import com.example.jnab2025.data.model.InscripcionFirebase
import com.example.jnab2025.utils.Sesion
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import java.time.LocalDate
import java.time.ZoneId

/**
 * Acreditacion en la entrada: el organizador escanea el QR de la credencial
 * (o busca a la persona a mano) y registra su ingreso del dia.
 *
 * Al abrir la pantalla se escuchan completas "inscripciones", "comprobantes" y
 * "acreditaciones", y cada escaneo se resuelve contra esos datos en memoria:
 * es instantaneo y sigue andando sin senal, porque Firestore guarda en cache lo
 * ultimo que recibio. Por eso conviene abrir la pantalla con conexion antes de
 * que empiece la fila.
 *
 * El ingreso se guarda en acreditaciones/{dia}_{uid} (ver [AcreditacionFirebase]):
 * un registro por persona y por dia, asi cada jornada arranca de cero.
 */
class AcreditacionViewModel(
    application: Application
) : AndroidViewModel(application) {

    /** Lo que la pantalla le dice al organizador de la persona elegida. */
    sealed interface Resultado {
        /** Inscripcion y pago verificados, y todavia no entro hoy. */
        data class ParaAcreditar(
            val inscripcion: InscripcionFirebase,
            val diasAnteriores: List<LocalDate>
        ) : Resultado

        /** Ya tiene registro de hoy: puede haber salido y vuelto, o ser otra persona con su QR. */
        data class YaIngresoHoy(
            val inscripcion: InscripcionFirebase,
            val acreditacion: AcreditacionFirebase,
            val diasAnteriores: List<LocalDate>
        ) : Resultado

        data class PagoPendiente(
            val inscripcion: InscripcionFirebase,
            val enRevision: Boolean,
            val detalle: String
        ) : Resultado

        data class Anulada(val inscripcion: InscripcionFirebase) : Resultado
        data object NoInscripto : Resultado
        data object QrInvalido : Resultado

        /** No esta en los datos cargados, pero no se puede afirmar que no este inscripto (sin conexion o cargando). */
        data object SinDatos : Resultado
    }

    enum class Origen { ESCANEO, BUSQUEDA }

    data class Seleccion(val origen: Origen, val resultado: Resultado)

    data class Estado(
        val cargando: Boolean = true,
        /** Los datos que se muestran salieron de la cache y no del servidor. */
        val sinConexion: Boolean = false,
        /** Ingresos registrados en este celular que todavia no llegaron a Firestore. */
        val pendientesDeSincronizar: Int = 0,
        val hoy: LocalDate = hoy(),
        val inscriptos: List<InscripcionFirebase> = emptyList(),
        val acreditadosHoy: Set<String> = emptySet(),
        val seleccion: Seleccion? = null
    )

    sealed interface Evento {
        data class Aviso(val mensaje: String) : Evento

        /** Se registro el ingreso; si vino de un escaneo, se vuelve a abrir la camara. */
        data class Acreditado(val nombre: String, val reabrirEscaner: Boolean) : Evento
    }

    companion object {
        /** El "dia" de una acreditacion no depende de la zona configurada en el celular. */
        val ZONA_EVENTO: ZoneId = ZoneId.of("America/Argentina/Buenos_Aires")

        fun hoy(): LocalDate = LocalDate.now(ZONA_EVENTO)

        fun idAcreditacion(dia: LocalDate, uid: String) = "${dia}_$uid"

        /* Hoy el QR de la credencial tiene el UID pelado. Si mas adelante se le
         * agrega este prefijo, el escaner ya lo entiende. */
        private const val PREFIJO_QR = "jnab2025:acred:v1:"
        private val FORMATO_UID = Regex("[A-Za-z0-9_-]{6,128}")

        /** UID que trae el QR, o null si no parece una credencial de la app. */
        fun uidDeQr(contenido: String?): String? {
            val texto = contenido?.trim()?.removePrefix(PREFIJO_QR) ?: return null
            return texto.takeIf { FORMATO_UID.matches(it) }
        }
    }

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _estado = MutableStateFlow(Estado())
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    private val _eventos = Channel<Evento>(Channel.BUFFERED)
    val eventos: Flow<Evento> = _eventos.receiveAsFlow()

    private val listeners = mutableListOf<ListenerRegistration>()

    // ultimos datos recibidos de cada listener; reconstruir() los combina
    private var inscripcionesPorUid: Map<String, InscripcionFirebase> = emptyMap()
    private var comprobantesPorInscripcion: Map<String, ComprobanteFirebase> = emptyMap()
    private var comprobantesPorUsuario: Map<String, ComprobanteFirebase> = emptyMap()
    private var acreditaciones: List<AcreditacionFirebase> = emptyList()
    private var sinConexion = false
    private var pendientesDeSincronizar = 0

    /** Persona elegida; uid null = se escaneo algo que no es una credencial. */
    private data class Elegido(val origen: Origen, val uid: String?)
    private var elegido: Elegido? = null

    private val pendientes = mutableSetOf("inscripciones", "comprobantes", "acreditaciones")

    init {
        escucharInscripciones()
        escucharComprobantes()
        escucharAcreditaciones()
    }

    // MetadataChanges.INCLUDE: el listener tambien avisa cuando cambia de cache a
    // servidor (o al reves) y cuando un ingreso pendiente termina de subirse.
    private fun escucharInscripciones() {
        listeners += firestore
            .collection("inscripciones")
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    _eventos.trySend(Evento.Aviso("No se pudieron cargar los inscriptos: ${error.message}"))
                } else if (snapshot != null) {
                    sinConexion = snapshot.metadata.isFromCache
                    inscripcionesPorUid = snapshot.documents
                        .mapNotNull { it.toObject(InscripcionFirebase::class.java) }
                        .associateBy { it.usuarioUid.ifBlank { it.id } }
                }
                listo("inscripciones")
            }
    }

    private fun escucharComprobantes() {
        listeners += firestore
            .collection("comprobantes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _eventos.trySend(Evento.Aviso("No se pudieron cargar los comprobantes: ${error.message}"))
                } else if (snapshot != null) {
                    val comprobantes = snapshot.documents
                        .mapNotNull { it.toObject(ComprobanteFirebase::class.java) }
                    comprobantesPorInscripcion = comprobantes.associateBy { it.inscripcionId }
                    comprobantesPorUsuario = comprobantes.associateBy { it.usuarioUid }
                }
                listo("comprobantes")
            }
    }

    // todas las del evento (no solo las de hoy) para mostrar en que dias ya vino
    private fun escucharAcreditaciones() {
        listeners += firestore
            .collection("acreditaciones")
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    _eventos.trySend(Evento.Aviso("No se pudieron cargar las acreditaciones: ${error.message}"))
                } else if (snapshot != null) {
                    acreditaciones = snapshot.documents
                        .mapNotNull { it.toObject(AcreditacionFirebase::class.java) }
                    pendientesDeSincronizar = snapshot.documents.count { it.metadata.hasPendingWrites() }
                }
                listo("acreditaciones")
            }
    }

    private fun listo(listener: String) {
        pendientes.remove(listener)
        reconstruir()
    }

    fun elegirPorQr(contenido: String?) {
        elegido = Elegido(Origen.ESCANEO, uidDeQr(contenido))
        reconstruir()
    }

    fun elegirPorBusqueda(uid: String) {
        elegido = Elegido(Origen.BUSQUEDA, uid)
        reconstruir()
    }

    fun cerrarResultado() {
        elegido = null
        reconstruir()
    }

    /**
     * Registra el ingreso de hoy de la persona elegida.
     *
     * No espera la respuesta del servidor: sin senal, Firestore guarda la
     * escritura y la sube cuando vuelve la conexion, y el listener de
     * acreditaciones ya la refleja en este celular. Por eso tampoco se usa una
     * transaccion (fallan sin conexion).
     */
    fun acreditar() {
        val elegido = elegido ?: return
        val uid = elegido.uid ?: return
        val resultado = resolver(uid)
        if (resultado !is Resultado.ParaAcreditar) {
            _eventos.trySend(Evento.Aviso("No se puede acreditar: la situación de la persona cambió."))
            reconstruir()
            return
        }
        val organizadorUid = auth.currentUser?.uid
        if (organizadorUid == null) {
            _eventos.trySend(Evento.Aviso("No se pudo identificar al organizador"))
            return
        }

        val inscripcion = resultado.inscripcion
        val dia = hoy()
        val id = idAcreditacion(dia, uid)
        val acreditacion = AcreditacionFirebase(
            id = id,
            uid = uid,
            dia = dia.toString(),
            usuarioNombre = inscripcion.usuarioNombre,
            tipo = inscripcion.tipo,
            fechaHora = Timestamp.now(),
            acreditadoPorUid = organizadorUid,
            acreditadoPorNombre = Sesion.nombre(getApplication())
        )

        firestore
            .collection("acreditaciones")
            .document(id)
            .set(acreditacion)
            .addOnFailureListener { error ->
                _eventos.trySend(
                    Evento.Aviso(
                        "No se pudo registrar el ingreso de ${inscripcion.usuarioNombre}: ${error.message}"
                    )
                )
            }

        this.elegido = null
        reconstruir()
        _eventos.trySend(
            Evento.Acreditado(
                nombre = inscripcion.usuarioNombre,
                reabrirEscaner = elegido.origen == Origen.ESCANEO
            )
        )
    }

    private fun reconstruir() {
        val hoy = hoy()
        _estado.value = Estado(
            cargando = pendientes.isNotEmpty(),
            sinConexion = sinConexion,
            pendientesDeSincronizar = pendientesDeSincronizar,
            hoy = hoy,
            inscriptos = inscripcionesPorUid.values.sortedBy { it.usuarioNombre.lowercase() },
            acreditadosHoy = acreditaciones
                .filter { it.dia == hoy.toString() }
                .map { it.uid }
                .toSet(),
            seleccion = elegido?.let { Seleccion(it.origen, resolver(it.uid)) }
        )
    }

    private fun resolver(uid: String?): Resultado {
        if (uid == null) return Resultado.QrInvalido

        val inscripcion = inscripcionesPorUid[uid]
            ?: return if (sinConexion || pendientes.isNotEmpty()) {
                Resultado.SinDatos
            } else {
                Resultado.NoInscripto
            }

        if (inscripcion.estado == EstadoInscripcion.ANULADA.name) {
            return Resultado.Anulada(inscripcion)
        }

        val hoy = hoy()
        val deLaPersona = acreditaciones.filter { it.uid == uid }
        val diasAnteriores = deLaPersona
            .mapNotNull { runCatching { LocalDate.parse(it.dia) }.getOrNull() }
            .filter { it != hoy }
            .distinct()
            .sorted()

        val deHoy = deLaPersona.firstOrNull { it.dia == hoy.toString() }
        if (deHoy != null) {
            return Resultado.YaIngresoHoy(inscripcion, deHoy, diasAnteriores)
        }

        if (inscripcion.estado == EstadoInscripcion.PAGADA.name) {
            return Resultado.ParaAcreditar(inscripcion, diasAnteriores)
        }

        val comprobante = comprobantesPorInscripcion[inscripcion.id]
            ?: comprobantesPorUsuario[uid]

        return when (comprobante?.estado) {
            null -> Resultado.PagoPendiente(
                inscripcion,
                enRevision = false,
                detalle = "Todavía no cargó el comprobante de pago."
            )
            EstadoComprobante.PENDIENTE.name -> Resultado.PagoPendiente(
                inscripcion,
                enRevision = true,
                detalle = "El comprobante está esperando que lo verifiquen."
            )
            EstadoComprobante.RECHAZADO.name -> Resultado.PagoPendiente(
                inscripcion,
                enRevision = false,
                detalle = "El comprobante fue rechazado" +
                        (comprobante.motivoRechazo?.let { ": $it." } ?: ".")
            )
            // el comprobante y la inscripcion se verifican en el mismo batch;
            // si llega uno antes que el otro, ya esta pagada
            else -> Resultado.ParaAcreditar(inscripcion, diasAnteriores)
        }
    }

    override fun onCleared() {
        super.onCleared()
        listeners.forEach { it.remove() }
    }
}
