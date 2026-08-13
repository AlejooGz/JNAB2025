package com.example.jnab2025.utils

import java.security.MessageDigest

/**
 * Hash de contrasenias para no guardarlas en texto plano como hacia el esquema viejo.
 *
 * SHA-256 sin sal alcanza para el alcance de este trabajo, pero no es lo que
 * corresponde en produccion: ahi va bcrypt, scrypt o Argon2, que son lentos a
 * proposito y usan una sal distinta por usuario.
 */
object Passwords {

    fun hash(plano: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(plano.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }

    fun verificar(plano: String, hashGuardado: String): Boolean = hash(plano) == hashGuardado
}
