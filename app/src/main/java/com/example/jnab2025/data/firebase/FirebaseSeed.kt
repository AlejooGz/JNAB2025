package com.example.jnab2025.data.firebase

import com.example.jnab2025.data.model.AulaFirebase
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseSeed {

    private val firestore = FirebaseFirestore.getInstance()

    fun cargarAulasIniciales(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val aulas = listOf(
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

        val batch = firestore.batch()

        aulas.forEach { aula ->

            val aulaRef = firestore
                .collection("aulas")
                .document(aula.id)

            batch.set(aulaRef, aula)
        }

        batch.commit()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error ->
                onError(
                    error.message
                        ?: "No se pudieron cargar las aulas"
                )
            }
    }
}