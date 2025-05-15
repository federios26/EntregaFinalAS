package com.example.deathnote

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.deathnote.databinding.ActivityRegistroBasicoBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class RegistroBasico : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBasicoBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var imageUri: Uri? = null

    // Registros para manejar resultados
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) openCamera()
        else showToast("Permiso de cámara requerido")
    }

    private val galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) openGallery()
        else showToast("Permiso de almacenamiento requerido")
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            bitmap?.let {
                binding.imgFoto.setImageBitmap(it)
                imageUri = saveImageToInternalStorage(it)
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.data
            imageUri?.let { binding.imgFoto.setImageURI(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBasicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnTomarFoto.setOnClickListener { checkOrRequestCameraPermission() }
        binding.btnSeleccionarGaleria.setOnClickListener { checkOrRequestGalleryPermission() }
        binding.btnContinuar.setOnClickListener { validateAndContinue() }
    }

    private fun checkOrRequestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> openCamera()
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun checkOrRequestGalleryPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> openGallery()
            else -> galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun validateAndContinue() {
        val nombre = binding.etNombreCompleto.text.toString().trim()
        val apellido = binding.etApellidoCompleto.text.toString().trim()

        when {
            nombre.isEmpty() -> binding.etNombreCompleto.error = "Nombre requerido"
            apellido.isEmpty() -> binding.etApellidoCompleto.error = "Apellido requerido"
            imageUri == null -> showToast("Se requiere una imagen")
            else -> uploadImageAndSaveData(nombre, apellido)
        }
    }

    private fun uploadImageAndSaveData(nombre: String, apellido: String) {
        val imageRef = storage.reference.child("personas/${UUID.randomUUID()}")

        if (imageUri != null && imageUri.toString().startsWith("file://")) {
            // CASO: imagen tomada con la cámara
            try {
                val file = File(imageUri!!.path!!)
                val bytes = file.readBytes() // convierte la imagen en bytes

                imageRef.putBytes(bytes) // sube los bytes, no la Uri
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            savePersonData(nombre, apellido, downloadUri.toString())
                        }
                    }
                    .addOnFailureListener { e ->
                        showToast("Error al subir imagen: ${e.message}")
                    }
            } catch (e: IOException) {
                showToast("Error leyendo imagen: ${e.message}")
            }

        } else if (imageUri != null) {
            // CASO: imagen desde galería (esto sí funciona normalmente)
            imageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        savePersonData(nombre, apellido, downloadUri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    showToast("Error al subir imagen: ${e.message}")
                }
        } else {
            showToast("No se encontró imagen para subir")
        }
    }


    private fun savePersonData(nombre: String, apellido: String, imageUrl: String) {
        // Objeto sin ID (Firestore lo generará automáticamente)
        val persona = hashMapOf(
            "nombre" to nombre,
            "apellido" to apellido,
            "imagenUrl" to imageUrl,
            "causaMuerte" to "", // Se completará después
            "detalles" to "" // Se completará después
        )

        firestore.collection("personas_muertas")
            .add(persona) // Firestore genera el ID aquí
            .addOnSuccessListener { documentReference ->
                showToast("Registro exitoso")
                startActivity(
                    Intent(this, RegistroDetallesActivity::class.java).apply {
                        putExtra("DOCUMENT_ID", documentReference.id) // Pasamos el ID generado
                    }
                )
                finish()
            }
            .addOnFailureListener { e ->
                showToast("Error al guardar: ${e.message}")
            }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri? {
        return try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val file = File(cacheDir, fileName)
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            }
            Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
