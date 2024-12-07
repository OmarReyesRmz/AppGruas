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
        private lateinit var db: DBsqlite

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // Ocultar la barra de estado
            getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            setContentView(R.layout.activity_login)
            db = DBsqlite(this)

            if(!db.datosExistentes()){
                db.guardarDatos("NO","NIGUNO","NIGUNO","NIGUNO",
                    "NIGUNO","NIGUNO","NIGUNO",0f,0f,"NO")
            }else if(db.obtenerLogeado() == "SI"){
                val intent = Intent(this, PedirGrua::class.java)
                startActivity(intent)
                finish()
            }
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




        private fun comprobarcliente(clientes: List<Clientes>, Email: String, Password: String){
            //No hay nada
            if(Email.isEmpty() || Password.isEmpty()) {
                // Mostrar una ventana emergente si los campos están vacíos
                showCustomDialog("Por favor, llena ambos campos antes de continuar.")
                return
            }
            for (cliente in clientes) {
                if(Email == cliente.email){
                    if(Password == cliente.password){
                        //Contraseña e Email correctos
                        // Inicia la otra actividad
                        val intent = Intent(this, PedirGrua::class.java)
                        db.actualizarnombre(cliente.nombre)
                        db.actualizarcorreo(cliente.email)
                        db.actualizarapellidos(cliente.apellido)
                        db.actualizardireccion(cliente.direccion)
                        db.actualizartelefono(cliente.telefono)
                        db.actualizarid(cliente.id)
                        db.actualizarlogeado("SI")
                        db.actualizartipo_usuario("cliente")
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

            // Se equivoco en el email, no existe o es un conductores
            RetrofitClient.instance.getConductores().enqueue(object : Callback<List<Conductores>> {
                override fun onResponse(
                    call: Call<List<Conductores>>,
                    response: Response<List<Conductores>>
                ) {
                    if (response.isSuccessful) {
                        val conductores = response.body()
                        conductores?.let {
                            comprobarconductor(it,Email, Password)
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "Error en la respuesta", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<Conductores>>, t: Throwable) {
                    Log.e("API_ERROR", "Error al conectar con la API: ${t.message}")
                    Toast.makeText(this@MainActivity, "Error 123 - : ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }


        private fun comprobarconductor(conductores: List<Conductores>,Email: String, Password: String){
            for (conductor in conductores) {
                if(Email == conductor.email){
                    if(Password == conductor.password){
                        //Contraseña e Email correctos
                        // Inicia la otra actividad
                        val intent = Intent(this, PedirGrua::class.java)
                        db.actualizarnombre(conductor.nombre)
                        db.actualizarcorreo(conductor.email)
                        db.actualizarapellidos(conductor.apellido)
                        db.actualizardireccion(conductor.direccion)
                        db.actualizartelefono(conductor.telefono)
                        db.actualizarlogeado("SI")
                        db.actualizartipo_usuario("conductor")
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
