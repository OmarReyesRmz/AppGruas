package com.example.gruas

import com.example.gruas.Ubicacion
import com.google.gson.annotations.SerializedName

data class Clientes(
    @SerializedName("id_usuario") val id: Int,
    val nombre: String,
    val apellido: String,
    val direccion: String,
    val telefono: String,
    val email: String,
    val password: String,
    val ubicacion: Ubicacion
)
