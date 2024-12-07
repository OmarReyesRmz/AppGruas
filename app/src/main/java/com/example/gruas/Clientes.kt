package com.example.gruas

import com.example.gruas.Ubicacion

data class Clientes(
    val id: Int,
    val nombre: String,
    val apellido: String,
    val direccion: String,
    val telefono: String,
    val email: String,
    val password: String,
    val ubicacion: Ubicacion
)
