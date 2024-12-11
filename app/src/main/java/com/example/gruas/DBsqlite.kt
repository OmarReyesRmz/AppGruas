package com.example.gruas

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.Vector

class DBsqlite(context: Context?): SQLiteOpenHelper(context, TABLE_NAME, null, DATABASE_VERSION){
    companion object{
        val DATABASE_VERSION: Int = 1
        val TABLE_NAME: String = "datos"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE_NAME (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "realizado_pedido TEXT NOT NULL," +          // Para identificar en qué nivel va
                    "nombre TEXT NOT NULL," +
                    "apellidos TEXT NOT NULL," +          // Para identificar en qué mundo va
                    "telefono TEXT NOT NULL," +
                    "correo TEXT NOT NULL," +   // 1 para true (sí es la primera vez), 0 para false
                    "direccion TEXT NOT NULL," +  // Dinero acumulado
                    "tipo_usuario TEXT NOT NULL," +      // Tipo de serpiente elegida por el jugador
                    "latitud FLOAT NOT NULL," +
                    "longitud FLOAT NOT NULL," +
                    "logeado TEXT NOT NULL," +
                    "ubicacion TEXT NOT NULL," +
                    "tipodegrua TEXT NOT NULL," +
                    "direcciondeenvio TEXT NOT NULL," +
                    "modeloauto TEXT NOT NULL," +
                    "placas TEXT NOT NULL," +
                    "comentarios TEXT NOT NULL" +
                    ")"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //En caso de una nueva version haria que actualizar las tablas
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //En caso de regresar a una version anterior que habria que actualizar las tablas
    }

    fun datosExistentes(): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME", null)

        var existe = false
        if (cursor.moveToFirst()) {
            existe = cursor.getInt(0) > 0 // Si COUNT(*) es mayor a 0, significa que hay datos
        }

        cursor.close() // Cierra el cursor para liberar recursos
        return existe // Devuelve true si hay datos, false si no
    }


    fun guardarDatos(realizado_pedido: String, nombre: String, apellidos: String,
                     telefono: String, correo: String, direccion: String,
                     tipo_usuario: String, latitud: Float, longitud: Float, logeado:String,
                     ubicacion: String, tipodegrua: String, direcciondeenvio: String, modeloauto: String, placas: String, comentarios: String){
        val db = writableDatabase
        db.execSQL(
            "INSERT INTO $TABLE_NAME (realizado_pedido, nombre, apellidos, telefono, correo, direccion, tipo_usuario, latitud, longitud,logeado,ubicacion,tipodegrua,direcciondeenvio,modeloauto,placas,comentarios\n ) " +
                    "VALUES('$realizado_pedido', '$nombre', '$apellidos', '$telefono', '$correo', '$direccion', '$tipo_usuario', $latitud, $longitud, '$logeado', '$ubicacion', '$tipodegrua','$direcciondeenvio', '$modeloauto', '$placas', '$comentarios')\n"
        )

    }

    // Métodos para actualizar (SET)
    fun actualizarid(primeravez: Int) {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET id = $primeravez")
        db.close()
    }

    // Métodos para actualizar (SET)
    fun actualizarrealizarpedido(primeravez: String) {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET realizado_pedido = '$primeravez'")
        db.close()
    }

    fun actualizarnombre(nivel: String) {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET nombre = '$nivel'")
        db.close()
    }

    fun actualizarapellidos(nivel_jugando: String) {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET apellidos = '$nivel_jugando'")
        db.close()
    }

    fun actualizartelefono(mundo: String) {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET telefono = '$mundo'")
        db.close()
    }

    fun actualizarcorreo(mundo_jugando: String) {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET correo = '$mundo_jugando'")
        db.close()
    }

    fun actualizardireccion(dineroTotal: String) {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET direccion = '$dineroTotal'")
        db.close()
    }

    fun actualizartipo_usuario(tipoSerpiente: String) {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET tipo_usuario = '$tipoSerpiente'")
        db.close()
    }

    fun actualizarlatitud(iman: Float) {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET latitud = $iman")
        db.close()
    }

    fun actualizarlongitud(monedax5: Float) {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET longitud = $monedax5")
        db.close()
    }

    fun actualizarlogeado(monedax5: String) {
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET logeado = '$monedax5'")
        db.close()
    }

    fun actualizarubicacion(monedax5: String){
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET ubicacion = '$monedax5'")
        db.close()
    }

    fun actualizartipodegrua(monedax5: String){
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET tipodegrua = '$monedax5'")
        db.close()
    }

    fun actualizardirecciondeenvio(monedax5: String){
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET direcciondeenvio = '$monedax5'")
        db.close()
    }

    fun actualizarmodeloauto(monedax5: String){
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET modeloauto = '$monedax5'")
        db.close()
    }

    fun actualizarplacas(monedax5: String){
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET placas = '$monedax5'")
        db.close()
    }

