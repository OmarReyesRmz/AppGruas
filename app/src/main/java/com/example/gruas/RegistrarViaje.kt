package com.example.gruas

data class RegistrarViaje(
    val id_cliente: Int,
    val id_conductor: Int? = null,
    val id_grua: Int? = null,
    val latitud_cliente: Double,
    val longitud_cliente: Double,
    val latitud_conductor: Double,
    val longitud_conductor: Double,
    val modelo_del_auto: String,
    val placas_cliente: String,
    val comentarios:String,
    val costo_neutro: Double,
    val costo_iva: Double
)