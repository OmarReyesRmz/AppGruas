package com.example.gruas

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var db: DBsqlite
    private var destinationLatLng = LatLng(0.0, 0.0)
    private var currentLatLng: LatLng? = null
    private var isDestinationUpdated = false
    private var banderapaso = false
    private lateinit var clientes_pasados: List<Clientes>
    private var currentDialog2: Dialog? = null
    private var currentDialog3: Dialog? = null

    private val handler = Handler()
    private val updateRunnable = object : Runnable {
        override fun run() {
            // Actualizamos la ubicación del usuario y destino cada 1 segundo
            getMyLocation()
            handler.postDelayed(this, 1000) // Repite cada 1 segundo
        }
    }

    private val updateRunnable2 = object : Runnable {
        override fun run() {
            RetrofitClient.instance.getClientes().enqueue(object : Callback<List<Clientes>> {
                override fun onResponse(
                    call: Call<List<Clientes>>,
                    response: Response<List<Clientes>>
                ) {
                    if (response.isSuccessful) {
                        val clientes = response.body()
                        clientes?.let {
                            if(clientes_pasados != it) {
                                comprobarcliente(it)
                                clientes_pasados = it
                                Log.d("Hola","hubo un cambios")
                            }else{
                                Log.d("Hola","no hay cambios")
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error en la respuesta", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<Clientes>>, t: Throwable) {
                    Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                    Toast.makeText(requireContext(), "Error 123 - : ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
            handler.postDelayed(this,1000)
        }

    }

    private val updateRunnable3 = object : Runnable {
        override fun run() {
            RetrofitClient.instance.getConductores().enqueue(object : Callback<List<Conductores>> {
                override fun onResponse(
                    call: Call<List<Conductores>>,
                    response: Response<List<Conductores>>
                ){
                    if (response.isSuccessful) {
                        val conductores = response.body()
                        conductores?.let {
                            for (conductor in it){
                                if(conductor.aceptada == true && conductor.solicitud.usuario == db.obtenerid()) {
                                    val LatLng = LatLng(db.obtenerLatitud().toDouble(),db.obtenerLongitud().toDouble())
                                    comprobarconductor(it,LatLng)
                                    //Log.d("Hola","hubo un cambios en conductores")
                                }
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error en la respuesta", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<Conductores>>, t: Throwable) {
                    Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                    Toast.makeText(requireContext(), "Error 123 - : ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
            handler.postDelayed(this,1000)
        }
    }

    private val updateRunnable4 = object : Runnable {
        override fun run() {
            RetrofitClient.instance.getConductores().enqueue(object : Callback<List<Conductores>> {
                override fun onResponse(
                    call: Call<List<Conductores>>,
                    response: Response<List<Conductores>>
                ) {
                    if (response.isSuccessful) {
                        val conductores = response.body()
                        conductores?.let {
                            for (conductor in it){
                                if(conductor.aceptada == false && conductor.solicitud.usuario != 0) {
                                    val LatLng = LatLng(db.obtenerLatitud().toDouble(),db.obtenerLongitud().toDouble())
                                    Log.d("Intentando","Intentando borrar solicitud ${db.obtenerid()}")
                                    BorrarSolicitud(db.obtenerid())
                                    ActualizarSolicitudAtivo(conductor.solicitud.usuario, false)
                                    LeerClientes2(true,conductor.solicitud.usuario)
                                    if(db.obtenerTipoUsuario() == "cliente"){
                                        currentDialog3?.dismiss()
                                        currentDialog3 = null
                                    }
                                    Thread.sleep(1000)
                                    actualizarConductor(conductor.id,conductor.ubicacion.latitud.toDouble(),conductor.ubicacion.longitud.toDouble(), false, false,0,false)
                                    isDestinationUpdated = false
                                    updateLocationOnMap(LatLng)
                                    Thread.sleep(1000)
                                    actualizardestinationLatLng(LatLng)
                                    Log.d("Hola","Un conductor termino su viaje")

                                }
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error en la respuesta", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<Conductores>>, t: Throwable) {
                    Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                    Toast.makeText(requireContext(), "Error 123 - : ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
            handler.postDelayed(this,1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        db = DBsqlite(requireContext())
        // Inicializar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Configurar el mapa
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        checkLocationPermissionAndGetLocation()
        // Comenzamos el ciclo de actualización de posiciones
        handler.post(updateRunnable3)
        handler.post(updateRunnable)
    }

    private fun checkLocationPermissionAndGetLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicitar permisos si no están concedidos
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getMyLocation()
        }
    }

    private fun getMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Obtener la ubicación actual
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val newLatLng = LatLng(location.latitude, location.longitude)
                if (currentLatLng == null || currentLatLng != newLatLng) {
                    currentLatLng = newLatLng
                    if (db.obtenerRealizadoPedido() == "REALIZANDO" && db.obtenerTipoUsuario() == "cliente") {
                        db.actualizarlatitud(location.latitude.toFloat())
                        db.actualizarlongitud(location.longitude.toFloat())
                        nuevoviaje()
                        cargadepantalla()
                        LeerClientes2(false, db.obtenerid())
                        actualizardestinationLatLng(newLatLng)
                    } else if(db.obtenerTipoUsuario() == "conductor"){
                        Log.d("Datos_conductor","Datos conductor ID: ${db.obtenerid()}")
                        db.actualizarlatitud(location.latitude.toFloat())
                        db.actualizarlongitud(location.longitude.toFloat())
                        actualizardestinationLatLng(newLatLng)
                    }else{
                        updateLocationOnMap(newLatLng)
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("LocationError", "Error al obtener la ubicación: ${exception.message}")
        }
    }

    private fun updateLocationOnMap(latLng: LatLng) {
        map.clear()  // Limpiar el mapa antes de agregar nuevas ubicaciones
        map.addMarker(MarkerOptions().position(latLng).title("Mi ubicación"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        //Log.d("Hola","estoy actualizando las hubicaciones")
        // Verificar si el destino ha sido actualizado
        if (isDestinationUpdated) {
            Log.d("Datos_conductor","${latLng.longitude} - ${latLng.latitude} /// ${destinationLatLng.longitude} - ${destinationLatLng.latitude}")
            Log.d("Hola2","Realizando viaje")
            getDirections(latLng, destinationLatLng)
            map.addMarker(MarkerOptions().position(destinationLatLng).title("Destino"))
            if(db.obtenerTipoUsuario() == "cliente"){
                NombreConductor { nombre ->
                    showinfo(nombre)
                }
            }else if(db.obtenerTipoUsuario() == "conductor"){
                NombreCliente { nombre ->
                    showinfo2(nombre)
                }
            }
        }
    }

    private fun actualizardestinationLatLng(latLng: LatLng) {
        RetrofitClient.instance.getConductores().enqueue(object : Callback<List<Conductores>> {
            override fun onResponse(
                call: Call<List<Conductores>>,
                response: Response<List<Conductores>>
            ) {
                if (response.isSuccessful) {
                    val conductores = response.body()
                    conductores?.let {
                        comprobarconductor(it, latLng)
                    }
                } else {
                    Toast.makeText(requireContext(), "Error en la respuesta", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Conductores>>, t: Throwable) {
                Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                Toast.makeText(requireContext(), "Error 123 - : ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun comprobarconductor(conductores: List<Conductores>, latLng: LatLng) {
        for (conductor in conductores) {
            //Log.d("Hola1", "Ya acepte un pedido estoy en viaje")
            if (conductor.ubicacion.activo) {
                if(db.obtenerTipoUsuario() == "cliente" && db.obtenerRealizadoPedido() == "REALIZANDO"){
                    if(conductor.aceptada && db.obtenerid() == conductor.solicitud.usuario){
                        val latLng2 = LatLng(conductor.ubicacion.latitud, conductor.ubicacion.longitud)
                        destinationLatLng = latLng
                        isDestinationUpdated = true
                        handler.removeCallbacks(updateRunnable3)
                        handler.post(updateRunnable4)
                        currentDialog2?.dismiss()
                        currentDialog2 = null

                        actualizarCliente(db.obtenerid(),db.obtenerLatitud().toDouble(),db.obtenerLongitud().toDouble(),true,true, conductor.id)
                        updateLocationOnMap(latLng2)
                        break
                    }
                }else if(db.obtenerTipoUsuario() == "conductor" && db.obtenerid() == conductor.id){
                    db.actualizarlatitud(conductor.ubicacion.latitud.toFloat())//latLng.latitud.toFloat()
                    db.actualizarlongitud(conductor.ubicacion.longitud.toFloat())//latLng.longitude.toFloat()
                    Log.d("Hola2", "Ya acepte un pedido estoy en viaje1")
                    if(conductor.aceptada){
                        Log.d("Hola2", "Ya acepte un pedido estoy en viaje2")
                        actualizarConductor(conductor.id,db.obtenerLatitud().toDouble(),db.obtenerLongitud().toDouble(),false,true,conductor.solicitud.usuario,false)
                        val lntlng2 = LatLng(conductor.ubicacion.latitud,conductor.ubicacion.longitud)
                        //Log.d("Hola", "Estoy pasando para actualizar la ubicacion del conductor2")
                        ActualizarDestinationUbication(conductor.solicitud.usuario,lntlng2)
                    }else {
                        handler.removeCallbacks(updateRunnable3)
                        Log.d("Datos_conductor","Entre aqui")
                        LeerClientes()
                    }
                }else if(db.obtenerRealizadoPedido() == "NINGUNO"){
                    isDestinationUpdated = false
                    updateLocationOnMap(latLng)
                }
            }
        }
    }

    fun mostrarpedido(id: Int, nombre: String){
        showCustomDialog("estan solicitando un servicio el cliente $nombre",id)

    }

    fun actualizarConductor(id: Int, latitud: Double, longitud: Double, espera: Boolean, atendido: Boolean, idcliente: Int? = null, Bandera: Boolean) {
        Log.d("Actualizacion", "Conductor $id - Espera: $espera, Atendido: $atendido")
        val conductor = ActualizarDatosConductores(
            latitud = latitud,
            longitud = longitud,
            activo = true,
            atendido = atendido,
            espera = espera,
            usuario = idcliente
        )

        val call = RetrofitClient.instance.actualizarConductores(id, conductor)

        call.enqueue(object : Callback<Conductores> {
            override fun onResponse(call: Call<Conductores>, response: Response<Conductores>) {
                if (response.isSuccessful) {
                    if(Bandera) {
                        LeerConductores(idcliente)
                    }
                } else {
                    // Manejar el error si la respuesta no es exitosa
                    Log.e("Conductor", "Error al actualizar el cliente: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Conductores>, t: Throwable) {
                // Manejar errores de red o problemas con Retrofit
                Log.e("Conductor", "Error de red o conexión: ${t.message}")
            }
        })
    }

    fun NombreCliente(callback: (String) -> Unit) {
        RetrofitClient.instance.getClientes().enqueue(object : Callback<List<Clientes>> {
            override fun onResponse(
                call: Call<List<Clientes>>,
                response: Response<List<Clientes>>
            ) {
                if (response.isSuccessful) {
                    val clientes = response.body()
                    clientes?.let {
                        for (cliente in it) {
                            //Log.d("Ubicaciones clientes", "${db.obtenerid()} - ${cliente.ubicacion.conductor}")
                            if (db.obtenerid() == cliente.ubicacion.conductor) {
                                val nombre = cliente.nombre + " " + cliente.apellido
                                Log.d("Ubicaciones clientes", "Cliente encontrado: $nombre")
                                callback(nombre)
                                return // Salimos del loop y evitamos múltiples llamadas al callback
                            }
                        }
                    }
                    // Si no se encontró el cliente
                    callback("")
                } else {
                    Log.e("API_ERROR", "Error en la respuesta: ${response.code()}")
                    Toast.makeText(requireContext(), "Error en la respuesta", Toast.LENGTH_SHORT).show()
                    callback("")
                }
            }

            override fun onFailure(call: Call<List<Clientes>>, t: Throwable) {
                Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                Toast.makeText(requireContext(), "Error al conectar: ${t.message}", Toast.LENGTH_SHORT).show()
                callback("")
            }
        })
    }

    fun NombreConductor(callback: (String) -> Unit) {
        RetrofitClient.instance.getConductores().enqueue(object : Callback<List<Conductores>> {
            override fun onResponse(
                call: Call<List<Conductores>>,
                response: Response<List<Conductores>>
            ) {
                if (response.isSuccessful) {
                    val conductores = response.body()
                    conductores?.let {
                        for (conductor in it) {
                            Log.d("Ubicaciones cliente", "${db.obtenerid()} - ${conductor.solicitud.usuario}")
                            if (db.obtenerid() == conductor.solicitud.usuario) {
                                val nombre = conductor.nombre + " " + conductor.apellido
                                Log.d("Ubicaciones cliente", "Entre: $nombre")
                                callback(nombre)
                                return
                            }
                        }
                    }
                    // Si no se encontró un conductor
                    callback("")
                } else {
                    Toast.makeText(requireContext(), "Error en la respuesta", Toast.LENGTH_SHORT).show()
                    callback("")
                }
            }

            override fun onFailure(call: Call<List<Conductores>>, t: Throwable) {
                Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                Toast.makeText(requireContext(), "Error al conectar: ${t.message}", Toast.LENGTH_SHORT).show()
                callback("")
            }
        })
    }

    fun LeerConductores(id: Int? = null){
        RetrofitClient.instance.getConductores().enqueue(object : Callback<List<Conductores>> {
            override fun onResponse(
                call: Call<List<Conductores>>,
                response: Response<List<Conductores>>
            ) {
                if (response.isSuccessful) {
                    val conductores = response.body()
                    conductores?.let {
                        comprobarconductores2(it,id)
                    }
                } else {
                    Toast.makeText(requireContext(), "Error en la respuesta", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Conductores>>, t: Throwable) {
                Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                Toast.makeText(requireContext(), "Error 123 - : ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun LeerClientes(){
        Log.d("Datos_conductor","Lectura de clientes")
        RetrofitClient.instance.getClientes().enqueue(object : Callback<List<Clientes>> {
            override fun onResponse(
                call: Call<List<Clientes>>,
                response: Response<List<Clientes>>
            ) {
                if (response.isSuccessful) {
                    val clientes = response.body()
                    clientes?.let {
                        comprobarcliente(it)
                        clientes_pasados = it
                        handler.post(updateRunnable2)
                    }
                } else {
                    Toast.makeText(requireContext(), "Error en la respuesta", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Clientes>>, t: Throwable) {
                Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                Toast.makeText(requireContext(), "Error 123 - : ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun LeerClientes2(Bandera: Boolean, Id: Int){
        RetrofitClient.instance.getClientes().enqueue(object : Callback<List<Clientes>> {
            override fun onResponse(
                call: Call<List<Clientes>>,
                response: Response<List<Clientes>>
            ) {
                if (response.isSuccessful) {
                    val clientes = response.body()
                    clientes?.let {
                        for (cliente in it){
                            if(Id == cliente.id && cliente.ubicacion.conductor != 0 && Bandera){
                                Log.d("Datos_cond","No se porque entre aqui pero entre")
                                db.actualizarrealizarpedido("NINGUNO")
                                handler.removeCallbacks(updateRunnable4)
                                isDestinationUpdated = false
                                banderapaso = false
                                actualizarCliente(cliente.id,cliente.ubicacion.latitud,cliente.ubicacion.longitud,false,false,null)
                                Thread.sleep(500)
                                if(db.obtenerTipoUsuario() == "cliente") {
                                    showCustomDialog2("Ya termino tu pedido puedes volver a pedir\notro servicio cuando quieras")
                                }
                            }else if(db.obtenerid() == cliente.id && db.obtenerRealizadoPedido()=="REALIZANDO" && !Bandera && !cliente.ubicacion.atendido){
                                Log.d("Datos_cond","No se porque entre aqui pero entre")
                                actualizarCliente(db.obtenerid(),db.obtenerLatitud().toDouble(),db.obtenerLongitud().toDouble(),true,false,null)
                                //Log.d("Prubea","Ya actualice el cliente - ${cliente.id} a 0 conductor")
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Error en la respuesta", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Clientes>>, t: Throwable) {
                Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                Toast.makeText(requireContext(), "Error 123 - : ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun comprobarcliente(cliente: List<Clientes>){
        for(clientes in cliente){
            Log.d("Datos_conductor","Mensajes de pedidos")
            if(clientes.ubicacion.activo && !clientes.ubicacion.atendido){
                mostrarpedido(clientes.id, clientes.nombre)
                return
            }
        }
    }

    private fun comprobarconductores2(conductores: List<Conductores>, id: Int? = null){
        var band = 10000000
        //id = id_cliente
        for (conductor in conductores) {
            if(!conductor.aceptada && conductor.solicitud.espera && conductor.id == db.obtenerid()){
                //actualizar solicitud aceptada
                ActualizarSolicitudAceptada(conductor.id)
                band = conductor.id
                break;
            }
        }

        for(conductor in conductores){
            if(!conductor.aceptada && conductor.solicitud.espera && conductor.ubicacion.activo && band != conductor.id){
                actualizarConductor(conductor.id,conductor.ubicacion.latitud.toDouble(),conductor.ubicacion.longitud.toDouble(), false, false,null,false)
            }
        }

        for(conductor in conductores){
            //Log.d("Hola","$id - ${conductor.solicitud.usuario}, ${conductor.aceptada}")
            if(conductor.solicitud.usuario == id){
                val lntlng = LatLng(conductor.ubicacion.latitud,conductor.ubicacion.longitud)
                //Log.d("Hola","Antes de entrar")
                Log.d("Datos_cond","Actualizando viajes")
                actualizarviaje(conductor.id, conductor.ubicacion.latitud, conductor.ubicacion.longitud, conductor.solicitud.usuario)
                ActualizarDestinationUbication(id,lntlng)
                //Log.d("Hola","despues de entrar")
                break
            }
        }
    }

    private fun latitudCliente(id:Int,callback: (Double) -> Unit) {
        RetrofitClient.instance.getClientes().enqueue(object : Callback<List<Clientes>> {
            override fun onResponse(
                call: Call<List<Clientes>>,
                response: Response<List<Clientes>>
            ) {
                if (response.isSuccessful) {
                    val clientes = response.body()
                    clientes?.let {
                        for (cliente in it) {
                            //Log.d("Ubicaciones clientes", "${db.obtenerid()} - ${cliente.ubicacion.conductor}")
                            if (cliente.id == id) {
                                val nombre = cliente.ubicacion.latitud
                                callback(nombre)
                                return // Salimos del loop y evitamos múltiples llamadas al callback
                            }
                        }
                    }
                } else {
                    Log.e("API_ERROR", "Error en la respuesta: ${response.code()}")
                    Toast.makeText(requireContext(), "Error en la respuesta", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Clientes>>, t: Throwable) {
                Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                Toast.makeText(requireContext(), "Error al conectar: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun longitudCliente(id:Int,callback: (Double) -> Unit) {
        RetrofitClient.instance.getClientes().enqueue(object : Callback<List<Clientes>> {
            override fun onResponse(
                call: Call<List<Clientes>>,
                response: Response<List<Clientes>>
            ) {
                if (response.isSuccessful) {
                    val clientes = response.body()
                    clientes?.let {
                        for (cliente in it) {
                            //Log.d("Ubicaciones clientes", "${db.obtenerid()} - ${cliente.ubicacion.conductor}")
                            if (cliente.id == id) {
                                val nombre = cliente.ubicacion.longitud
                                callback(nombre)
                                return // Salimos del loop y evitamos múltiples llamadas al callback
                            }
                        }
                    }
                } else {
                    Log.e("API_ERROR", "Error en la respuesta: ${response.code()}")
                    Toast.makeText(requireContext(), "Error en la respuesta", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Clientes>>, t: Throwable) {
                Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                Toast.makeText(requireContext(), "Error al conectar: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun ActualizarSolicitudAceptada(id: Int){
        val aceptada = ActualizarAceptadaRequest(
            aceptada = true
        )
        val call = RetrofitClient.instance.actualizarAceptada(id,aceptada)

        call.enqueue(object : Callback<Conductores> {
            override fun onResponse(call: Call<Conductores>, response: Response<Conductores>) {
                if (response.isSuccessful) {
                    // La actualización fue exitosa, puedes manejar la respuesta
                    handler.removeCallbacks(updateRunnable2)
                    Log.d("Aceptada", "Conductor actualizado exitosamente")
                } else {
                    // Manejar el error si la respuesta no es exitosa
                    Log.e("Aceptada", "Error al actualizar el cliente: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Conductores>, t: Throwable) {
                // Manejar errores de red o problemas con Retrofit
                Log.e("Aceptada", "Error de red o conexión: ${t.message}")
            }
        })
    }

    fun ActualizarSolicitudAceptada2(id: Int){
        val aceptada = ActualizarAceptadaRequest(
            aceptada = false
        )
        val call = RetrofitClient.instance.actualizarAceptada(id,aceptada)

        call.enqueue(object : Callback<Conductores> {
            override fun onResponse(call: Call<Conductores>, response: Response<Conductores>) {
                if (response.isSuccessful) {
                    // La actualización fue exitosa, puedes manejar la respuesta
                    Log.d("Aceptada", "Conductor actualizado exitosamente")
                } else {
                    // Manejar el error si la respuesta no es exitosa
                    Log.e("Aceptada", "Error al actualizar el cliente: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Conductores>, t: Throwable) {
                // Manejar errores de red o problemas con Retrofit
                Log.e("Aceptada", "Error de red o conexión: ${t.message}")
            }
        })
    }

    fun ActualizarSolicitudAtivo(id: Int, Activo: Boolean){
        val aceptada = ActualizarActivoRequest2(
            activo = Activo
        )
        val call = RetrofitClient.instance.actualizarActivo(id,aceptada)

        call.enqueue(object : Callback<Clientes> {
            override fun onResponse(call: Call<Clientes>, response: Response<Clientes>) {
                if (response.isSuccessful) {
                    // La actualización fue exitosa, puedes manejar la respuesta
                    Log.d("Aceptada", "Conductor actualizado exitosamente")
                } else {
                    // Manejar el error si la respuesta no es exitosa
                    Log.e("Aceptada", "Error al actualizar el cliente: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Clientes>, t: Throwable) {
                // Manejar errores de red o problemas con Retrofit
                Log.e("Aceptada", "Error de red o conexión: ${t.message}")
            }
        })
    }

    fun ActualizarDestinationUbication(id: Int,lntlng:LatLng){
        RetrofitClient.instance.getClientes().enqueue(object : Callback<List<Clientes>> {
            override fun onResponse(
                call: Call<List<Clientes>>,
                response: Response<List<Clientes>>
            ) {
                if (response.isSuccessful) {
                    val clientes = response.body()
                    clientes?.let {
                        //Log.d("Hola","id cliente $id")
                        for(clientes in it){
                            Log.d("Hola","id cliente $id entre")
                            if(clientes.id == id){
                                Log.d("Hola","Actualizada ${clientes.ubicacion.latitud} - ${clientes.ubicacion.longitud} //// ${lntlng.longitude} - ${lntlng.latitude}")
                                destinationLatLng = LatLng(clientes.ubicacion.latitud.toDouble(), clientes.ubicacion.longitud.toDouble())
                                isDestinationUpdated = true
                                updateLocationOnMap(lntlng)
                                break
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Error en la respuesta", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Clientes>>, t: Throwable) {
                Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                Toast.makeText(requireContext(), "Error 123 - : ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun actualizarCliente(id: Int, latitud: Double, longitud: Double,Activo: Boolean, Atendido: Boolean, Conductor: Int? = null) {
        val cliente = ActualizarDatosClientes(
            latitud = latitud,
            longitud = longitud,
            activo = Activo,
            atendido = Atendido,
            conductor = Conductor
        )
        Log.d("Datos_con", cliente.toString())
        val call = RetrofitClient.instance.actualizarCliente(id, cliente)

        call.enqueue(object : Callback<Clientes> {
            override fun onResponse(call: Call<Clientes>, response: Response<Clientes>) {
                if (response.isSuccessful) {
                    Log.d("Cliente", "Cliente actualizado exitosamente")
                } else {
                    Log.e("Cliente", "Error al actualizar el cliente: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Clientes>, t: Throwable) {
                // Manejar errores de red o problemas con Retrofit
                Log.e("Cliente", "Error de red o conexión: ${t.message}")
            }
        })
    }

    private fun getDirections(origin: LatLng, destination: LatLng) {
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&key=AIzaSyBgGEK7O07ZBDfggXBmGlBAZZAUyG56shQ"  // Reemplaza con tu clave de API válida

        Thread {
            try {
                val urlObj = URL(url)
                val connection = urlObj.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonResponse = JSONObject(response.toString())
                Log.d("DirectionsAPI", "Response: $response")

                val status = jsonResponse.getString("status")
                if (status != "OK") {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Error en la API: $status", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                // Extrae las rutas y dibuja la polilínea
                val routes = jsonResponse.getJSONArray("routes")
                if (routes.length() > 0) {
                    val route = routes.getJSONObject(0)
                    val legs = route.getJSONArray("legs")
                    val steps = legs.getJSONObject(0).getJSONArray("steps")

                    // Dibuja los pasos de la ruta en el mapa
                    for (i in 0 until steps.length()) {
                        val step = steps.getJSONObject(i)
                        val polyline = step.getJSONObject("polyline").getString("points")
                        val decodedPath = PolyUtil.decode(polyline)

                        requireActivity().runOnUiThread {
                            val polylineOptions = PolylineOptions()
                                .addAll(decodedPath)
                                .width(8f) // Grosor de la línea
                                .color(android.graphics.Color.BLUE) // Color de la línea
                                .geodesic(true)

                            map.addPolyline(polylineOptions)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DirectionsAPI", "Error al obtener la ruta", e)
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error al obtener la ruta: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)  // Detener las actualizaciones cuando se destruye el fragmento
        handler.removeCallbacks(updateRunnable2)
        handler.removeCallbacks(updateRunnable3)
        handler.removeCallbacks(updateRunnable4)
        currentDialog2?.dismiss()
        currentDialog2 = null
        currentDialog3?.dismiss()
        currentDialog3 = null
    }

    companion object {
        private var currentDialog: AlertDialog? = null
    }

    private fun showCustomDialog(message: String, id: Int, onButtonClick: (() -> Unit)? = null) {
        // Cerrar cualquier diálogo existente antes de crear uno nuevo
        currentDialog?.dismiss()

        val dialogView = layoutInflater.inflate(R.layout.dialog_alert_topbackgroud, null)
        val dialog = android.app.AlertDialog.Builder(requireContext()).setView(dialogView).create()

        // Almacenar la referencia al diálogo actual
        currentDialog = dialog

        // Configura la animación del diálogo
        dialog.window?.apply {
            setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
            attributes.y = 50
            setWindowAnimations(R.style.DialogAnimation2)
            WindowManager.LayoutParams.MATCH_PARENT
            WindowManager.LayoutParams.WRAP_CONTENT
            setDimAmount(0.6f)
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        // Configura el mensaje y el botón
        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialog_person_name)
        val dialogMessage2 = dialogView.findViewById<TextView>(R.id.dialog_car_model)
        val dialogMessage3 = dialogView.findViewById<TextView>(R.id.dialog_address)
        val dialogButton = dialogView.findViewById<Button>(R.id.dialog_button)

        dialogButton.setOnClickListener {
            dialogButton.isEnabled = false
            actualizarConductor(
                db.obtenerid(),
                db.obtenerLatitud().toDouble(),
                db.obtenerLongitud().toDouble(),
                true,
                false,
                id,
                true
            )
            onButtonClick?.invoke()

            // Limpiar la referencia al diálogo actual
            currentDialog = null
            dialog.dismiss()
        }


        RetrofitClient.instance.getViajes().enqueue(object : Callback<List<RegistrarViaje>> {
            override fun onResponse(
                call: Call<List<RegistrarViaje>>,
                response: Response<List<RegistrarViaje>>
            ) {
                if (response.isSuccessful) {
                    val viajes = response.body()
                    viajes?.let{
                        for(viaje in it){
                            if(viaje.id_cliente == id){
                                dialogMessage2.text = viaje.modelo_del_auto
                                dialogMessage3.text = viaje.placas_cliente
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Error en la respuesta", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<RegistrarViaje>>, t: Throwable) {
                Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                Toast.makeText(requireContext(), "Error 123 - : ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        dialogMessage.text = message

        dialog.setOnDismissListener {
            dialogButton.isEnabled = false
            // Limpiar la referencia al diálogo actual
            if (currentDialog == dialog) {
                currentDialog = null
            }
        }

        dialog.show()
    }

    private fun cargadepantalla(onButtonClick: (() -> Unit)? = null) {
        // Infla el diseño del diálogo
        if(db.obtenerRealizadoPedido() == "REALIZANDO") {
            val dialogView = layoutInflater.inflate(R.layout.dialog_alert_waiting, null)
            val dialog =
                android.app.AlertDialog.Builder(requireContext()).setView(dialogView).create()

            // Configura la animación del diálogo
            dialog.window?.apply {
                setWindowAnimations(R.style.DialogAnimation) // Animaciones personalizadas
                setDimAmount(0.6f) // Atenuar el fondo (entre 0.0 y 1.0)
                setBackgroundDrawable(ColorDrawable(Color.parseColor("#00000000")))
            }
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(false)

            // Encuentra la vista que girará dentro de `dialogView`
            val rotatingView =
                dialogView.findViewById<ImageView>(R.id.dialog_icon) // Cambiado a `dialogView`
            if (rotatingView != null) {
                val rotateAnimation =
                    AnimationUtils.loadAnimation(requireContext(), R.anim.dialog_animation_infinite)
                rotatingView.startAnimation(rotateAnimation)
            } else {
                // Si no se encuentra la vista, puedes manejarlo con un log o excepción
                Log.e(
                    "DialogError",
                    "No se encontró el elemento con ID dialog_icon en el diseño del diálogo."
                )
            }

            dialog.show()
            currentDialog2 = dialog
        }
    }

    private fun showCustomDialog2(message: String, onButtonClick: (() -> Unit)? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_alert, null)
        val dialog = android.app.AlertDialog.Builder(requireContext()).setView(dialogView).create()

        // Configura la animación del diálogo
        dialog.window?.apply {
            setWindowAnimations(R.style.DialogAnimation) // Animaciones personalizadas
            setDimAmount(0.6f) // Atenuar el fondo (entre 0.0 y 1.0)
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        // Configura el mensaje y el botón
        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialog_message)
        val dialogButton = dialogView.findViewById<Button>(R.id.dialog_button)

        dialogMessage.text = message

        dialogButton.setOnClickListener {
            // Deshabilitar el botón inmediatamente
            dialogButton.isEnabled = false

            // Ejecutar la acción personalizada solo una vez
            onButtonClick?.invoke()

            // Cerrar el diálogo después de un pequeño retraso para asegurar que no se active de nuevo
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            // Deshabilitar animaciones adicionales al cerrar
            //Toast.makeText(this,"hola").show()
            dialogButton.isEnabled = false
        }

        dialog.show()
    }

    private fun actualizarviaje(id_conductor : Int, latitud_conductor: Double, longitud_conductor: Double, id_cliente: Int){
        val viajes = ActualizarViaje(
            id_conductor = id_conductor,
            latitud_conductor = latitud_conductor,
            longitud_conductor = longitud_conductor,
            id_cliente = id_cliente
        )

        val call = RetrofitClient.instance.actualizarViaje(viajes)

        Log.d("Pruebac","$viajes")

        call.enqueue(object : Callback<RegistrarViaje> {
            override fun onResponse(call: Call<RegistrarViaje>, response: Response<RegistrarViaje>) {
                if (response.isSuccessful) {
                    // La actualización fue exitosa, puedes manejar la respuesta
                    Log.d("Pruebac", "Conductor actualizado exitosamente")
                } else {
                    // Manejar el error si la respuesta no es exitosa
                    Log.e("Pruebac", "Error al actualizar: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<RegistrarViaje>, t: Throwable) {
                // Manejar errores de red o problemas con Retrofit
                Log.e("Pruebac", "Error de red o conexión: ${t.message}")
            }
        })
    }

    private fun nuevoviaje(){
        var id_cliente: Int = 0
        var latitud_cliente: Double = 0.0000
        var longitud_cliente: Double= 0.0000
        var id_grua: Int? = null
        var direccion_envio: String = ""
        var modelo_del_auto: String = ""
        var placas_cliente: String = ""
        var comentarios:String = ""
        var latitud_conductor: Double= 0.0000
        var longitud_conductor: Double= 0.0000
        var id_conductor: Int? = null
        var costo_neutro: Double = 0.0000
        var costo_iva: Double = 0.0000

        if(db.obtenerTipoUsuario() == "cliente"){
            id_cliente = db.obtenerid()
            latitud_cliente = db.obtenerLatitud().toDouble()
            longitud_cliente = db.obtenerLongitud().toDouble()
            comentarios = db.obtenerComentarios()
            placas_cliente = db.obtenerPlacas()
            modelo_del_auto = db.obtenerModeloauto()
        }

        val nuevoViaje =
            RegistrarViaje(id_cliente,id_conductor,id_grua,latitud_cliente,longitud_cliente,latitud_conductor,
                            longitud_conductor,modelo_del_auto,placas_cliente,comentarios,costo_neutro,costo_iva)

        Log.d("Problema1", nuevoViaje.toString())
        //Verificar que no este ya en un viaje
        RetrofitClient.instance.getClientes().enqueue(object : Callback<List<Clientes>> {
            override fun onResponse(
                call: Call<List<Clientes>>,
                response: Response<List<Clientes>>
            ) {
                if (response.isSuccessful) {
                    val clientes = response.body()
                    clientes?.let {
                        for(cliente in it){
                            //Log.d("Nuevo viaje","${db.obtenerid()} = ${cliente.id} && ${cliente.ubicacion.activo}")
                            if(db.obtenerid() == cliente.id){
                                nuevoviaje2(nuevoViaje)
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Error en la respuesta", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Clientes>>, t: Throwable) {
                Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                Toast.makeText(requireContext(), "Error 123 - : ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun nuevoviaje2(nuevoViaje: RegistrarViaje){
        // Llamada a la API para registrar al cliente
        RetrofitClient.instance.registrarViaje(nuevoViaje).enqueue(object :
            retrofit2.Callback<RegistrarViaje> {  // Cambia el tipo de respuesta al esperado por la API
            override fun onResponse(call: retrofit2.Call<RegistrarViaje>, response: retrofit2.Response<RegistrarViaje>) {
                if (response.isSuccessful) {
                    // Procesar la respuesta exitosa
                    Log.d("Hola","Viaje nuevo registrado")
                } else {
                    Log.e("API_ERROR", "Error en la respuesta: ${response.code()}")
                    Toast.makeText(requireContext(), "Error en el registro: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<RegistrarViaje>, t: Throwable) {
                // Manejar errores de conexión o excepciones
                Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                Toast.makeText(requireContext(), "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showinfo(message: String,onButtonClick: (() -> Unit)? = null) {
        // Cerrar cualquier diálogo existente antes de crear uno nuevo
        currentDialog?.dismiss()

        val dialogView = layoutInflater.inflate(R.layout.dialog_info_conductor, null)
        val dialog = android.app.AlertDialog.Builder(requireContext()).setView(dialogView).create()

        // Almacenar la referencia al diálogo actual
        currentDialog = dialog

        // Configura la animación del diálogo
        dialog.window?.apply {
            setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
            attributes.y = 50
            setWindowAnimations(R.style.DialogAnimation2)
            WindowManager.LayoutParams.MATCH_PARENT
            WindowManager.LayoutParams.WRAP_CONTENT
            setDimAmount(0f)
            setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        // Configura el mensaje y el botón
        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialog_person_name)
        dialogMessage.text = message

        dialog.show()
        currentDialog3 = dialog
    }

    private fun showinfo2(message: String,onButtonClick: (() -> Unit)? = null) {
        // Cerrar cualquier diálogo existente antes de crear uno nuevo
        currentDialog?.dismiss()

        val dialogView = layoutInflater.inflate(R.layout.dialog_info_cliente, null)
        val dialog = android.app.AlertDialog.Builder(requireContext()).setView(dialogView).create()

        // Almacenar la referencia al diálogo actual
        currentDialog = dialog

        // Configura la animación del diálogo
        dialog.window?.apply {
            setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
            attributes.y = 50
            setWindowAnimations(R.style.DialogAnimation2)
            WindowManager.LayoutParams.MATCH_PARENT
            WindowManager.LayoutParams.WRAP_CONTENT
            setDimAmount(0f)
            setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        // Configura el mensaje y el botón
        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialog_person_name)
        val dialogMessage2 = dialogView.findViewById<TextView>(R.id.dialog_car_model)
        val dialogMessage3 = dialogView.findViewById<TextView>(R.id.dialog_address)
        val dialogButton = dialogView.findViewById<Button>(R.id.dialog_button2)

        RetrofitClient.instance.getViajes().enqueue(object : Callback<List<RegistrarViaje>> {
            override fun onResponse(
                call: Call<List<RegistrarViaje>>,
                response: Response<List<RegistrarViaje>>
            ) {
                if (response.isSuccessful) {
                    val viajes = response.body()
                    viajes?.let{
                        for(viaje in it){
                            if(viaje.id_conductor == db.obtenerid()){
                                dialogMessage.text = (viaje.costo_neutro + viaje.costo_iva).toString()
                                dialogMessage2.text = viaje.modelo_del_auto
                                dialogMessage3.text = viaje.placas_cliente
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Error en la respuesta", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<RegistrarViaje>>, t: Throwable) {
                Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                Toast.makeText(requireContext(), "Error 123 - : ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        dialogMessage.text = message

        dialogButton.setOnClickListener {

            dialogButton.isEnabled = false
            ActualizarSolicitudAceptada2(db.obtenerid())
            handler.post(updateRunnable4)
            onButtonClick?.invoke()
            // Limpiar la referencia al diálogo actual
            currentDialog = null
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            dialogButton.isEnabled = false
            // Limpiar la referencia al diálogo actual
            if (currentDialog == dialog) {
                currentDialog = null
            }
        }

        dialog.show()
    }

    private fun BorrarSolicitud(id: Int){
        val call = RetrofitClient.instance.eliminarSolicitud(id)

        call.enqueue(object : Callback<RespuestaServidor> {
            override fun onResponse(call: Call<RespuestaServidor>, response: Response<RespuestaServidor>) {
                if (response.isSuccessful) {
                    println("Respuesta del servidor: ${response.body()?.message}")
                } else {
                    println("Error HTTP: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<RespuestaServidor>, t: Throwable) {
                println("Fallo de red o servidor: ${t.message}")
            }
        })
    }
}
