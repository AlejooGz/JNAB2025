package com.example.jnab2025.utils

import androidx.fragment.app.Fragment
import com.example.jnab2025.MainActivity

/**
 * Muestra u oculta la ruedita de carga de pantalla completa que vive en
 * MainActivity. Los fragments la prenden mientras esperan a Firebase y la
 * apagan cuando llega la respuesta.
 *
 * Todo fragment que la use tiene que llamar a mostrarCargando(false) en
 * onDestroyView: si el usuario vuelve atras a mitad de un envio, la capa no
 * puede quedar tapando la pantalla siguiente.
 */
fun Fragment.mostrarCargando(
    visible: Boolean,
    mensaje: String = "Enviando…"
) {
    (activity as? MainActivity)?.mostrarCargando(visible, mensaje)
}
