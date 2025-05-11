package com.example.deathnote

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.deathnote.R

class RegistroBasico : AppCompatActivity() {

    private lateinit var etNombreCompleto: EditText
    private lateinit var etApellidoCompleto: EditText
    private lateinit var btnTomarFoto: Button
    private lateinit var btnSeleccionarGaleria: Button
    private lateinit var imgFoto: ImageView
    private lateinit var btnContinuar: Button

    private var imagenUri: Uri? = null
    private var imagenTomada = false

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val CAMERA_REQUEST_CODE = 101
        private const val GALLERY_REQUEST_CODE = 102
        private const val READ_EXTERNAL_STORAGE_PERMISSION_CODE = 103
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_basico)

        // Inicializar vistas
        etNombreCompleto = findViewById(R.id.etNombreCompleto)
        etApellidoCompleto = findViewById(R.id.etApellidoCompleto)
        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        btnSeleccionarGaleria = findViewById(R.id.btnSeleccionarGaleria)
        imgFoto = findViewById(R.id.imgFoto)
        btnContinuar = findViewById(R.id.btnContinuar)

        // Configurar listeners
        btnTomarFoto.setOnClickListener {
            if (checkCameraPermission()) {
                abrirCamara()
            } else {
                solicitarPermisosCamara()
            }
        }

        btnSeleccionarGaleria.setOnClickListener {
            if (checkGalleryPermission()) {
                abrirGaleria()
            } else {
                solicitarPermisosGaleria()
            }
        }

        btnContinuar.setOnClickListener {
            continuarRegistro()
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun solicitarPermisosCamara() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    private fun checkGalleryPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun solicitarPermisosGaleria() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            READ_EXTERNAL_STORAGE_PERMISSION_CODE
        )
    }

    private fun abrirCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    abrirCamara()
                } else {
                    Toast.makeText(
                        this,
                        "Se requiere permiso de cámara para continuar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            READ_EXTERNAL_STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    abrirGaleria()
                } else {
                    Toast.makeText(
                        this,
                        "Se requiere permiso de acceso a galería para continuar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    imgFoto.setImageBitmap(imageBitmap)
                    imagenTomada = true
                }
                GALLERY_REQUEST_CODE -> {
                    val selectedImage = data?.data
                    imagenUri = selectedImage
                    imgFoto.setImageURI(selectedImage)
                    imagenTomada = true
                }
            }
        }
    }

    private fun continuarRegistro() {
        // Validar campos obligatorios
        if (etNombreCompleto.text.toString().trim().isEmpty()) {
            etNombreCompleto.error = "Este campo es obligatorio"
            return
        }

        if (etApellidoCompleto.text.toString().trim().isEmpty()) {
            etApellidoCompleto.error = "Este campo es obligatorio"
            return
        }

        if (!imagenTomada) {
            Toast.makeText(this, "Por favor adjunte una imagen", Toast.LENGTH_SHORT).show()
            return
        }

        // Continuar al siguiente formulario
        val intent = Intent(this, RegistroDetallesActivity::class.java)
        intent.putExtra("NOMBRE_COMPLETO", etNombreCompleto.text.toString().trim())
        intent.putExtra("APELLIDO_COMPLETO", etApellidoCompleto.text.toString().trim())
        // La imagen se manejará a través de una base de datos o almacenamiento local
        // en una aplicación real, aquí solo pasamos los datos de texto
        startActivity(intent)

    }
}