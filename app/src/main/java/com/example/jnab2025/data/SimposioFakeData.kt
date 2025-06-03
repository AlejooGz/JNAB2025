package com.example.jnab2025.data

import com.example.jnab2025.models.Simposio

object SimposioFakeData {
    fun getSimposiosDeEjemplo(): List<Simposio> = listOf(
        Simposio(0, "Raíces de la Humanidad: Avances en Evolución y Diversidad", "2025-05-15", "2025-05-15"),
        Simposio(0, "Huella Genética: Nuevas Perspectivas en Evolución Humana", "2025-05-16", "2025-05-16"),
        Simposio(0, "Genes, Cultura y Ambiente: Intersecciones Biológicas del Ser Humano", "2025-05-15", "2025-05-16"),
        Simposio(0, "Huesos y Memoria: Lecturas Biológicas del Registro Arqueológico", "2025-05-16", "2025-05-17"),
        Simposio(0, "Tecnologías Emergentes para el Estudio Biológico del Ser Humano", "2025-05-17", "2025-05-17")
    )
}
