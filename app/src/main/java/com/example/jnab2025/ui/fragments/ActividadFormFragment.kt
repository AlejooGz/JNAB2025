package com.example.jnab2025.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.data.model.ActividadFirebase
import com.example.jnab2025.data.model.TipoActividad
import com.example.jnab2025.databinding.FragmentActividadFormBinding
import com.example.jnab2025.ui.adapters.SlotHorarioAdapter
import com.example.jnab2025.ui.viewmodels.ActividadViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import com.example.jnab2025.R

abstract class ActividadFormFragment : Fragment() {
    private var _binding: FragmentActividadFormBinding? = null
    protected val binding get() = _binding!!
    protected val viewModel: ActividadViewModel by viewModels()
    protected abstract val actividadId: String?
    protected abstract val encabezado: String
    private var aulaSeleccionadaId: String? = null
    private var aulaSeleccionadaNombre: String? = null
    private var aulaSeleccionadaEdificio: String? = null
    private var aulaSeleccionadaPiso: Int? = null
    private var fechaSeleccionada: LocalDate? = null
    private var horaSeleccionada: LocalTime? = null
    private var actividadCargada: ActividadFirebase? = null
    private lateinit var slotAdapter: SlotHorarioAdapter<ActividadViewModel.Slot>
    private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentActividadFormBinding.inflate(
                inflater,
                container,
                false
            )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvEncabezado.text = encabezado
        configurarTipos()
        configurarDuraciones()
        configurarSlots()
        configurarListeners()
        actividadId?.let {
            viewModel.cargarActividad(it)
        }
        observarDatos()
    }

    private fun configurarTipos() {
        val tipos = listOf(
            TipoActividad.CONFERENCIA.name to "Conferencia",
            TipoActividad.COFFEE_BREAK.name to "Coffee break",
            TipoActividad.ACREDITACION.name to "Acreditación",
            TipoActividad.OTRO.name to "Otra actividad"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner_actividad,
            tipos.map { it.second }
        )
        adapter.setDropDownViewResource(
            R.layout.item_spinner_actividad_dropdown
        )
        binding.spTipo.adapter = adapter
    }

    private fun configurarDuraciones() {
        val duraciones = listOf(
            30,
            60,
            90,
            120
        )

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner_actividad,
            duraciones.map { "$it minutos" }
        )

        adapter.setDropDownViewResource(
            R.layout.item_spinner_actividad_dropdown
        )
        binding.spDuracion.adapter = adapter
    }

    private fun configurarSlots() {
        slotAdapter =
            SlotHorarioAdapter<ActividadViewModel.Slot>(
                obtenerInicio = { it.inicio },
                obtenerFin = { it.fin },
                estaLibre = { it.libre },
                obtenerOcupadoPor = { it.ocupadoPor }
            ) { slot ->
                horaSeleccionada = slot.inicio
                slotAdapter.seleccionar(slot.inicio)
                pintarHorarioFinal()
            }
        binding.rvSlots.apply {
            layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(
                    requireContext()
                )
            adapter = slotAdapter
        }
        actualizarSlots()
    }

    private fun configurarListeners() {
        binding.btnFecha.setOnClickListener { elegirFecha()
        }
        binding.btnAula.setOnClickListener { elegirAula()
        }
        binding.spDuracion.setOnItemSelectedListener(
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    horaSeleccionada = null
                    slotAdapter.seleccionar(null)
                    actualizarSlots()
                    pintarHorarioFinal()
                }
                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                    horaSeleccionada = null
                    slotAdapter.seleccionar(null)
                    actualizarSlots()
                    pintarHorarioFinal()
                }
            }
        )

        binding.btnGuardar.setOnClickListener { guardar()
        }
        binding.btnCancelar.setOnClickListener {
            findNavController().popBackStack()
        }
    }
    private fun elegirFecha() {
        val actual = fechaSeleccionada ?: LocalDate.now()

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                fechaSeleccionada =
                    LocalDate.of(
                        year,
                        month + 1,
                        day
                    )
                pintarFecha()
                actualizarSlots()
            },
            actual.year,
            actual.monthValue - 1,
            actual.dayOfMonth
        ).show()
    }
    private fun elegirAula() {
        val aulas = viewModel.aulas.value

        if (aulas.isEmpty()) {

            Toast.makeText(
                requireContext(),
                "Todavía no se cargaron las aulas",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val opciones = mutableListOf(
                "Sin aula (actividad general)"
            )

        opciones +=
            aulas.map { aula ->
                buildString {
                    append(aula.nombre)
                    if (aula.edificio.isNotBlank()) {
                        append(" · ${aula.edificio}")
                    }
                    append(" · Piso ${aula.piso}")
                }
            }
        android.app.AlertDialog.Builder(
            requireContext()
        )
            .setTitle("Elegir aula")
            .setItems(
                opciones.toTypedArray()
            ) { _, posicion ->

                if (posicion == 0) {

                    aulaSeleccionadaId = null
                    aulaSeleccionadaNombre = null
                    aulaSeleccionadaEdificio = null
                    aulaSeleccionadaPiso = null

                } else {

                    val aula = aulas[posicion - 1]

                    aulaSeleccionadaId = aula.id
                    aulaSeleccionadaNombre = aula.nombre
                    aulaSeleccionadaEdificio = aula.edificio
                    aulaSeleccionadaPiso = aula.piso
                }
                pintarAula()
                actualizarSlots()
            }
            .show()
    }

    private fun actualizarSlots() {
        val slots =
            viewModel.calcularSlots(
                fecha = fechaSeleccionada,
                aulaId = aulaSeleccionadaId,
                duracionMinutos =
                    duracionSeleccionada().toLong(),
                propiaActividadId =
                    actividadCargada?.id
            )
        binding.tvSinSlots.visibility =
            if (slots.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        binding.tvSinSlots.text =
            "No hay horarios disponibles para esa duración"

        slotAdapter.submitList(slots)

        val seleccionado =
            horaSeleccionada
        if (
            seleccionado != null &&
            slots.none {
                it.inicio == seleccionado &&
                        it.libre
            }
        ) {

            horaSeleccionada = null
            slotAdapter.seleccionar(null)
            pintarHorarioFinal()
        }
    }

    private fun duracionSeleccionada(): Int {
        val texto =
            binding.spDuracion
                .selectedItem
                ?.toString()
                ?: "30 minutos"

        return texto
            .substringBefore(" ")
            .toIntOrNull()
            ?: 30
    }

    private fun pintarFecha() {
        binding.tvFecha.text =
            fechaSeleccionada
                ?.format(formatoFecha)
                ?: "Elegir fecha"
    }

    private fun pintarAula() {
        binding.tvAula.text =
            aulaSeleccionadaNombre
                ?: "Sin aula (actividad general)"
    }

    private fun pintarHorarioFinal() {
        val inicio =
            horaSeleccionada

        if (inicio == null) {
            binding.tvHorarioSeleccionado.text =
                "Elegí un horario de inicio"
            return
        }

        val fin =
            inicio.plusMinutes(
                duracionSeleccionada().toLong()
            )
        binding.tvHorarioSeleccionado.text =
            "$inicio - $fin"
    }

    private fun observarDatos() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                launch {

                    viewModel.aulas.collectLatest { aulas ->

                        val aulaId =
                            aulaSeleccionadaId

                        if (
                            aulaId != null &&
                            aulaSeleccionadaNombre == null
                        ) {
                            val aula =
                                aulas.firstOrNull {
                                    it.id == aulaId
                                }

                            aula?.let { seleccionada ->
                                aulaSeleccionadaNombre = seleccionada.nombre
                                aulaSeleccionadaEdificio = seleccionada.edificio
                                aulaSeleccionadaPiso = seleccionada.piso
                            }
                        }
                        pintarAula()
                    }
                }

                launch {

                    viewModel.actividad.collectLatest { actividad ->
                        actividad ?: return@collectLatest
                        actividadCargada = actividad
                        binding.etTitulo.setText(actividad.titulo
                        )
                        binding.etDescripcion.setText(actividad.descripcion
                        )
                        fechaSeleccionada =
                            actividad.fecha
                                ?.toDate()
                                ?.toInstant()
                                ?.atZone(
                                    ZoneId.systemDefault()
                                )
                                ?.toLocalDate()
                        aulaSeleccionadaId = actividad.aulaId
                                .takeIf { it.isNotBlank() }
                        aulaSeleccionadaNombre = actividad.aulaNombre
                                .takeIf { it.isNotBlank() }
                        aulaSeleccionadaEdificio = actividad.aulaEdificio
                                .takeIf { it.isNotBlank() }
                        aulaSeleccionadaPiso = actividad.aulaPiso
                                .takeIf { it != 0 }

                        pintarFecha()
                        pintarAula()
                        seleccionarTipo(
                            actividad.tipo
                        )

                        val duracion =
                            calcularDuracion(
                                actividad.horaInicio,
                                actividad.horaFin
                            )

                        seleccionarDuracion(
                            duracion
                        )

                        horaSeleccionada =
                            runCatching {
                                LocalTime.parse(
                                    actividad.horaInicio
                                )
                            }.getOrNull()

                        actualizarSlots()

                        horaSeleccionada?.let {
                            slotAdapter.seleccionar(it)
                        }

                        pintarHorarioFinal()
                    }
                }

                launch {

                    viewModel.avisos.collectLatest { mensaje ->

                        Toast.makeText(
                            requireContext(),
                            mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                launch {

                    viewModel.guardados.collectLatest {

                        findNavController()
                            .popBackStack()
                    }
                }
            }
        }
    }

    private fun seleccionarTipo(
        tipo: String
    ) {

        val posicion =
            when (tipo) {

                TipoActividad.CONFERENCIA.name ->
                    0

                TipoActividad.COFFEE_BREAK.name ->
                    1

                TipoActividad.ACREDITACION.name ->
                    2

                else ->
                    3
            }

        binding.spTipo.setSelection(
            posicion
        )
    }

    private fun seleccionarDuracion(
        duracion: Int
    ) {

        val posicion =
            when (duracion) {

                60 -> 1
                90 -> 2
                120 -> 3

                else -> 0
            }

        binding.spDuracion.setSelection(
            posicion
        )
    }

    private fun calcularDuracion(
        inicio: String,
        fin: String
    ): Int {

        return runCatching {

            val horaInicio =
                LocalTime.parse(inicio)

            val horaFin =
                LocalTime.parse(fin)

            java.time.Duration
                .between(
                    horaInicio,
                    horaFin
                )
                .toMinutes()
                .toInt()

        }.getOrDefault(30)
    }

    private fun tipoSeleccionado(): String {

        return when (
            binding.spTipo.selectedItemPosition
        ) {

            0 ->
                TipoActividad.CONFERENCIA.name

            1 ->
                TipoActividad.COFFEE_BREAK.name

            2 ->
                TipoActividad.ACREDITACION.name

            else ->
                TipoActividad.OTRO.name
        }
    }

    private fun guardar() {

        val titulo =
            binding.etTitulo
                .text
                .toString()
                .trim()

        val descripcion =
            binding.etDescripcion
                .text
                .toString()
                .trim()

        val fecha =
            fechaSeleccionada

        val horaInicio =
            horaSeleccionada

        if (titulo.isBlank()) {

            aviso(
                "Ingresá un título"
            )

            return
        }

        if (fecha == null) {

            aviso(
                "Elegí una fecha"
            )

            return
        }

        if (horaInicio == null) {

            aviso(
                "Elegí un horario de inicio"
            )

            return
        }

        val duracion =
            duracionSeleccionada()

        val horaFin =
            horaInicio.plusMinutes(
                duracion.toLong()
            )

        if (
            horaFin >
            LocalTime.of(20, 0)
        ) {

            aviso(
                "La actividad no puede terminar después de las 20:00"
            )

            return
        }

        val actividad =
            ActividadFirebase(

                id =
                    actividadCargada?.id
                        ?: "",

                eventoId =
                    actividadCargada?.eventoId
                        ?: "",

                titulo =
                    titulo,

                descripcion =
                    descripcion,

                tipo =
                    tipoSeleccionado(),

                fecha =
                    fecha.toTimestamp(),

                aulaId =
                    aulaSeleccionadaId
                        .orEmpty(),

                aulaNombre =
                    aulaSeleccionadaNombre
                        .orEmpty(),

                aulaEdificio =
                    aulaSeleccionadaEdificio
                        .orEmpty(),

                aulaPiso =
                    aulaSeleccionadaPiso
                        ?: 0,

                horaInicio =
                    horaInicio.toString(),

                horaFin =
                    horaFin.toString()
            )

        if (actividadCargada == null) {

            viewModel.crearActividad(
                actividad
            )

        } else {

            viewModel.actualizarActividad(
                actividad
            )
        }
    }

    private fun aviso(
        mensaje: String
    ) {

        Toast.makeText(
            requireContext(),
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun LocalDate.toTimestamp():
            com.google.firebase.Timestamp {

        val instant =
            atStartOfDay(
                ZoneId.systemDefault()
            ).toInstant()

        return com.google.firebase.Timestamp(
            Date.from(instant)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvSlots.adapter = null
        _binding = null
    }
}