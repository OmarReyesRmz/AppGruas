package com.example.gruas

class Conductores (
    val id: Int,
    val nombre: String,
    val apellido: String,
    val direccion: String,
    val telefono: String,
    val email: String,
    val password: String,
    val ubicacion: Ubicacion,
    val solicitud: Solicitud,
    val aceptada: Boolean
)