    fun actualizarcomentarios(monedax5: String){
        val db = writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET comentarios = '$monedax5'")
        db.close()
    }

    
    // Métodos para obtener valores (GET)
    fun obtenerid(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id FROM $TABLE_NAME", null)
        var tipoSerpiente = 0
        if (cursor.moveToFirst()) {
            tipoSerpiente = cursor.getInt(0)
        }
        cursor.close()
        return tipoSerpiente
    }

    fun obtenernombre(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT nombre FROM $TABLE_NAME", null)
        var tipoSerpiente = ""
        if (cursor.moveToFirst()) {
            tipoSerpiente = cursor.getString(0)
        }
        cursor.close()
        return tipoSerpiente
    }


    // Métodos para obtener valores (GET)
    fun obtenerRealizadoPedido(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT realizado_pedido FROM $TABLE_NAME", null)
        var realizadoPedido = ""
        if (cursor.moveToFirst()) {
            realizadoPedido = cursor.getString(0)
        }
        cursor.close()
        return realizadoPedido
    }

    fun obtenerApellidos(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT apellidos FROM $TABLE_NAME", null)
        var apellidos = ""
        if (cursor.moveToFirst()) {
            apellidos = cursor.getString(0)
        }
        cursor.close()
        return apellidos
    }

    fun obtenerTelefono(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT telefono FROM $TABLE_NAME", null)
        var telefono = ""
        if (cursor.moveToFirst()) {
            telefono = cursor.getString(0)
        }
        cursor.close()
        return telefono
    }

    fun obtenerCorreo(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT correo FROM $TABLE_NAME", null)
        var correo = ""
        if (cursor.moveToFirst()) {
            correo = cursor.getString(0)
        }
        cursor.close()
        return correo
    }

    fun obtenerDireccion(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT direccion FROM $TABLE_NAME", null)
        var direccion = ""
        if (cursor.moveToFirst()) {
            direccion = cursor.getString(0)
        }
        cursor.close()
        return direccion
    }

    fun obtenerTipoUsuario(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT tipo_usuario FROM $TABLE_NAME", null)
        var tipoUsuario = ""
        if (cursor.moveToFirst()) {
            tipoUsuario = cursor.getString(0)
        }
        cursor.close()
        return tipoUsuario
    }

    fun obtenerLatitud(): Float {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT latitud FROM $TABLE_NAME", null)
        var latitud = 0.0f // Cambiar a tipo Float

        try {
            if (cursor.moveToFirst()) {
                latitud = cursor.getFloat(0) // Obtener el valor de latitud como Float
            }
        } catch (e: Exception) {
            e.printStackTrace() // Para capturar cualquier excepción si ocurre
        } finally {
            cursor.close() // Asegurarse de cerrar el cursor
        }

        return latitud
    }


    fun obtenerLongitud(): Float {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT longitud FROM $TABLE_NAME", null)
        var longitud = 0.0f
        if (cursor.moveToFirst()) {
            longitud = cursor.getFloat(0)
        }
        cursor.close()
        return longitud
    }

    fun obtenerLogeado(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT logeado FROM $TABLE_NAME", null)
        var logeado = ""
        if (cursor.moveToFirst()) {
            logeado = cursor.getString(0)
        }
        cursor.close()
        return logeado
    }

    fun obtenerUbicacion(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT ubicacion FROM $TABLE_NAME", null)
        var ubicacion = ""
        if (cursor.moveToFirst()) {
            ubicacion = cursor.getString(0)
        }
        cursor.close()
        return ubicacion
    }

    fun obtenerTipodegrua(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT tipodegrua FROM $TABLE_NAME", null)
        var tipodegrua = ""
        if (cursor.moveToFirst()) {
            tipodegrua = cursor.getString(0)
        }
        cursor.close()
        return tipodegrua
    }

    fun obtenerDirecciondeenvio(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT direcciondeenvio FROM $TABLE_NAME", null)
        var direcciondeenvio = ""
        if (cursor.moveToFirst()) {
            direcciondeenvio = cursor.getString(0)
        }
        cursor.close()
        return direcciondeenvio
    }

    fun obtenerModeloauto(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT modeloauto FROM $TABLE_NAME", null)
        var modeloauto = ""
        if (cursor.moveToFirst()) {
            modeloauto = cursor.getString(0)
        }
        cursor.close()
        return modeloauto
    }

    fun obtenerPlacas(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT placas FROM $TABLE_NAME", null)
        var placas = ""
        if (cursor.moveToFirst()) {
            placas = cursor.getString(0)
        }
        cursor.close()
        return placas
    }

    fun obtenerComentarios(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT comentarios FROM $TABLE_NAME", null)
        var comentarios = ""
        if (cursor.moveToFirst()) {
            comentarios = cursor.getString(0)
        }
        cursor.close()
        return comentarios
    }




}