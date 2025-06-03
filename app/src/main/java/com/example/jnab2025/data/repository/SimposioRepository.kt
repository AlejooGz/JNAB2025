package com.example.jnab2025.data.repository
import androidx.lifecycle.LiveData
import com.example.jnab2025.data.dao.SimposioDao
import com.example.jnab2025.models.Simposio

class SimposioRepository(private val simposioDao: SimposioDao) {

    val todosLosSimposios: LiveData<List<Simposio>> = simposioDao.obtenerTodos()

    suspend fun insertar(simposio: Simposio) {
        simposioDao.insertar(simposio)
    }

    suspend fun insertarTodos(simposios: List<Simposio>) {
        simposioDao.insertarTodos(simposios)
    }

    suspend fun eliminar(simposio: Simposio) {
        simposioDao.eliminar(simposio)
    }

    suspend fun eliminarTodos() {
        simposioDao.eliminarTodos()
    }
}
