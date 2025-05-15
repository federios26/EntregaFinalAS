package com.example.deathnote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PersonasMuertasAdapter(
    private val listaPersonas: List<DatosPersonasMuertas>,
    private val onItemClick: (DatosPersonasMuertas) -> Unit
) : RecyclerView.Adapter<PersonasMuertasAdapter.ViewHolder>() {

    // Crea una nueva vista (invocado por el layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Crea una nueva vista
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_persona_muerta, parent, false)
        return ViewHolder(view)
    }

    // Reemplaza el contenido de una vista (invocado por el layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val persona = listaPersonas[position]
        holder.bind(persona)
        holder.itemView.setOnClickListener {
            onItemClick(persona)
        }
    }

    // Devuelve el tamaño de tu dataset
    override fun getItemCount() = listaPersonas.size

    // Proporciona una referencia a las vistas para cada elemento de datos
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        private val tvCausa: TextView = itemView.findViewById(R.id.tvCausa)
        private val imgFoto: ImageView = itemView.findViewById(R.id.imgFoto)

        fun bind(persona: DatosPersonasMuertas) {
            tvNombre.text = "${persona.nombre} ${persona.apellido}"
            tvCausa.text = persona.causaMuerte

            // Cargar imagen con Glide si hay una URL
            if (persona.imagenUrl != null) {
                Glide.with(itemView.context)
                    .load(persona.imagenUrl)
                    .placeholder(R.drawable.default_person)
                    .error(R.drawable.default_person)
                    .into(imgFoto)
            } else {
                // Imagen por defecto si no hay foto
                imgFoto.setImageResource(R.drawable.default_person)
            }
        }
    }
}