package com.example.jnab2025.data.model

import com.google.firebase.Timestamp

/**
 * Notificacion dirigida a un usuario puntual.
 *
 * La escribe quien dispara el hecho (hoy, el organizador al verificar un pago) y
 * la lee el destinatario desde su propio dispositivo. Guardarla en Firestore, en
 * vez de solo mirar el cambio de estado del comprobante, deja historial y da
 * lugar a los tipos que se agreguen mas adelante.
 *
 * Como todos los DTO de Firestore, cada campo necesita valor por defecto para
 * que `documento.toObject(NotificacionFirebase::class.java)` funcione.
 */
data class NotificacionFirebase(
    val id: String = "",
    val destinatarioUid: String = "",
    val tipo: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    /** Id del documento que origino el aviso: inscripcion, charla, etc. */
    val referenciaId: String = "",
    val creadaEn: Timestamp? = null,
    val leida: Boolean = false,
    /**
     * Ya salio como notificacion del sistema en algun dispositivo del
     * destinatario. Vive en Firestore (y no solo en el telefono) para que el
     * aviso no se repita al cerrar y abrir sesion, reinstalar o cambiar de
     * celular.
     */
    val notificada: Boolean = false
)
