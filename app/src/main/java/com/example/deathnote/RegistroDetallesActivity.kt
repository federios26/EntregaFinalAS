package com.example.deathnote

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.deathnote.databinding.ActivityRegistroDetallesBinding
import com.google.firebase.firestore.FirebaseFirestore

class RegistroDetallesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroDetallesBinding
    private lateinit var db: FirebaseFirestore
    private var documentId: String? = null

    companion object {
        private const val TIEMPO_CAUSA_MS = 40000L
        private const val TIEMPO_DETALLES_MS = 400000L
        private const val TIEMPO_POST_DETALLES_MS = 40000L
        private const val CAUSA_DEFAULT = "ATAQUE AL CORAZON"
        private const val DETALLES_DEFAULT = "Sin detalles adicionales"
    }

    private lateinit var timerCausa: CountDownTimer
    private lateinit var timerDetalles: CountDownTimer
    private var timerPostDetalles: CountDownTimer? = null

    private var detallesYaEscritos = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroDetallesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        documentId = intent.getStringExtra("DOCUMENT_ID")
        if (documentId == null) {
            Toast.makeText(this, "Error: No se recibió el ID del documento", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding.txtTemporizadorCausa.text = "40s"
        binding.txtTemporizadorDetalles.text = "6:40"
        binding.txtTemporizadorMuerte.visibility = View.GONE

        iniciarTemporizadorCausa()

        binding.etCausaMuerte.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                detenerTemporizadorCausa()
                if (binding.etCausaMuerte.text.toString().trim().isEmpty()) {
                    binding.etCausaMuerte.setText(CAUSA_DEFAULT)
                }
                iniciarTemporizadorDetalles()
                true
            } else false
        }

        binding.etCausaMuerte.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                detenerTemporizadorCausa()
                if (binding.etCausaMuerte.text.toString().trim().isEmpty()) {
                    binding.etCausaMuerte.setText(CAUSA_DEFAULT)
                }
                iniciarTemporizadorDetalles()
            }
        }

        binding.etDetalles.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !::timerDetalles.isInitialized) {
                iniciarTemporizadorDetalles()
            }
        }

        binding.etDetalles.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!detallesYaEscritos && !s.isNullOrBlank()) {
                    detallesYaEscritos = true
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnRegistrar.setOnClickListener {
            detenerTemporizadorDetalles()

            val detalles = binding.etDetalles.text.toString().trim()
            if (detallesYaEscritos && detalles.isNotEmpty() && detalles != DETALLES_DEFAULT) {
                iniciarTemporizadorPostDetalles()
            } else {
                guardarDetallesYFinalizar()
            }
        }

        binding.btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun iniciarTemporizadorCausa() {
        timerCausa = object : CountDownTimer(TIEMPO_CAUSA_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.txtTemporizadorCausa.text = "${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                if (binding.etCausaMuerte.text.toString().trim().isEmpty()) {
                    binding.etCausaMuerte.setText(CAUSA_DEFAULT)
                }
                iniciarTemporizadorDetalles()
            }
        }.start()
    }

    private fun detenerTemporizadorCausa() {
        if (::timerCausa.isInitialized) timerCausa.cancel()
    }

    private fun iniciarTemporizadorDetalles() {
        timerDetalles = object : CountDownTimer(TIEMPO_DETALLES_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 60000).toInt()
                val seconds = ((millisUntilFinished % 60000) / 1000).toInt()
                binding.txtTemporizadorDetalles.text = String.format("%d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                if (binding.etDetalles.text.toString().trim().isEmpty()) {
                    binding.etDetalles.setText(DETALLES_DEFAULT)
                    guardarDetallesYFinalizar()
                }
            }
        }.start()
    }

    private fun detenerTemporizadorDetalles() {
        if (::timerDetalles.isInitialized) timerDetalles.cancel()
    }

    private fun iniciarTemporizadorPostDetalles() {
        binding.txtTemporizadorMuerte.visibility = View.VISIBLE

        timerPostDetalles = object : CountDownTimer(TIEMPO_POST_DETALLES_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                binding.txtTemporizadorMuerte.text = "Muerte en $seconds segundos..."
            }

            override fun onFinish() {
                guardarDetallesYFinalizar()
            }
        }.start()
    }

    private fun guardarDetallesYFinalizar() {
        val causa = binding.etCausaMuerte.text.toString().trim().ifEmpty { CAUSA_DEFAULT }
        val detalles = binding.etDetalles.text.toString().trim().ifEmpty { DETALLES_DEFAULT }

        val datosActualizados = mapOf(
            "causaMuerte" to causa,
            "detalles" to detalles
        )

        documentId?.let { id ->
            db.collection("personas_muertas")
                .document(id)
                .update(datosActualizados)
                .addOnSuccessListener {
                    Toast.makeText(this, "Detalles guardados exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al guardar detalles: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::timerCausa.isInitialized) timerCausa.cancel()
        if (::timerDetalles.isInitialized) timerDetalles.cancel()
        timerPostDetalles?.cancel()
    }
}
