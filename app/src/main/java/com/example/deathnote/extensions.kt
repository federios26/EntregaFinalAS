package com.example.deathnote

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

fun Bitmap.toUri(context: Context): Uri? {
    return try {
        // 1. Crear un archivo temporal único
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )

        // 2. Comprimir el Bitmap al archivo
        FileOutputStream(file).use { fos ->
            this.compress(Bitmap.CompressFormat.JPEG, 85, fos)
        }

        // 3. Devolver la Uri del archivo
        Uri.fromFile(file)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

