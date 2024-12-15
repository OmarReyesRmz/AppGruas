package com.example.gruas

data class RegistrarViaje(
    val id_cliente: Int,
    val latitud_cliente: Double,
    val longitud_cliente: Double,
    val tipo_grua: String,
    val direccion_envio: String,
    val modelo_del_auto: String,
    val placas_cliente: String,
    val comentarios:String,
    val latitud_conductor: Double,
    val longitud_conductor: Double,
    val id_conductor: Int
)