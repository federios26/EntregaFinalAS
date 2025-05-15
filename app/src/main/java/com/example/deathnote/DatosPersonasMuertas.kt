package com.example.deathnote

data class DatosPersonasMuertas(
    val id: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val causaMuerte: String = "",
    val detalles: String = "",
    val imagenUrl: String? = null
)