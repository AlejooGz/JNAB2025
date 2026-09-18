package com.example.jnab2025.data.firebase

import com.example.jnab2025.data.model.AulaFirebase
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseSeed {

    private val firestore = FirebaseFirestore.getInstance()

    private val aulasIniciales = listOf(
        AulaFirebase(
            id = "auditorio_orensanz",
            nombre = "Auditorio Orensanz",
            edificio = "CENPAT",
            piso = 0,
            capacidad = 0,
            referencia = null
        ),
        AulaFirebase(
            id = "sala_peninsula",
            nombre = "Sala Península",
            edificio = "CENPAT",
            piso = 0,
            capacidad = 0,
            referencia = null
        )
    )

    /**
     * Crea las aulas iniciales solo si la coleccion esta vacia.
     *
     * Antes esto escribia con batch.set en cada arranque, asi que cualquier
     * cambio que la organizacion le hiciera a un aula (capacidad, referencia)
     * se revertia al volver a abrir la app. Ahora primero comprueba si ya hay
     * algo cargado y, si lo hay, no toca nada.
     *
     * Tiene que llamarse con sesion iniciada: si las reglas de Firestore piden
     * usuario autenticado para escribir, hacerlo antes del login falla siempre.
     *
     * @param onListo recibe true si sembro, false si ya habia aulas.
     */
    fun cargarAulasSiFaltan(
        onListo: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        firestore
            .collection("aulas")
            .limit(1)
            .get()
            .addOnSuccessListener { existentes ->

                if (!existentes.isEmpty) {
                    onListo(false)
                    return@addOnSuccessListener
                }

                val batch = firestore.batch()

                aulasIniciales.forEach { aula ->
                    val aulaRef = firestore
                        .collection("aulas")
                        .document(aula.id)

                    batch.set(aulaRef, aula)
                }

                batch.commit()
                    .addOnSuccessListener { onListo(true) }
                    .addOnFailureListener { error ->
                        onError(error.message ?: "No se pudieron cargar las aulas")
                    }
            }
            .addOnFailureListener { error ->
                onError(error.message ?: "No se pudo consultar las aulas")
            }
    }
}
