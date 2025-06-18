package com.example.gruas

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    //Ver Clientes - Conductores - Viajes
    @GET("clientes/ver-clientes/") // Ruta específica de tu API
    fun getClientes(): Call<List<Clientes>>

    @GET("conductores/ver-conductores/")
    fun getConductores(): Call<List<Conductores>>

    @GET("viajes/ver-viajes/")
    fun getViajes(): Call<List<RegistrarViaje>>

    @GET("gruas/ver-gruas/")
    fun getGruas(): Call<List<Gruas>>


    //Registro de Clientes - Viajes
    @POST("clientes/Registrar-Cliente/")
    fun registrarCliente(@Body cliente: Clientes): Call<Clientes>

    @POST("viajes/Registrar-Viaje/")
    fun registrarViaje(@Body viaje: RegistrarViaje): Call<RegistrarViaje>


    //Actualizar los Clientes - Conductores - Viajes
    @PUT("clientes/actualizar-ubicacion/clientes/{id}")
    fun actualizarCliente(
        @Path("id") id: Int,  // ID del cliente a actualizar
        @Body cliente: ActualizarDatosClientes  // Objeto Cliente que contiene los datos a actualizar
    ): Call<Clientes>

    @PUT("conductores/actualizar-ubicacion/conductores/{id}")
    fun actualizarConductores(
        @Path("id") id: Int,
        @Body conductor: ActualizarDatosConductores
    ): Call<Conductores>

    @PUT("conductores/actualizar-aceptada/conductores/{id}")
    fun actualizarAceptada(
        @Path("id") id: Int,
        @Body request: ActualizarAceptadaRequest
    ): Call<Conductores>

    @DELETE("clientes/eliminar-solicitud/:{id_conductor}")
    fun eliminarSolicitud(
        @Path("id_conductor") idConductor: Int
    ): Call<RespuestaServidor>


    @PUT("clientes/actualizar-activo/clientes/:{id}")
    fun actualizarActivo(
        @Path("id") id: Int,
        @Body request: ActualizarActivoRequest2
    ): Call<Clientes>

    @PUT("viajes/Actualizar-Viajes/")
    fun actualizarViaje(@Body viaje: ActualizarViaje): Call<RegistrarViaje>

}
