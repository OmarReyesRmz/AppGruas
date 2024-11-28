package com.example.gruas

import android.os.Bundle
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class PedirGrua : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ocultar la barra de estado
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


        setContentView(R.layout.activity_main)

        // Datos para los Spinners
        val locationOptions = listOf("Mi Ubicación", "Otra Ubicación")
        val craneOptions = listOf("Grua Hidráulica", "Grua de Plataforma", "Grua de Arrastre", "Grua Telescópica", "Grua de Carga")

        // Referencias de los Spinners
        val locationSpinner: Spinner = findViewById(R.id.spinnerLocation)
        val craneSpinner: Spinner = findViewById(R.id.spinnerCraneType)

        // Adaptadores para los Spinners
        val locationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locationOptions)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        locationSpinner.adapter = locationAdapter

        val craneAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, craneOptions)
        craneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        craneSpinner.adapter = craneAdapter

        // Manejo de selección
        locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedLocation = locationOptions[position]
                // Realiza acciones con el valor seleccionado
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No se seleccionó nada
            }
        }

        craneSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedCrane = craneOptions[position]
                // Realiza acciones con el valor seleccionado
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No se seleccionó nada
            }
        }
    }
}
