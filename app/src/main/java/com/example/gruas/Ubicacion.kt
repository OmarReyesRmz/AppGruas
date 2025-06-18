package com.example.gruas

data class Ubicacion(
    val latitud: Double,
    val longitud: Double,
    val activo: Boolean,
    val atendido: Boolean,
    val conductor: Int? = null
)

data class Ubicacion2(
    val latitud: Double,
    val longitud: Double,
    val activo: Boolean,
    val atendido: Boolean
)

data class RespuestaServidor(
    val message: String
)


