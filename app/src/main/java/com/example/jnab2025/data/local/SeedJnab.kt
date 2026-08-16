package com.example.jnab2025.data.local

import androidx.room.withTransaction
import com.example.jnab2025.data.model.AgendaUsuario
import com.example.jnab2025.data.model.Aula
import com.example.jnab2025.data.model.CategoriaLugar
import com.example.jnab2025.data.model.Charla
import com.example.jnab2025.data.model.ComprobantePago
import com.example.jnab2025.data.model.EstadoComprobante
import com.example.jnab2025.data.model.EstadoInscripcion
import com.example.jnab2025.data.model.EstadoTrabajo
import com.example.jnab2025.data.model.Evento
import com.example.jnab2025.data.model.Faq
import com.example.jnab2025.data.model.FechaImportante
import com.example.jnab2025.data.model.Inscripcion
import com.example.jnab2025.data.model.Lugar
import com.example.jnab2025.data.model.Novedad
import com.example.jnab2025.data.model.PublicoFaq
import com.example.jnab2025.data.model.Rol
import com.example.jnab2025.data.model.Simposio
import com.example.jnab2025.data.model.TipoActividad
import com.example.jnab2025.data.model.TipoFecha
import com.example.jnab2025.data.model.TipoInscripcion
import com.example.jnab2025.data.model.Trabajo
import com.example.jnab2025.data.model.Usuario
import com.example.jnab2025.data.model.UsuarioRol
import com.example.jnab2025.utils.Passwords
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Datos de arranque. Corre una sola vez, cuando Room crea jnab.db.
 *
 * A diferencia del seed viejo, que borraba y recargaba todo en cada onCreate de
 * MainActivity, esto no se vuelve a ejecutar: lo que cargue el usuario persiste.
 * Para volver a sembrar hay que desinstalar la app o borrar sus datos.
 */
object SeedJnab {

    private val DIA_1: LocalDate = LocalDate.of(2025, 5, 15)
    private val DIA_2: LocalDate = LocalDate.of(2025, 5, 16)
    private val DIA_3: LocalDate = LocalDate.of(2025, 5, 17)

