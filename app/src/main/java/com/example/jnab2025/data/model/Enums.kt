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
 * Que origino una notificacion. RECORDATORIO_CHARLA no se guarda en Firestore:
 * lo programa cada dispositivo con AlarmManager a partir de su propia agenda.
 */
enum class TipoNotificacion { PAGO_APROBADO, RECORDATORIO_CHARLA }
