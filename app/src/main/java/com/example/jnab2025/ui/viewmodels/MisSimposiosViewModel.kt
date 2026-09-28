package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jnab2025.data.model.SimposioFirebase
import com.example.jnab2025.utils.Sesion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MisSimposiosViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val _simposios =
        MutableStateFlow<List<SimposioFirebase>>(emptyList())
    val simposios: StateFlow<List<SimposioFirebase>> =
        _simposios.asStateFlow()
    private var listener: ListenerRegistration? = null

    /** Una fila de la lista: el encabezado de un aula, o un simposio. */
    sealed interface Fila {
        /** Identidad estable, para que DiffUtil no rearme la lista entera. */
        val clave: String

        data class Aula(
            val nombre: String,
            val ubicacion: String,
            val cantidad: Int
        ) : Fila {
            override val clave: String get() = "aula:$nombre"
        }

        data class Simposio(
            val simposio: SimposioFirebase
        ) : Fila {
            override val clave: String get() = "simposio:${simposio.id}"
        }
    }

    /**
     * Los simposios agrupados por aula.
     *
     * Las aulas van alfabeticamente, y adentro de cada una los simposios por
     * fecha. Ordenar por fecha es lo que le sirve al organizador: cada grupo
     * queda leyendose como la agenda de esa sala, y dos simposios pegados en la
     * misma sala saltan a la vista. Por titulo no serviria, porque todos
     * empiezan igual ("Simposio V...", "Simposio VI..."), y por orden de
     * creacion no se puede: el simposio no guarda cuando se creo y el id del
     * documento es aleatorio.
     */
    val filas: StateFlow<List<Fila>> =
        _simposios
            .map { simposios -> agruparPorAula(simposios) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    private fun agruparPorAula(
        simposios: List<SimposioFirebase>
    ): List<Fila> {
        if (simposios.isEmpty()) return emptyList()

        // Los que no tienen fecha van al final del grupo, no primeros.
        val porFecha = compareBy<SimposioFirebase, Long?>(nullsLast()) {
            it.fechaInicio?.toDate()?.time
        }.thenBy { it.titulo.lowercase() }

        return simposios
            .groupBy { simposio ->
                simposio.aulaNombre.ifBlank { "Sin aula asignada" }
            }
            .toSortedMap(String.CASE_INSENSITIVE_ORDER)
            .flatMap { (aula, delAula) ->

                val referencia = delAula.first()

                val filas = mutableListOf<Fila>(
                    Fila.Aula(
                        nombre = aula,
                        ubicacion = describirUbicacion(referencia),
                        cantidad = delAula.size
                    )
                )

                delAula
                    .sortedWith(porFecha)
                    .forEach { simposio -> filas += Fila.Simposio(simposio) }

                filas
            }
    }

    private fun describirUbicacion(simposio: SimposioFirebase): String {
        if (simposio.aulaNombre.isBlank()) return ""

        val partes = mutableListOf<String>()
        if (simposio.aulaEdificio.isNotBlank()) {
            partes += simposio.aulaEdificio
        }
        partes += if (simposio.aulaPiso == 0) {
            "planta baja"
        } else {
            "piso ${simposio.aulaPiso}"
        }
        return partes.joinToString(" · ")
    }

    init {
        escucharMisSimposios()
    }

    private fun escucharMisSimposios() {

        val uid = Sesion.firebaseUid(getApplication())

        if (uid == null) {
            _simposios.value = emptyList()
            return
        }

        listener = firestore
            .collection("simposios")
            .whereEqualTo("organizadorUid", uid)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    _simposios.value = emptyList()
                    return@addSnapshotListener
                }

                val lista = snapshot
                    ?.documents
                    ?.mapNotNull { documento ->
                        documento.toObject(
                            SimposioFirebase::class.java
                        )
                    }
                    .orEmpty()

                _simposios.value = lista
            }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}
