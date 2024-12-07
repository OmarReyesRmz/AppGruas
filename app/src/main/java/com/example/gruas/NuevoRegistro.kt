package com.example.gruas

import android.content.Intent
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.WindowInsetsAnimation
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Callback
import okhttp3.Response
import okhttp3.ResponseBody


class NuevoRegistro: AppCompatActivity() {

    private lateinit var btnlogin: TextView
    private lateinit var btnregiser: Button
    private lateinit var EditTextNombre: EditText
    private lateinit var EditTextPrimerApellido: EditText
    private lateinit var EditTextSegundoApellido: EditText
    private lateinit var EditTextEmail: EditText
    private lateinit var EditTextPassword1: EditText
    private lateinit var EditTextPassword2: EditText
    private lateinit var EditTextPhone: EditText
    private lateinit var EditTextDireccion: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ocultar la barra de estado
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_register)

        btnregiser = findViewById(R.id.ButtonRegister)
        btnlogin = findViewById(R.id.VolverLogin)
        EditTextNombre = findViewById(R.id.editTextName)
        EditTextPrimerApellido = findViewById(R.id.editTextFirstLastName)
        EditTextSegundoApellido = findViewById(R.id.editTextSecondLastName)
        EditTextEmail = findViewById(R.id.editTextEmail)
        EditTextPhone = findViewById(R.id.editTextPhoneNumber)
        EditTextDireccion = findViewById(R.id.editTextDireccion)
        EditTextPassword1 = findViewById(R.id.editTextPassword)
        EditTextPassword2 = findViewById(R.id.editTextPassword2)
        val intents = Intent(this, MainActivity::class.java)

        btnregiser.setOnClickListener {
            val name = EditTextNombre.text.toString()
            val lastname1 = EditTextPrimerApellido.text.toString()
            val lastname2 = EditTextSegundoApellido.text.toString()
            val email = EditTextEmail.text.toString()
            val phone = EditTextPhone.text.toString()
            val direccion = EditTextDireccion.text.toString()
            val password1 = EditTextPassword1.text.toString()
            val password2 = EditTextPassword2.text.toString()
            val fullName = "$lastname1 $lastname2"

            if(name.isNotEmpty() && lastname1.isNotEmpty() && lastname2.isNotEmpty()
                && email.isNotEmpty() && phone.isNotEmpty() && direccion.isNotEmpty() &&
                password1.isNotEmpty() && password2.isNotEmpty()){
                if (password1 == password2) {  // Asegúrate de que las contraseñas coinciden
                    val ubicacion = Ubicacion(0.0,0.0,false,false)
                    val nuevoCliente = Clientes(1, name, fullName, direccion, phone, email, password1,ubicacion)

                    // Llamada a la API para registrar al cliente
                    RetrofitClient.instance.registrarCliente(nuevoCliente).enqueue(object :
                        retrofit2.Callback<Clientes> {  // Cambia el tipo de respuesta al esperado por la API
                        override fun onResponse(call: retrofit2.Call<Clientes>, response: retrofit2.Response<Clientes>) {
                            if (response.isSuccessful) {
                                // Procesar la respuesta exitosa
                                val usuarioRegistrado = response.body()
                                usuarioRegistrado?.let {
                                    Log.d("API_SUCCESS", "Usuario registrado: ${it}")
                                    //Toast.makeText(this@NuevoRegistro, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                    showCustomDialog("Usuario registrado con éxito.") {
                                        startActivity(intents)
                                        finish()
                                    }
                                    // Si necesitas comprobar algo después, hazlo aquí
                                    // comprobarcliente(it, email, password1)
                                }
                            } else {
                                Log.e("API_ERROR", "Error en la respuesta: ${response.code()}")
                                Toast.makeText(this@NuevoRegistro, "Error en el registro: ${response.message()}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<Clientes>, t: Throwable) {
                            // Manejar errores de conexión o excepciones
                            Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                            Toast.makeText(this@NuevoRegistro, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    // Manejar el caso en el que las contraseñas no coincidan
                    showCustomDialog("Las contraseñas no coinciden.")
                    //Toast.makeText(this@NuevoRegistro, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                }
            }else{
                showCustomDialog("Por favor, llena todos los campos.")
            }
        }


        btnlogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun showCustomDialog(message: String, onButtonClick: (() -> Unit)? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_alert, null)
        val dialog = android.app.AlertDialog.Builder(this).setView(dialogView).create()

        // Configura la animación del diálogo
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation

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
            dialogButton.isEnabled = false
        }

        dialog.show()
    }
}

