package com.example.deathnote

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ListaMuertosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PersonasMuertasAdapter
    private val listaPersonasMuertas = mutableListOf<DatosPersonasMuertas>()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var progressBar: ProgressBar
    private lateinit var tvMensaje: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_muertos)

        recyclerView = findViewById(R.id.recyclerViewPersonas)
        progressBar = findViewById(R.id.progressBar)
        tvMensaje = findViewById(R.id.tvMensaje)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PersonasMuertasAdapter(listaPersonasMuertas) { persona ->
            // acción si se hace clic (opcional)
        }
        recyclerView.adapter = adapter

        obtenerDatosDesdeFirestore()
    }

    private fun obtenerDatosDesdeFirestore() {
        progressBar.visibility = View.VISIBLE
        tvMensaje.visibility = View.GONE

        db.collection("personas_muertas")
            .addSnapshotListener { snapshots, e ->
                progressBar.visibility = View.GONE

                if (e != null) {
                    Log.w("ListaMuertos", "Error al leer Firestore", e)
                    tvMensaje.text = "Error al cargar datos"
                    tvMensaje.visibility = View.VISIBLE
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    listaPersonasMuertas.clear()
                    for (doc in snapshots) {
                        val persona = doc.toObject(DatosPersonasMuertas::class.java)
                        listaPersonasMuertas.add(persona)
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    tvMensaje.text = "No hay personas muertas registradas"
                    tvMensaje.visibility = View.VISIBLE
                }
            }
    }
}
