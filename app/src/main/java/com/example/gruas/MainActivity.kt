    package com.example.gruas

    import android.content.Intent
    import android.os.Bundle
    import android.util.Log
    import android.view.WindowManager
    import android.widget.AdapterView
    import android.widget.ArrayAdapter
    import android.widget.Button
    import android.widget.EditText
    import android.widget.Spinner
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import org.w3c.dom.Text
    import retrofit2.Call
    import retrofit2.Callback
    import retrofit2.Response

    class MainActivity : AppCompatActivity() {

        private lateinit var btnLogin: Button
        private lateinit var btnRegistrar: TextView
        private lateinit var EditTextEmial: EditText
        private lateinit var EditTextPassword: EditText

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // Ocultar la barra de estado
            getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            setContentView(R.layout.activity_login)

            EditTextEmial = findViewById(R.id.editTextEmail)
            EditTextPassword = findViewById(R.id.editTextPassword)
            btnRegistrar = findViewById(R.id.NuevoRegistro)
            btnLogin = findViewById(R.id.ButtonLogin)



            btnLogin.setOnClickListener {
                // Obtén los valores ingresados por el usuario
                val email = EditTextEmial.text.toString()
                val password = EditTextPassword.text.toString()

                // Llamada a la API
                RetrofitClient.instance.getClientes().enqueue(object : Callback<List<Clientes>> {
                    override fun onResponse(
                        call: Call<List<Clientes>>,
                        response: Response<List<Clientes>>
                    ) {
                        if (response.isSuccessful) {
                            val clientes = response.body()
                            clientes?.let {
                                comprobarcliente(it,email,password)
                            }
                        } else {
                            Toast.makeText(this@MainActivity, "Error en la respuesta", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<List<Clientes>>, t: Throwable) {
                        Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                        Toast.makeText(this@MainActivity, "Error 123 - : ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }


            btnRegistrar.setOnClickListener{
                val intent = Intent(this, NuevoRegistro::class.java)
                startActivity(intent)
            }

        }




        private fun comprobarcliente(conductores: List<Clientes>, Email: String, Password: String){
            //No hay nada
            if(Email.isEmpty() || Password.isEmpty()) {
                // Mostrar una ventana emergente si los campos están vacíos
                showCustomDialog("Por favor, llena ambos campos antes de continuar.")
                return
            }
            for (conductor in conductores) {
                if(Email == conductor.email){
                    if(Password == conductor.password){
                        //Contraseña e Email correctos
                        // Inicia la otra actividad
                        val intent = Intent(this, PedirGrua::class.java)
                        intent.putExtra("nombre", conductor.nombre)
                        startActivity(intent)
                        finish()
                        return
                    }else{
                        // Se equivo de contraseña pero no email
                        // Mostrar una ventana emergente si los campos están vacíos
                        showCustomDialog("La contraseña que ingresaste no es correcta. ¿Quieres intentarlo de nuevo?")
                        return
                    }
                }
            }
            // Se equivoco en el email, no existe
            // Mostrar una ventana emergente si los campos están vacíos
            showCustomDialog("Este email no existe en nuestros registros.")
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
