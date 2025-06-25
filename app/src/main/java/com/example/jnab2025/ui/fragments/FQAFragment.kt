package com.example.jnab2025.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.models.FaqItem
import com.example.jnab2025.ui.adapters.FaqAdapter

class FAQFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_faq, container, false)

        val faqList = listOf(
            FaqItem("Asistentes", isHeader = true),
            FaqItem(
                "¿Cómo me inscribo a las Jornadas?",
                "Desde la app podés inscribirte seleccionando las charlas en la sección 'Charlas'."
            ),
            FaqItem(
                "¿Puedo seleccionar las charlas que me interesan?",
                "Sí. Marcando una charla como 'Me interesa' o 'Voy a asistir' se guarda en tu agenda."
            ),
            FaqItem(
                "¿Dónde se llevarán a cabo las charlas?",
                " Este año las charlas se llevaran a cabo en la UNPSJB de Pto. Madryn, el aula y su horario estaran publicados en el simposio."
            ),
            FaqItem(
                "¿La app me notifica cuándo empieza una actividad?",
                "Sí(en un futuro). Recibirás recordatorios antes de las actividades guardadas en tu agenda."
            ),
            FaqItem(
                "¿Cómo armo mi agenda personalizada?",
                "Simplemente tocá el ícono de estrella en las charlas que te interesen y se agregará en tu agenda."
            ),
            FaqItem(
                "¿Qué hacer si no puedo asistir a una charla?",
                "Podés desmarcarla desde tu agenda en cualquier momento."
            ),
            FaqItem(
                "¿Dónde consultar cambios de último momento?",
                "En la sección 'Novedades' encontrarás los anuncios importantes del evento."
            ),

            FaqItem("Expositores", isHeader = true),
            FaqItem(
                "¿Cómo subo mi trabajo?",
                "Desde tu perfil de expositor podés cargar el archivo PDF y enviarlo."
            ),
            FaqItem(
                "¿Dónde envío el comprobante de pago?",
                "Una vez tu trabajo sea aceptado, en la misma sección donde subís tu trabajo vas podés cargar el comprobante."
            ),
            FaqItem(
                "¿Puedo ver el estado de evaluación de mi trabajo?",
                "Sí. El estado aparece junto al título del trabajo en tu perfil."
            ),
            FaqItem(
                "¿Qué pasa si necesito modificar un resumen ya enviado?",
                "Podes usar la opción 'Modificar' si está habilitada sino tendrás que contactar a la organización."
            ),
            FaqItem(
                "¿Cómo sé en qué horario presentaré?",
                "Una vez confirmado el simposio, verás tu charla con tu nombre, el horario y aula en donde expondrás!."
            ),
            FaqItem(
                "¿Dónde consultar si mi presentación fue aceptada?",
                "La app mostrará el estado actualizado en tu perfil de expositor."
            ),
            FaqItem(
                "¿La app me avisa de fechas importantes?",
                "Sí, recibirás notificaciones sobre vencimientos(en un futuro), envíos y agenda."
            )
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerFaq)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = FaqAdapter(faqList)

        return view
    }
}
