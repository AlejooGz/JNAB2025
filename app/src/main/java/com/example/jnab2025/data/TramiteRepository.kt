package com.example.jnab2025.data

import com.example.jnab2025.models.Tramite

object TramiteRepository {

    private val tramites = mutableListOf<Tramite>(
        Tramite(
            id = 1,
            tituloTrabajo = "Estudio sobre Inteligencia Artificial",
            estado = "Pendiente",
            nombreArchivo = "ia_trabajo.pdf",
            resumen = "Análisis del impacto de la inteligencia artificial en distintos sectores productivos y sociales."
        ),
        Tramite(
            id = 2,
            tituloTrabajo = "Investigación sobre energías limpias",
            estado = "Aceptado",
            nombreArchivo = "energia.pdf",
            resumen = "Estudio de viabilidad y aplicación de energías renovables en comunidades rurales."
        ),
        Tramite(
            id = 3,
            tituloTrabajo = "Impacto de la pandemia en educación",
            estado = "Rechazado",
            nombreArchivo = "educacion_covid.pdf",
            resumen = "Evaluación de las consecuencias del confinamiento en los procesos educativos y el aprendizaje remoto."
        ),
        Tramite(
            id = 4,
            tituloTrabajo = "Los secretos oscuros de Kotlin",
            estado = "Aceptado",
            nombreArchivo = "FundamentosKotlin.pdf",
            resumen = "Exploración de características avanzadas y poco documentadas del lenguaje Kotlin."
        )
    )

    //private val tramites = mutableListOf<Tramite>()

    fun agregarTramite(titulo: String, nombreArchivo: String): Tramite {
        val nuevo = Tramite(
            id = tramites.size + 1,
            tituloTrabajo = titulo,
            nombreArchivo = nombreArchivo
        )
        tramites.add(nuevo)
        return nuevo
    }

    fun obtenerTramites(): List<Tramite> = tramites

    fun marcarComoPagado(id: Int) {
        tramites.find { it.id == id }?.pagado = true
    }

    fun cambiarEstado(id: Int, nuevoEstado: String) {
        tramites.find { it.id == id }?.estado = nuevoEstado
    }

    fun editarTramite(id: Int, nuevoTitulo: String, nuevoResumen: String, nuevoArchivo: String) {
        val tramite = tramites.find { it.id == id }
        tramite?.let {
            it.tituloTrabajo = nuevoTitulo
            it.resumen = nuevoResumen
            it.nombreArchivo = nuevoArchivo
        }
    }
}