package com.example.jnab2025.ui.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.models.FaqItem

class FaqAdapter(private val faqList: List<FaqItem>) : RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_faq, parent, false)
        return FaqViewHolder(view)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        holder.bind(faqList[position])
    }

    override fun getItemCount() = faqList.size

    inner class FaqViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionText: TextView = itemView.findViewById(R.id.faqQuestion)
        private val answerText: TextView = itemView.findViewById(R.id.faqAnswer)
        private val icon: ImageView = itemView.findViewById(R.id.expandIcon)

        fun bind(item: FaqItem) {
            questionText.text = item.question

            if (item.isHeader) {
                // Estilo para los encabezados
                questionText.textSize = 18f
                questionText.setTypeface(null, Typeface.BOLD)
                answerText.visibility = View.GONE
                icon.visibility = View.GONE
            } else {
                // Estilo normal para preguntas
                questionText.textSize = 16f
                questionText.setTypeface(null, Typeface.NORMAL)

                answerText.text = item.answer.ifEmpty { "Respuesta pendiente." }
                answerText.visibility = if (item.isExpanded) View.VISIBLE else View.GONE

                icon.setImageResource(
                    if (item.isExpanded) R.drawable.baseline_expand_less_24
                    else R.drawable.baseline_expand_more_24
                )
                icon.visibility = View.VISIBLE

                itemView.setOnClickListener {
                    item.isExpanded = !item.isExpanded
                    notifyItemChanged(adapterPosition)
                }
            }
        }
    }
}
