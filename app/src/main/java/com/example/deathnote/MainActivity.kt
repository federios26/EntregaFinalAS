package com.example.deathnote

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.deathnote.ListaMuertosActivity
import com.example.deathnote.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnRegistrar = findViewById<Button>(R.id.btn_registrar)
        val btnVerLista = findViewById<Button>(R.id.btn_lista_muertos)

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
