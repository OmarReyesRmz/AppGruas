package com.example.gruas

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("ver-clientes/") // Ruta específica de tu API
    fun getClientes(): Call<List<Clientes>>

    @GET("ver-conductores/")
    fun getConductores(): Call<List<Conductores>>

    @PUT("actualizar-ubicacion/clientes/{id}")
    fun actualizarCliente(
        @Path("id") id: Int,  // ID del cliente a actualizar
        @Body cliente: ActualizarDatosClientes  // Objeto Cliente que contiene los datos a actualizar
    ): Call<Clientes>

    @PUT("actualizar-ubicacion/conductores/{id}")
    fun actualizarConductores(
        @Path("id") id: Int,
        @Body conductor: ActualizarDatosConductores
    ): Call<Conductores>

    @PUT("actualizar-aceptada/conductores/{id}")
    fun actualizarAceptada(
        @Path("id") id: Int,
        @Body request: ActualizarAceptadaRequest
    ): Call<Conductores>

    @PUT("actualizar-activo/clientes/:{id}")
    fun actualizarActivo(
        @Path("id") id: Int,
        @Body request: ActualizarActivoRequest2
    ): Call<Clientes>


    @POST("Registrar-Cliente/")
    fun registrarCliente(@Body cliente: Clientes): Call<Clientes>

    @POST("Registrar-Viaje/")
    fun registrarViaje(@Body viaje: RegistrarViaje): Call<RegistrarViaje>


}
