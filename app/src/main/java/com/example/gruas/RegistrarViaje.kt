package com.example.gruas

data class RegistrarViaje(
    val id_viaje: Int,
    val id_cliente: Int,
    val latitud_cliente: Int,
    val longitud_cliente: Int,
    val tipo_grua: String,
    val direccion_envio: String,
    val modelo_del_auto: String,
    val placas_cliente: Int,
    val comentarios:String,
    val latitud_conductor: Int,
    val longitud_conductor: Int,
    val id_conductor: Int
)
