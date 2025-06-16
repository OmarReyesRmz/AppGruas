package com.example.gruas

data class Gruas(
    val id_grua: Int,
    val modelo: String,
    val no_serie: String,
    val placa: String,
    val tipo_grua: String,
    val id_conductor: Int? = null
)
