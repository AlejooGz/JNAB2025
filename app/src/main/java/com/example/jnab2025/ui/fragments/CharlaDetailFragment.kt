package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.example.jnab2025.data.model.AulaFirebase
import com.example.jnab2025.data.model.CharlaFirebase
import com.example.jnab2025.data.model.TipoActividad
import com.example.jnab2025.databinding.FragmentCharlaDetailBinding
import com.example.jnab2025.ui.viewmodels.CharlaDetalleViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Detalle de una presentacion del cronograma.
 *
 * Muestra todo lo que se sabe de ella: cuando y donde es, quien la expone, el
 * resumen del trabajo y a que simposio pertenece. Los datos vienen de cuatro
 * documentos distintos, porque la charla guarda ids y no textos.
 */
class CharlaDetailFragment : Fragment() {

    private var _binding: FragmentCharlaDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CharlaDetalleViewModel by viewModels()
    private val args: CharlaDetailFragmentArgs by navArgs()

    private val formatoFecha =
        DateTimeFormatter.ofPattern(
            "EEEE, dd 'de' MMMM 'de' yyyy",
            Locale("es", "AR")
        )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharlaDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.cargar(args.charlaId)
        observar()
    }

    private fun observar() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.detalle.collectLatest { detalle ->
                        detalle ?: return@collectLatest
                        pintar(detalle)
                    }
                }

                launch {
                    viewModel.avisos.collectLatest { aviso ->
                        Toast.makeText(requireContext(), aviso, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun pintar(detalle: CharlaDetalleViewModel.Detalle) {
        val charla = detalle.charla
        val trabajo = detalle.trabajo
        val simposio = detalle.simposio

        binding.tvTituloCharla.text = charla.titulo.ifBlank {
            trabajo?.titulo.orEmpty().ifBlank { "Presentación" }
        }

        val tipo = runCatching { TipoActividad.valueOf(charla.tipo) }
            .getOrDefault(TipoActividad.PRESENTACION)

        binding.tvTipo.text = listOfNotNull(
            etiquetaTipo(tipo),
            simposio?.titulo?.takeIf { it.isNotBlank() }
        ).joinToString(" · ")

        val fecha = charla.fecha
            ?.toDate()
            ?.toInstant()
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDate()

        binding.tvFecha.text = fecha
            ?.format(formatoFecha)
            ?.replaceFirstChar { it.uppercase() }
            ?: "Fecha no disponible"

        binding.tvHorario.text = construirHorario(charla)

        pintarLugar(detalle)
        pintarExpositor(detalle)

        mostrarSeccion(
            binding.cardResumen,
            binding.tvResumen,
            trabajo?.resumen
        )

        pintarSimposio(detalle)
    }

    /**
     * El aula sale de la coleccion aulas. Si no se pudo leer, se cae a los
     * datos que el simposio guarda copiados; y recien como ultimo recurso se
     * muestra el id, que es lo que se veia siempre antes de esto.
     */
    private fun pintarLugar(detalle: CharlaDetalleViewModel.Detalle) {
        val aula: AulaFirebase? = detalle.aula
        val simposio = detalle.simposio

        val nombre = aula?.nombre?.takeIf { it.isNotBlank() }
            ?: simposio?.aulaNombre?.takeIf { it.isNotBlank() }

        binding.tvSala.text = nombre
            ?: detalle.charla.aulaId.ifBlank { "Aula no disponible" }

        val edificio = aula?.edificio?.takeIf { it.isNotBlank() }
            ?: simposio?.aulaEdificio?.takeIf { it.isNotBlank() }

        val piso = aula?.piso ?: simposio?.aulaPiso

        val partes = mutableListOf<String>()
        edificio?.let { partes += it }
        piso?.let { partes += if (it == 0) "Planta baja" else "Piso $it" }
        aula?.capacidad?.takeIf { it > 0 }?.let { partes += "Capacidad $it" }
        aula?.referencia?.takeIf { it.isNotBlank() }?.let { partes += it }

        binding.tvSalaDetalle.text = partes.joinToString(" · ")
        binding.tvSalaDetalle.visibility =
            if (partes.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun pintarExpositor(detalle: CharlaDetalleViewModel.Detalle) {
        val trabajo = detalle.trabajo

        val nombre = trabajo?.autorNombre?.takeIf { it.isNotBlank() }
        binding.cardExpositor.visibility =
            if (nombre == null) View.GONE else View.VISIBLE
        binding.tvExpositor.text = nombre.orEmpty()

        val email = trabajo?.autorEmail?.takeIf { it.isNotBlank() }
        binding.tvExpositorEmail.text = email.orEmpty()
        binding.tvExpositorEmail.visibility =
            if (email == null) View.GONE else View.VISIBLE
    }

    private fun pintarSimposio(detalle: CharlaDetalleViewModel.Detalle) {
        val simposio = detalle.simposio

        if (simposio == null) {
            binding.cardSimposio.visibility = View.GONE
            return
        }
        binding.cardSimposio.visibility = View.VISIBLE
        binding.tvSimposioTitulo.text = simposio.titulo

        val tema = simposio.temaCentral.takeIf { it.isNotBlank() }
        binding.tvSimposioTema.text = tema?.let { "Tema central: $it" }.orEmpty()
        binding.tvSimposioTema.visibility =
            if (tema == null) View.GONE else View.VISIBLE

        val descripcion = simposio.descripcion.takeIf { it.isNotBlank() }
        binding.tvSimposioDescripcion.text = descripcion.orEmpty()
        binding.tvSimposioDescripcion.visibility =
            if (descripcion == null) View.GONE else View.VISIBLE
    }

    /** Esconde la tarjeta entera cuando el dato no esta, en vez de dejarla vacia. */
    private fun mostrarSeccion(tarjeta: View, texto: TextView, contenido: String?) {
        val hay = !contenido.isNullOrBlank()
        tarjeta.visibility = if (hay) View.VISIBLE else View.GONE
        texto.text = contenido.orEmpty()
    }

    private fun construirHorario(charla: CharlaFirebase): String {
        if (charla.horaInicio.isBlank() || charla.horaFin.isBlank()) {
            return "Horario no disponible"
        }

        val inicio = runCatching { LocalTime.parse(charla.horaInicio) }.getOrNull()
        val fin = runCatching { LocalTime.parse(charla.horaFin) }.getOrNull()

        if (inicio == null || fin == null) {
            return "${charla.horaInicio} - ${charla.horaFin}"
        }

        val minutos = Duration.between(inicio, fin).toMinutes()

        /* Una presentacion son 30 minutos partidos en exposicion y preguntas:
         * decirlo aca le ahorra la pregunta al expositor y al asistente. */
        return if (minutos == CharlaFirebase.MINUTOS_PRESENTACION.toLong()) {
            "${charla.horaInicio} - ${charla.horaFin} " +
                    "(${CharlaFirebase.MINUTOS_EXPOSICION} de exposición + " +
                    "${CharlaFirebase.MINUTOS_PREGUNTAS} de preguntas)"
        } else {
            "${charla.horaInicio} - ${charla.horaFin} ($minutos minutos)"
        }
    }

    private fun etiquetaTipo(tipo: TipoActividad): String = when (tipo) {
        TipoActividad.PRESENTACION -> "Presentación"
        TipoActividad.CONFERENCIA -> "Conferencia"
        TipoActividad.COFFEE_BREAK -> "Coffee break"
        TipoActividad.ACREDITACION -> "Acreditación"
        TipoActividad.OTRO -> "Actividad"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