    suspend fun poblar(db: JnabDatabase) = db.withTransaction {
        val usuarios = db.usuarioDao()
        val eventos = db.eventoDao()
        val simposios = db.simposioDao()
        val trabajos = db.trabajoDao()
        val charlas = db.charlaDao()
        val agenda = db.agendaDao()
        val inscripciones = db.inscripcionDao()
        val contenido = db.contenidoDao()

        // ---------- evento ----------
        val eventoId = eventos.insertar(
            Evento(
                nombre = "Jornadas Nacionales de Antropologia Biologica 2025",
                fechaInicio = DIA_1,
                fechaFin = DIA_3,
                sede = "UNPSJB - Sede Puerto Madryn",
                montoInscripcion = 25000.0
            )
        )

        // ---------- aulas ----------
        val aula1 = eventos.insertarAula(aula("Aula 1", 0, 80, "Planta baja, frente a la entrada"))
        val aula2 = eventos.insertarAula(aula("Aula 2", 0, 60, "Planta baja, pasillo izquierdo"))
        val aula5 = eventos.insertarAula(aula("Aula 5", 1, 45, "Primer piso, ala oeste"))
        val aula8 = eventos.insertarAula(aula("Aula 8", 1, 120, "Primer piso, ala este"))
        val aula12 = eventos.insertarAula(aula("Aula 12", 2, 40, "Segundo piso, junto a la biblioteca"))
        val auditorio = eventos.insertarAula(aula("Auditorio", 0, 250, "Planta baja, ala norte"))

        // ---------- usuarios ----------
        // Las contrasenias son las mismas que usaba UserFakeData, ahora hasheadas.
        val admin = crear(usuarios, "Ana", "Bianchi", "admin@jnab.ar", "1234", Rol.ORGANIZADOR)
        val leo = crear(usuarios, "Leonardo", "Morales", "leo@jnab.ar", "hola123", Rol.EXPOSITOR, Rol.ASISTENTE)
        val rafa = crear(usuarios, "Rafael", "Orbe", "usuario1@jnab.ar", "pass1", Rol.EXPOSITOR, Rol.ASISTENTE)
        val alejo = crear(usuarios, "Alejo", "Gonzalez", "alejo@jnab.ar", "hola123", Rol.ASISTENTE)
        val luciana = crear(usuarios, "Luciana", "Perez", "luciana@jnab.ar", "hola123", Rol.ASISTENTE)
        val santiago = crear(usuarios, "Santiago", "Toro", "santiago@jnab.ar", "hola123", Rol.ASISTENTE)

        // ---------- simposios ----------
        // Cada uno con su aula fija. Ninguno comparte aula con fechas superpuestas.
        val sim1 = simposios.insertar(
            Simposio(
                eventoId = eventoId, organizadorId = admin, aulaId = aula1,
                titulo = "Raices de la Humanidad: Avances en Evolucion y Diversidad",
                descripcion = "Novedades en el estudio de la evolucion humana y la diversidad biologica.",
                temaCentral = "Evolucion humana",
                fechaInicio = DIA_1, fechaFin = DIA_1
            )
        )
        val sim2 = simposios.insertar(
            Simposio(
                eventoId = eventoId, organizadorId = admin, aulaId = aula8,
                titulo = "Huella Genetica: Nuevas Perspectivas en Evolucion Humana",
                descripcion = "Genetica de poblaciones aplicada al poblamiento americano.",
                temaCentral = "Genetica de poblaciones",
                fechaInicio = DIA_2, fechaFin = DIA_2
            )
        )
        val sim3 = simposios.insertar(
            Simposio(
                eventoId = eventoId, organizadorId = admin, aulaId = aula2,
                titulo = "Genes, Cultura y Ambiente: Intersecciones Biologicas del Ser Humano",
                descripcion = "Interaccion entre factores geneticos, culturales y ambientales.",
                temaCentral = "Bioantropologia",
                fechaInicio = DIA_1, fechaFin = DIA_2
            )
        )
        val sim4 = simposios.insertar(
            Simposio(
                eventoId = eventoId, organizadorId = admin, aulaId = aula5,
                titulo = "Huesos y Memoria: Lecturas Biologicas del Registro Arqueologico",
                descripcion = "Bioarqueologia y analisis de restos oseos.",
                temaCentral = "Bioarqueologia",
                fechaInicio = DIA_2, fechaFin = DIA_3
            )
        )
        simposios.insertar(
            Simposio(
                eventoId = eventoId, organizadorId = admin, aulaId = aula12,
                titulo = "Tecnologias Emergentes para el Estudio Biologico del Ser Humano",
                descripcion = "Imagenes 3D, secuenciacion masiva y analisis computacional.",
                temaCentral = "Metodologia",
                fechaInicio = DIA_3, fechaFin = DIA_3
            )
        )

        // ---------- trabajos ----------
        // Uno de cada estado, para que ninguna pantalla quede vacia.
        val t1 = trabajos.insertar(
            trabajo(sim1, leo, "Variabilidad craneofacial en poblaciones patagonicas",
                "Analisis morfometrico sobre una muestra de 120 individuos.", EstadoTrabajo.APROBADO,
                resueltoPor = admin)
        )
        val t2 = trabajos.insertar(
            trabajo(sim3, rafa, "Dieta y ambiente en el Holoceno tardio",
                "Isotopos estables aplicados al estudio de la dieta.", EstadoTrabajo.APROBADO,
                resueltoPor = admin)
        )
        val t3 = trabajos.insertar(
            trabajo(sim1, rafa, "Asimetria bilateral y actividad fisica",
                "Comparacion de marcadores de estres ocupacional.", EstadoTrabajo.APROBADO,
                resueltoPor = admin)
        )
        val t4 = trabajos.insertar(
            trabajo(sim2, leo, "Linajes mitocondriales en el noroeste argentino",
                "Secuenciacion de la region control en 45 muestras.", EstadoTrabajo.APROBADO,
                resueltoPor = admin)
        )
        // Pendientes: es lo que ve el organizador en "Ver propuestas" del simposio 2.
        trabajos.insertar(
            trabajo(sim2, rafa, "Marcadores autosomicos y mestizaje",
                "Estimacion de ancestria en poblaciones urbanas.", EstadoTrabajo.ENVIADO)
        )
        trabajos.insertar(
            trabajo(sim2, leo, "Diversidad del cromosoma Y en Patagonia",
                "Haplogrupos y su distribucion geografica.", EstadoTrabajo.ENVIADO)
        )
        // Rechazado, para probar que se muestre el motivo.
        trabajos.insertar(
            trabajo(sim4, rafa, "Notas sobre coleccion osteologica",
                "Descripcion preliminar de una coleccion de museo.", EstadoTrabajo.RECHAZADO,
                resueltoPor = admin,
                motivo = "El resumen no presenta resultados ni metodologia. Se sugiere reenviar " +
                    "con datos concretos para la proxima edicion."
            )
        )

        // ---------- charlas programadas ----------
        // Reproduce el ejemplo del enunciado: 14:00 en un aula y 14:40 en otra.
        val c1 = charlas.insertar(
            Charla.presentacion(eventoId, sim1, t1,
                "Variabilidad craneofacial en poblaciones patagonicas", DIA_1, LocalTime.of(14, 0))
        )
        val c2 = charlas.insertar(
            Charla.presentacion(eventoId, sim3, t2,
                "Dieta y ambiente en el Holoceno tardio", DIA_1, LocalTime.of(14, 40))
        )
        // Esta se solapa con c2: sirve para probar el aviso de choque de horarios.
        charlas.insertar(
            Charla.presentacion(eventoId, sim1, t3,
                "Asimetria bilateral y actividad fisica", DIA_1, LocalTime.of(14, 40))
        )
        charlas.insertar(
            Charla.presentacion(eventoId, sim2, t4,
                "Linajes mitocondriales en el noroeste argentino", DIA_2, LocalTime.of(10, 0))
        )

        // ---------- actividades sin simposio ----------
        charlas.insertar(
            actividad(eventoId, TipoActividad.ACREDITACION, "Acreditacion",
                DIA_1, LocalTime.of(8, 30), LocalTime.of(9, 30), auditorio)
        )
        charlas.insertar(
            actividad(eventoId, TipoActividad.CONFERENCIA, "Conferencia inaugural",
                DIA_1, LocalTime.of(9, 30), LocalTime.of(10, 30), auditorio)
        )
        listOf(DIA_1, DIA_2, DIA_3).forEach { dia ->
            charlas.insertar(
                actividad(eventoId, TipoActividad.COFFEE_BREAK, "Coffee break",
                    dia, LocalTime.of(16, 0), LocalTime.of(16, 30), null)
            )
        }
        charlas.insertar(
            actividad(eventoId, TipoActividad.CONFERENCIA, "Conferencia de cierre",
                DIA_3, LocalTime.of(17, 0), LocalTime.of(18, 0), auditorio)
        )

        // ---------- inscripciones ----------
        val inscLeo = inscripciones.insertar(
            inscripcion(leo, eventoId, TipoInscripcion.EXPOSITOR, EstadoInscripcion.PAGADA)
        )
        inscripciones.insertarComprobante(
            ComprobantePago(
                inscripcionId = inscLeo,
                archivoUri = "content://demo/comprobante-leo.pdf",
                nombreArchivo = "transferencia_leo.pdf",
                fechaCarga = Instant.parse("2025-04-28T14:10:00Z"),
                estado = EstadoComprobante.VERIFICADO,
                verificadoPorId = admin
            )
        )
        // Rafael todavia no pago: en "Mis trabajos" le tiene que aparecer el boton.
        inscripciones.insertar(
            inscripcion(rafa, eventoId, TipoInscripcion.EXPOSITOR, EstadoInscripcion.PENDIENTE_PAGO)
        )
        inscripciones.insertar(
            inscripcion(alejo, eventoId, TipoInscripcion.ESTUDIANTE, EstadoInscripcion.PAGADA)
        )
        inscripciones.insertar(
            inscripcion(luciana, eventoId, TipoInscripcion.ASISTENTE, EstadoInscripcion.PENDIENTE_PAGO)
        )
        // Santiago queda sin inscripcion a proposito, para poder probar el
        // circuito completo desde cero: inscribirse y despues pagar.

        // ---------- agenda de ejemplo ----------
        agenda.agregar(AgendaUsuario(alejo, c1, 15, Instant.parse("2025-05-01T10:00:00Z")))
        agenda.agregar(AgendaUsuario(alejo, c2, 30, Instant.parse("2025-05-01T10:01:00Z")))

        // ---------- fechas importantes ----------
        eventos.insertarFecha(
            FechaImportante(eventoId = eventoId, titulo = "Cierre de envio de resumenes",
                descripcion = "Ultimo dia para subir el trabajo al simposio elegido.",
                fechaLimite = Instant.parse("2025-04-30T23:59:00Z"), tipo = TipoFecha.CIERRE_ENVIO)
        )
        eventos.insertarFecha(
            FechaImportante(eventoId = eventoId, titulo = "Cierre de pago de inscripcion",
                descripcion = "Ultimo dia para cargar el comprobante de pago.",
                fechaLimite = Instant.parse("2025-05-09T23:59:00Z"), tipo = TipoFecha.CIERRE_PAGO)
        )
        eventos.insertarFecha(
            FechaImportante(eventoId = eventoId, titulo = "Comienzan las jornadas",
                descripcion = "Acreditacion desde las 8:30 en el Auditorio.",
                fechaLimite = Instant.parse("2025-05-15T08:30:00Z"), tipo = TipoFecha.INICIO_EVENTO)
        )

        // ---------- contenido ----------
        contenido.insertarNovedades(novedades(eventoId))
        contenido.insertarLugares(lugares(eventoId))
        contenido.insertarFaqs(faqs(eventoId))
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private fun aula(nombre: String, piso: Int, capacidad: Int, referencia: String) = Aula(
        nombre = nombre,
        edificio = "Edificio Central",
        piso = piso,
        capacidad = capacidad,
        referencia = referencia
    )

    private suspend fun crear(
        dao: com.example.jnab2025.data.local.dao.UsuarioDao,
        nombre: String,
        apellido: String,
        email: String,
        password: String,
        vararg roles: Rol
    ): Long {
        val id = dao.insertar(
            Usuario(
                nombre = nombre,
                apellido = apellido,
                email = email,
                passwordHash = Passwords.hash(password),
                institucion = "UNPSJB"
            )
        )
        roles.forEach { dao.asignarRol(UsuarioRol(id, it)) }
        return id
    }

    private fun trabajo(
        simposioId: Long,
        autorId: Long,
        titulo: String,
        resumen: String,
        estado: EstadoTrabajo,
        resueltoPor: Long? = null,
        motivo: String? = null
    ) = Trabajo(
        simposioId = simposioId,
        autorId = autorId,
        titulo = titulo,
        resumen = resumen,
        archivoUri = "content://demo/${titulo.take(12).replace(' ', '_')}.pdf",
        nombreArchivo = "${titulo.take(20).replace(' ', '_')}.pdf",
        fechaEnvio = Instant.parse("2025-04-20T12:00:00Z"),
        estado = estado,
        motivoRechazo = motivo,
        fechaResolucion = if (resueltoPor != null) Instant.parse("2025-05-02T09:00:00Z") else null,
        resueltoPorId = resueltoPor
    )

    private fun actividad(
        eventoId: Long,
        tipo: TipoActividad,
        titulo: String,
        fecha: LocalDate,
        desde: LocalTime,
        hasta: LocalTime,
        aulaId: Long?
    ) = Charla(
        eventoId = eventoId,
        simposioId = null,
        trabajoId = null,
        aulaId = aulaId,
        tipo = tipo,
        titulo = titulo,
        fecha = fecha,
        horaInicio = desde,
        horaFin = hasta
    )

    private fun inscripcion(
        usuarioId: Long,
        eventoId: Long,
        tipo: TipoInscripcion,
        estado: EstadoInscripcion
    ) = Inscripcion(
        usuarioId = usuarioId,
        eventoId = eventoId,
        tipo = tipo,
        estado = estado,
        monto = if (tipo == TipoInscripcion.ESTUDIANTE) 12500.0 else 25000.0,
        fechaAlta = Instant.parse("2025-04-22T16:00:00Z")
    )

    private fun novedades(eventoId: Long) = listOf(
        Novedad(eventoId = eventoId, titulo = "Descuentos para la JNAB 2025",
            descripcion = "Descuentos exclusivos en alojamientos, restaurantes y agencias.",
            fechaPublicacion = Instant.parse("2025-06-04T12:00:00Z")),
        Novedad(eventoId = eventoId, titulo = "Cambio de aula",
            descripcion = "El Simposio 3 se pasa al Aula 2.",
            fechaPublicacion = Instant.parse("2025-04-27T12:00:00Z")),
        Novedad(eventoId = eventoId, titulo = "Inicio de inscripciones",
            descripcion = "Desde hoy se abren las inscripciones a expositores.",
            fechaPublicacion = Instant.parse("2025-04-20T12:00:00Z")),
        Novedad(eventoId = eventoId, titulo = "Coffee break",
            descripcion = "Habra coffee break a las 16:00 los tres dias.",
            fechaPublicacion = Instant.parse("2025-04-28T12:00:00Z"))
    )

    private fun lugares(eventoId: Long) = listOf(
        Lugar(eventoId = eventoId, nombre = "Hotel Piren", categoria = CategoriaLugar.HOSPEDAJE,
            latitud = -42.768050690693606, longitud = -65.03227527534357, descuento = "15%"),
        Lugar(eventoId = eventoId, nombre = "Hotel Peninsula", categoria = CategoriaLugar.HOSPEDAJE,
            latitud = -42.76511063533343, longitud = -65.03402387505048, descuento = "10%"),
        Lugar(eventoId = eventoId, nombre = "Hotel Yene Hue", categoria = CategoriaLugar.HOSPEDAJE,
            latitud = -42.76415524356521, longitud = -65.03475588138787, descuento = "10%"),
        Lugar(eventoId = eventoId, nombre = "Hostel La Tosca", categoria = CategoriaLugar.HOSPEDAJE,
            latitud = -42.77049563496801, longitud = -65.0376193796195, descuento = "20%"),
        Lugar(eventoId = eventoId, nombre = "Complejo El Puente", categoria = CategoriaLugar.HOSPEDAJE,
            latitud = -42.772002881077995, longitud = -65.03039796874734, descuento = null),
        Lugar(eventoId = eventoId, nombre = "Bistro de Mar", categoria = CategoriaLugar.RESTAURANTE,
            latitud = -42.7723657833891, longitud = -65.02766469394666, descuento = "10%"),
        Lugar(eventoId = eventoId, nombre = "Parrilla Big David", categoria = CategoriaLugar.RESTAURANTE,
            latitud = -42.77256277056446, longitud = -65.02798911664559, descuento = "15%"),
        Lugar(eventoId = eventoId, nombre = "Fervor", categoria = CategoriaLugar.RESTAURANTE,
            latitud = -42.7618593905736, longitud = -65.03628094820698, descuento = null),
        Lugar(eventoId = eventoId, nombre = "All Peninsula", categoria = CategoriaLugar.AGENCIA,
            latitud = -42.76565025445565, longitud = -65.03385461227815, descuento = "10%"),
        Lugar(eventoId = eventoId, nombre = "Argentina Vision", categoria = CategoriaLugar.AGENCIA,
            latitud = -42.769021992821116, longitud = -65.03126665486892, descuento = "10%"),
        Lugar(eventoId = eventoId, nombre = "Cuyun Co", categoria = CategoriaLugar.AGENCIA,
            latitud = -42.76498740894729, longitud = -65.03418470245165, descuento = "12%"),
        Lugar(eventoId = eventoId, nombre = "Chucao", categoria = CategoriaLugar.AGENCIA,
            latitud = -42.770644950097015, longitud = -65.03190087589877, descuento = null)
    )

    private fun faqs(eventoId: Long): List<Faq> {
        val asistente = listOf(
            "Como me inscribo a las Jornadas?" to
                "Desde la app, en la seccion Inscripcion. Despues cargas el comprobante de pago.",
            "Puedo seleccionar las charlas que me interesan?" to
                "Si. Tocando la estrella en una charla se guarda en tu agenda personal.",
            "Donde se llevan a cabo las charlas?" to
                "En la UNPSJB de Puerto Madryn. Cada simposio tiene un aula fija, que figura en el cronograma.",
            "La app me avisa cuando empieza una actividad?" to
                "Si, con los minutos de anticipacion que elijas para cada charla de tu agenda.",
            "Que pasa si dos charlas que me interesan se pisan?" to
                "La app te avisa al agregarlas y te muestra el horario de cada una.",
            "Donde consulto cambios de ultimo momento?" to
                "En la seccion Novedades."
        )
        val expositor = listOf(
            "Como subo mi trabajo?" to
                "Elegis el simposio, cargas titulo y resumen, y adjuntas el PDF.",
            "Donde envio el comprobante de pago?" to
                "En la seccion de inscripcion. El pago es uno solo por persona, no por trabajo.",
            "Puedo ver el estado de evaluacion de mi trabajo?" to
                "Si, en Mis Trabajos. Si fue rechazado tambien vas a ver el motivo.",
            "Puedo modificar un resumen ya enviado?" to
                "Si, mientras el trabajo siga en estado enviado.",
            "Como se en que horario presento?" to
                "Una vez aprobado, en Mis Trabajos aparece el dia, la hora y el aula."
        )
        var orden = 0
        return asistente.map { (p, r) ->
            Faq(eventoId = eventoId, publico = PublicoFaq.ASISTENTE, pregunta = p, respuesta = r, orden = orden++)
        } + expositor.map { (p, r) ->
            Faq(eventoId = eventoId, publico = PublicoFaq.EXPOSITOR, pregunta = p, respuesta = r, orden = orden++)
        }
    }
}
