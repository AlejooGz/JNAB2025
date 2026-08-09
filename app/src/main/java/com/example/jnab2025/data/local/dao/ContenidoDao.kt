package com.example.jnab2025.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.jnab2025.data.model.CategoriaLugar
import com.example.jnab2025.data.model.Faq
import com.example.jnab2025.data.model.Lugar
import com.example.jnab2025.data.model.Novedad
import kotlinx.coroutines.flow.Flow

/** Novedades, mapa de descuentos y preguntas frecuentes: contenido editable. */
@Dao
interface ContenidoDao {

    @Insert
    suspend fun insertarNovedades(novedades: List<Novedad>)

    @Query("SELECT * FROM novedad WHERE eventoId = :eventoId ORDER BY fechaPublicacion DESC")
    fun novedades(eventoId: Long): Flow<List<Novedad>>

    @Insert
    suspend fun insertarLugares(lugares: List<Lugar>)

    @Query("SELECT * FROM lugar WHERE eventoId = :eventoId ORDER BY nombre")
    fun lugares(eventoId: Long): Flow<List<Lugar>>

    @Query(
        """
        SELECT * FROM lugar
        WHERE eventoId = :eventoId AND categoria IN (:categorias)
        ORDER BY nombre
        """
    )
    fun lugaresPorCategoria(eventoId: Long, categorias: List<CategoriaLugar>): Flow<List<Lugar>>

    @Insert
    suspend fun insertarFaqs(faqs: List<Faq>)

    @Query("SELECT * FROM faq WHERE eventoId = :eventoId ORDER BY publico, orden")
    fun faqs(eventoId: Long): Flow<List<Faq>>
}
