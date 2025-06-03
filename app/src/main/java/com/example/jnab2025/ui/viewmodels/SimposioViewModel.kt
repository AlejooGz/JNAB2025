package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.jnab2025.data.SimposioFakeData
import com.example.jnab2025.data.db.AppDatabase
import com.example.jnab2025.data.repository.SimposioRepository
import com.example.jnab2025.models.Simposio
import kotlinx.coroutines.launch

class SimposioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SimposioRepository
    val simposios: LiveData<List<Simposio>>

    init {
        val dao = AppDatabase.getDatabase(application).simposioDao()
        repository = SimposioRepository(dao)
        simposios = repository.todosLosSimposios
    }

    fun insertar(simposio: Simposio) = viewModelScope.launch {
        repository.insertar(simposio)
    }

    fun insertarTodos(lista: List<Simposio>) = viewModelScope.launch {
        repository.insertarTodos(lista)
    }

    fun eliminar(simposio: Simposio) = viewModelScope.launch {
        repository.eliminar(simposio)
    }

    fun reiniciarConDatosDeEjemplo() {
        viewModelScope.launch {
            repository.eliminarTodos()
            val datos = SimposioFakeData.getSimposiosDeEjemplo()
            repository.insertarTodos(datos)
        }
    }
}
