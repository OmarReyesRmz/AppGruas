package com.example.gruas

data class ActualizarDatosConductores(
    val latitud: Double,
    val longitud: Double,
    val activo: Boolean,
    val atendido: Boolean,
    val espera: Boolean,
    val usuario: Int
)
