package com.example.deathnote

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnRegistrar = findViewById<Button>(R.id.btnRegistrarPersona)
        val btnVerLista = findViewById<Button>(R.id.btnVerListaMuertos)

        btnRegistrar.setOnClickListener {
            val intent = Intent(this, RegistroBasico::class.java)
            startActivity(intent)
        }

        btnVerLista.setOnClickListener {
            val intent = Intent(this, ListaMuertosActivity::class.java)
            startActivity(intent)
        }
    }
}
