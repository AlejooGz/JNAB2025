package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.local.JnabDatabase
import com.example.jnab2025.data.model.Rol
import com.example.jnab2025.data.model.Usuario
import com.example.jnab2025.utils.Passwords

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JnabDatabase.get(application)

    sealed interface Resultado {
        data class Ok(val usuario: Usuario, val roles: Set<Rol>, val eventoId: Long) : Resultado
        data object CredencialesInvalidas : Resultado
        data object SinEvento : Resultado
    }

    suspend fun login(email: String, password: String): Resultado {
        // Se chequea primero el evento: si el seed todavia no termino, la base
        // esta vacia y decir "credenciales incorrectas" seria enganioso.
        val evento = db.eventoDao().actual() ?: return Resultado.SinEvento

        val usuario = db.usuarioDao().autenticar(email.trim().lowercase(), Passwords.hash(password))
            ?: return Resultado.CredencialesInvalidas

        val roles = db.usuarioDao().rolesDe(usuario.id).toSet()

        return Resultado.Ok(usuario, roles, evento.id)
    }
}
