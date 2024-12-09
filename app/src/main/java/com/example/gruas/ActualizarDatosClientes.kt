package com.example.gruas

data class ActualizarDatosClientes (
    val latitud: Double,
    val longitud: Double,
    val activo: Boolean,
    val atendido: Boolean,
    val conductor: Int
)