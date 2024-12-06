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
                val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Campos Vacíos")
                    .setMessage("Por favor, llena ambos campos antes de continuar.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setNegativeButton("OK") { dialog, _ ->
                        dialog.dismiss() // Cierra la ventana emergente
                    }
                    .create()

                alertDialog.show()
                return
            }
            for (conductor in conductores) {
                if(Email == conductor.email){
                    if(Password == conductor.password){
                        //Contraseña e Email correctos
                        // Inicia la otra actividad
                        val intent = Intent(this, PedirGrua2::class.java)
                        intent.putExtra("nombre", conductor.nombre)
                        startActivity(intent)
                        finish()
                        return
                    }else{
                        // Se equivo de contraseña pero no email
                        // Mostrar una ventana emergente si los campos están vacíos
                        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Error de Inicio de Sesión")
                            .setMessage("La contraseña que ingresaste no es correcta. ¿Quieres intentarlo de nuevo?")
                            .setIcon(android.R.drawable.ic_dialog_alert) // Ícono de alerta del sistema
                            .setPositiveButton("Reintentar") { dialog, _ ->
                                // Acción para el botón "Reintentar"
                                dialog.dismiss() // Cierra la alerta
                            }
                            .create()

                        alertDialog.show()
                        return
                    }
                }
            }
            // Se equivoco en el email, no existe
            // Mostrar una ventana emergente si los campos están vacíos
            val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Email Incorrecto")
                .setMessage("Este email no existe en nuestros registros.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNeutralButton("OK") { dialog, _ ->
                    dialog.dismiss() // Cierra la ventana emergente
                }
                .create()

            alertDialog.show()
        }


    }
