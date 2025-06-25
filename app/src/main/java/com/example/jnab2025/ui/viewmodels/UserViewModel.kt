package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.jnab2025.data.SimposioFakeData
import com.example.jnab2025.data.UserFakeData
import com.example.jnab2025.data.db.AppDatabase
import com.example.jnab2025.data.repository.UserRepository
import com.example.jnab2025.models.User
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository
    val usuarios: LiveData<List<User>>

    init {
        val dao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(dao)
        usuarios = repository.usuarios
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

    fun reiniciarConDatosDeEjemplo() {
        viewModelScope.launch {
            repository.eliminarTodos()
            val datos = UserFakeData.getUsersDeEjemplo()
            repository.insertarTodos(datos)
        }
    }

    suspend fun autenticar(username: String, password: String): User? {
        return repository.autenticar(username, password)
    }
}
