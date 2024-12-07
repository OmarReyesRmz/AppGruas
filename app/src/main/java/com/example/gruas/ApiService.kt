package com.example.gruas

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("ver-clientes/") // Ruta específica de tu API
    fun getClientes(): Call<List<Clientes>>

    @GET("ver-conductores/")
    fun getConductores(): Call<List<Conductores>>

    @POST("Registrar-Cliente/")
    fun registrarCliente(@Body cliente: Clientes): Call<Clientes>

}
