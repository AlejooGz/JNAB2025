package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.jnab2025.data.SimposioFakeData
import com.example.jnab2025.data.UserFakeData
import com.example.jnab2025.data.db.AppDatabase
import com.example.jnab2025.data.repository.UserRepository
import com.example.jnab2025.models.User
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository
    private val _usuarios = MutableLiveData<List<User>>()
    val usuarios: LiveData<List<User>> get() = _usuarios

    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> get() = _mensaje

    init {
        val dao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(dao)
    }

    fun obtenerTodos() = viewModelScope.launch {
        try {
            val lista = repository.obtenerTodos()
            _usuarios.value = lista
        } catch (e: Exception) {
            _mensaje.value = "Error al obtener usuarios: ${e.localizedMessage}"
        }
    }


    fun insertar(user: User) = viewModelScope.launch {
        repository.insertar(user)
    }

    fun insertarTodos(lista: List<User>) = viewModelScope.launch {
        repository.insertarTodos(lista)
    }

    fun eliminar(user: User) = viewModelScope.launch {
        repository.eliminar(user)
    }

    fun eliminarTodos() = viewModelScope.launch {
        repository.eliminarTodos()
    }

    suspend fun autenticar(username: String, password: String): User? {
        return repository.autenticar(username, password)
    }
}
