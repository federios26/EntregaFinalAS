package com.example.deathnote

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class RegistroDetallesActivity : AppCompatActivity() {

    private lateinit var txtInfoPersona: TextView
    private lateinit var etCausaMuerte: EditText
    private lateinit var etDetalles: EditText
    private lateinit var txtTemporizadorCausa: TextView
    private lateinit var txtTemporizadorDetalles: TextView
    private lateinit var btnRegistrar: Button
    private lateinit var btnVolver: Button

    private var causaTimer: CountDownTimer? = null
    private var detallesTimer: CountDownTimer? = null

    private var nombreCompleto: String = ""
    private var apellidoCompleto: String = ""

    companion object {
        private const val CAUSA_TIMER_DURATION = 40000L // 40 segundos
        private const val DETALLES_TIMER_DURATION = 400000L // 6 minutos y 40 segundos (400 segundos)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_detalles)

        // Obtener datos del intent
        nombreCompleto = intent.getStringExtra("NOMBRE_COMPLETO") ?: ""
        apellidoCompleto = intent.getStringExtra("APELLIDO_COMPLETO") ?: ""

        // Inicializar vistas
        txtInfoPersona = findViewById(R.id.txtInfoPersona)
        etCausaMuerte = findViewById(R.id.etCausaMuerte)
        etDetalles = findViewById(R.id.etDetalles)
        txtTemporizadorCausa = findViewById(R.id.txtTemporizadorCausa)
        txtTemporizadorDetalles = findViewById(R.id.txtTemporizadorDetalles)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        btnVolver = findViewById(R.id.btnVolver)

        // Configurar información de la persona
        txtInfoPersona.text = "Completando registro para: $nombreCompleto $apellidoCompleto"

        // Configurar estado inicial
        etDetalles.isEnabled = false
        txtTemporizadorDetalles.visibility = View.INVISIBLE

        // Configurar listeners
        etCausaMuerte.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty() && causaTimer == null) {
                    iniciarTemporizadorCausa()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty() && causaTimer != null) {
                    causaTimer?.cancel()
                    causaTimer = null
                    txtTemporizadorCausa.text = "40s"
                    etDetalles.isEnabled = false
                    etDetalles.text.clear()
                    txtTemporizadorDetalles.visibility = View.INVISIBLE
                    detallesTimer?.cancel()
                    detallesTimer = null
                }
            }
        })

        btnRegistrar.setOnClickListener {
            finalizarRegistro()
        }

        btnVolver.setOnClickListener {
            onBackPressed()
        }
    }

    private fun iniciarTemporizadorCausa() {
        txtTemporizadorCausa.visibility = View.VISIBLE
        causaTimer = object : CountDownTimer(CAUSA_TIMER_DURATION, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val segundos = millisUntilFinished / 1000
                txtTemporizadorCausa.text = "${segundos}s"
            }

            override fun onFinish() {
                txtTemporizadorCausa.text = "¡Tiempo!"
                etDetalles.isEnabled = true

                // Iniciar temporizador para detalles si hay causa de muerte
                if (!etCausaMuerte.text.isNullOrEmpty()) {
                    iniciarTemporizadorDetalles()
                }
            }
        }.start()
    }

    private fun iniciarTemporizadorDetalles() {
        txtTemporizadorDetalles.visibility = View.VISIBLE
        detallesTimer = object : CountDownTimer(DETALLES_TIMER_DURATION, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutos = millisUntilFinished / 60000
                val segundos = (millisUntilFinished % 60000) / 1000
                txtTemporizadorDetalles.text = "${minutos}:${String.format("%02d", segundos)}"
            }

            override fun onFinish() {
                txtTemporizadorDetalles.text = "¡Tiempo!"
                etDetalles.isEnabled = false
            }
        }.start()
    }

    private fun finalizarRegistro() {
        // Aquí implementarías el guardado de los datos en tu base de datos
        // Por ejemplo, usando SQLite, Room, Firebase, etc.

        // Para este ejemplo, solo mostramos un mensaje de éxito
        Toast.makeText(
            this,
            "Registro completado exitosamente para $nombreCompleto $apellidoCompleto",
            Toast.LENGTH_LONG
        ).show()

        // Volver a la pantalla principal (cerrando todas las pantallas anteriores)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancelar temporizadores para evitar fugas de memoria
        causaTimer?.cancel()
        detallesTimer?.cancel()
    }
}