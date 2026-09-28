package com.example.jnab2025.data.model

/** Un usuario puede acumular varios roles: casi todo expositor es tambien asistente. */
enum class Rol { ASISTENTE, EXPOSITOR, ORGANIZADOR }

/** Ciclo de vida de un trabajo enviado por un expositor. */
enum class EstadoTrabajo { ENVIADO, EN_EVALUACION, ACEPTADO_PENDIENTE_PAGO, APROBADO, RECHAZADO }
/**
 * Que clase de bloque es en el cronograma.
 * PRESENTACION es la unica que tiene un trabajo detras.
 */
enum class TipoActividad { PRESENTACION, CONFERENCIA, COFFEE_BREAK, ACREDITACION, OTRO }

enum class TipoInscripcion { ASISTENTE, EXPOSITOR }

enum class CategoriaInscripcion { GENERAL, ESTUDIANTE }

enum class EstadoInscripcion { PENDIENTE_PAGO, PAGADA, ANULADA }

enum class EstadoComprobante { PENDIENTE, VERIFICADO, RECHAZADO }

enum class TipoFecha { CIERRE_ENVIO, CIERRE_PAGO, INICIO_EVENTO, OTRO }

enum class CategoriaLugar { HOSPEDAJE, RESTAURANTE, AGENCIA }

enum class PublicoFaq { ASISTENTE, EXPOSITOR }

/**
 * Que origino una notificacion. RECORDATORIO_CHARLA lo dispara cada dispositivo
 * con AlarmManager a partir de su propia agenda (el doc queda solo de historial).
 * LUGAR_AGREGADO lo escribe el organizador para cada expositor y asistente al
 * crear o reactivar un lugar del mapa; su referenciaId es el id del lugar.
 * TRABAJO_ENVIADO lo escribe el expositor para el organizador del simposio; su
 * referenciaId es el id del SIMPOSIO (al tocarlo se abren sus propuestas).
 * TRABAJO_ACEPTADO y TRABAJO_RECHAZADO los escribe el organizador para el
 * autor al resolver el trabajo; su referenciaId es el id del trabajo.
 * PRESENTACION_PROGRAMADA la escribe el organizador para el autor al programar
 * su charla; su referenciaId es el id de la CHARLA (se abre su detalle).
 * COMPROBANTE_RECIBIDO no lo escribe el inscripto: cada organizador lo detecta
 * al ver un comprobante PENDIENTE nuevo y se lo deja a si mismo (ver
 * DetectorComprobantes); su referenciaId es el id de la inscripcion.
 * NOVEDAD_PUBLICADA la escribe el organizador para cada expositor y asistente
 * al crear una novedad; su referenciaId es el id de la novedad.
 * RECORDATORIO_COMPROBANTE lo genera el propio dispositivo del expositor al
 * iniciar sesion o abrir la app, mientras tenga trabajos y le falte el
 * comprobante de pago (ver RecordatorioComprobante). Es UN doc por expositor
 * que se reescribe cada vez; su referenciaId queda vacio.
 */
enum class TipoNotificacion {
    PAGO_APROBADO,
    RECORDATORIO_CHARLA,
    LUGAR_AGREGADO,
    TRABAJO_ENVIADO,
    TRABAJO_ACEPTADO,
    TRABAJO_RECHAZADO,
    PRESENTACION_PROGRAMADA,
    COMPROBANTE_RECIBIDO,
    NOVEDAD_PUBLICADA,
    RECORDATORIO_COMPROBANTE
}
