package com.example.gruas

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var btnRegistrar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ocultar la barra de estado
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_login)

        btnRegistrar = findViewById(R.id.NuevoRegistro)
        btnLogin = findViewById(R.id.ButtonLogin)

        btnLogin.setOnClickListener {
            // Al hacer clic en el botón, iniciar la otra actividad
            val intent = Intent(this, PedirGrua::class.java)
            startActivity(intent)
        }

        btnRegistrar.setOnClickListener{
            val intent = Intent(this, NuevoRegistro::class.java)
            startActivity(intent)
        }

    }
}
