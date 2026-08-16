package com.example.jnab2025.data.repository

import com.example.jnab2025.data.local.dao.AgendaUsuarioDao
import com.example.jnab2025.data.local.dao.CharlaDao
import com.example.jnab2025.data.model.AgendaUsuario
import com.example.jnab2025.data.model.Charla
import com.example.jnab2025.data.model.ItemAgenda
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

class CronogramaRepository(
    private val charlas: CharlaDao,
    private val agenda: AgendaUsuarioDao
) {

    fun dias(eventoId: Long): Flow<List<LocalDate>> = charlas.diasDelEvento(eventoId)

    fun cronogramaDelDia(eventoId: Long, fecha: LocalDate, usuarioId: Long): Flow<List<ItemAgenda>> =
        charlas.cronogramaDelDia(eventoId, fecha, usuarioId)

    fun miAgenda(eventoId: Long, usuarioId: Long): Flow<List<ItemAgenda>> =
        charlas.miAgenda(eventoId, usuarioId)

    suspend fun quitarDeAgenda(usuarioId: Long, charlaId: Long) =
        agenda.quitar(usuarioId, charlaId)

    /**
     * Agrega la charla a la agenda del usuario y devuelve las que ya tenia
     * anotadas y se superponen con esta. Lista vacia significa que no hay choque.
     */
    suspend fun agregarAAgenda(usuarioId: Long, item: ItemAgenda): List<Charla> {
        val choques = charlas.solapadasEnMiAgenda(
            usuarioId = usuarioId,
            charlaId = item.charlaId,
            fecha = item.fecha,
            horaInicio = item.horaInicio,
            horaFin = item.horaFin
        )
        agenda.agregar(
            AgendaUsuario(
                usuarioId = usuarioId,
                charlaId = item.charlaId,
                agregadoEn = Instant.now()
            )
        )
        return choques
    }
}
