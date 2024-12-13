package com.example.gruas

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
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
    private lateinit var clientes_pasados: List<Clientes>
    private lateinit var conductores_pasados: Conductores

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
                ) {
                    if (response.isSuccessful) {
                        val conductores = response.body()
                        conductores?.let {
                            for (conductor in it){
                                if(conductor.aceptada == true && conductor.solicitud.usuario == db.obtenerid()) {
                                    val LatLng = LatLng(db.obtenerLatitud().toDouble(),db.obtenerLongitud().toDouble())
                                    comprobarconductor(it,LatLng)
                                    Log.d("Hola","hubo un cambios en conductores")
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
                        actualizardestinationLatLng(newLatLng)
                    } else if(db.obtenerTipoUsuario() == "conductor"){
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
            //Log.d("Hola","${latLng.longitude} - ${latLng.latitude} /// ${destinationLatLng.longitude} - ${destinationLatLng.latitude}")
            map.addMarker(MarkerOptions().position(destinationLatLng).title("Destino"))
            getDirections(latLng, destinationLatLng)
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
            if (conductor.ubicacion.activo) {
                if(db.obtenerTipoUsuario() == "cliente"){
                    if(conductor.aceptada && db.obtenerid() == conductor.solicitud.usuario){
                        destinationLatLng = LatLng(conductor.ubicacion.latitud, conductor.ubicacion.longitud)
                        isDestinationUpdated = true
                        actualizarCliente(db.obtenerid(),db.obtenerLatitud().toDouble(),db.obtenerLongitud().toDouble(),true, conductor.id)
                        updateLocationOnMap(latLng)
                        handler.removeCallbacks(updateRunnable3)
                    }else{
                        actualizarCliente(db.obtenerid(),db.obtenerLatitud().toDouble(),db.obtenerLongitud().toDouble(),false,0)
                    }
                }else if(db.obtenerTipoUsuario() == "conductor" && db.obtenerid() == conductor.id){
                    db.actualizarlatitud(conductor.ubicacion.latitud.toFloat())//latLng.latitud.toFloat()
                    db.actualizarlongitud(conductor.ubicacion.longitud.toFloat())//latLng.longitude.toFloat()
                    if(conductor.aceptada){
                        //Log.d("Hola", "Ya acepte un pedido estoy en viaje")
                        actualizarConductor(conductor.id,db.obtenerLatitud().toDouble(),db.obtenerLongitud().toDouble(),false,true,conductor.solicitud.usuario,false)
                        val lntlng2 = LatLng(conductor.ubicacion.latitud,conductor.ubicacion.longitud)
                        //Log.d("Hola", "Estoy pasando para actualizar la ubicacion del conductor2")
                        ActualizarDestinationUbication(conductor.solicitud.usuario,lntlng2)
                    }else {
                        Log.d("Hola", "No paso")
                        LeerClientes()
                    }
                    return
                }
            }
        }
    }

    fun mostrarpedido(id: Int, nombre: String){
        showCustomDialog("estan solicitando un servicio el cliente $nombre",id)

    }

    fun actualizarConductor(id: Int, latitud: Double, longitud: Double, espera: Boolean, atendido: Boolean, idcliente: Int, Bandera: Boolean) {
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
                    // La actualización fue exitosa, puedes manejar la respuesta
                    Log.d("Conductor", "Conductor actualizado exitosamente")
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

    fun LeerConductores(id: Int){
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

    fun comprobarcliente(cliente: List<Clientes>){
        for(clientes in cliente){
            if(clientes.ubicacion.activo && !clientes.ubicacion.atendido){
                mostrarpedido(clientes.id, clientes.nombre)
                return
            }
        }
    }

    private fun comprobarconductores2(conductores: List<Conductores>, id: Int){
        var band = 10000000
        for (conductor in conductores) {
            if(!conductor.aceptada && conductor.solicitud.espera && conductor.id == db.obtenerid()){
                //actualizar solicitud aceptada
                //Log.d("Hola","$id cliente")
                ActualizarSolicitudAceptada(conductor.id)
                band = conductor.id
                //Log.d("Hola","$id cliente sali ya esta aceptada")
                break;
            }
        }

        for(conductor in conductores){
            if(!conductor.aceptada && conductor.solicitud.espera && conductor.ubicacion.activo && band != conductor.id){
                actualizarConductor(conductor.id,conductor.ubicacion.latitud.toDouble(),conductor.ubicacion.longitud.toDouble(), false, false,0,false)
            }
        }

        for(conductor in conductores){
            //Log.d("Hola","$id - ${conductor.solicitud.usuario}, ${conductor.aceptada}")
            if(conductor.solicitud.usuario == id){
                val lntlng = LatLng(conductor.ubicacion.latitud,conductor.ubicacion.longitud)
                //Log.d("Hola","Antes de entrar")
                ActualizarDestinationUbication(id,lntlng)
                //Log.d("Hola","despues de entrar")
                break
            }
        }
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
                            //Log.d("Hola","id cliente $id entre")
                            if(clientes.id == id){
                                //Log.d("Hola","Actualizada ${clientes.ubicacion.latitud} - ${clientes.ubicacion.longitud} //// ${lntlng.longitude} - ${lntlng.latitude}")
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

    fun actualizarCliente(id: Int, latitud: Double, longitud: Double, Atendido: Boolean, Conductor: Int) {
        val cliente = ActualizarDatosClientes(
            latitud = latitud,
            longitud = longitud,
            activo = true,
            atendido = Atendido,
            conductor = Conductor
        )

        val call = RetrofitClient.instance.actualizarCliente(id, cliente)

        call.enqueue(object : Callback<Clientes> {
            override fun onResponse(call: Call<Clientes>, response: Response<Clientes>) {
                if (response.isSuccessful) {
                    // La actualización fue exitosa, puedes manejar la respuesta
                    Log.d("Cliente", "Cliente actualizado exitosamente")
                } else {
                    // Manejar el error si la respuesta no es exitosa
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
    }

    companion object {
        private var currentDialog: AlertDialog? = null
    }

    private fun showCustomDialog(message: String, id: Int, onButtonClick: (() -> Unit)? = null) {
        // Cerrar cualquier diálogo existente antes de crear uno nuevo
        currentDialog?.dismiss()

        val dialogView = layoutInflater.inflate(R.layout.dialog_alert, null)
        val dialog = android.app.AlertDialog.Builder(requireContext()).setView(dialogView).create()

        // Almacenar la referencia al diálogo actual
        currentDialog = dialog

        // Configura la animación del diálogo
        dialog.window?.apply {
            setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
            attributes.y = 50
            setWindowAnimations(R.style.DialogAnimation)
            WindowManager.LayoutParams.MATCH_PARENT
            WindowManager.LayoutParams.WRAP_CONTENT
            setDimAmount(0.6f)
        }

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        // Configura el mensaje y el botón
        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialog_message)
        val dialogButton = dialogView.findViewById<Button>(R.id.dialog_button)

        dialogMessage.text = message

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

        dialog.setOnDismissListener {
            dialogButton.isEnabled = false
            // Limpiar la referencia al diálogo actual
            if (currentDialog == dialog) {
                currentDialog = null
            }
        }

        dialog.show()
    }
}
