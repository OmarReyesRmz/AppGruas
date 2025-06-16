package com.example.gruas

import com.google.gson.annotations.SerializedName

class Conductores (
    @SerializedName("id_usuario") val id: Int,
    val nombre: String,
    val apellido: String,
    val direccion: String,
    val telefono: String,
    val email: String,
    val password: String,
    val ubicacion: Ubicacion2,
    val solicitud: Solicitud,
    val aceptada: Boolean
)