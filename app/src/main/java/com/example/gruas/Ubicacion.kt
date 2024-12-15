package com.example.gruas

data class Ubicacion(
    val latitud: Double,
    val longitud: Double,
    val activo: Boolean,
    val atendido: Boolean,
    val conductor: Int
)

data class Ubicacion2(
    val latitud: Double,
    val longitud: Double,
    val activo: Boolean,
    val atendido: Boolean
)

