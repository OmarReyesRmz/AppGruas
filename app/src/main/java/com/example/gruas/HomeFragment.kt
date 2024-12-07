package com.example.gruas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment() {

    private lateinit var db: DBsqlite

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ocultar la barra de estado si es necesario (requiere compatibilidad con Activity)
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        db = DBsqlite(requireContext())
        // Configurar los Spinners
        setupSpinners(view)

        val callCraneButton: AppCompatButton = view.findViewById(R.id.call_crane_button)
        callCraneButton.setOnClickListener {
            navigateToMapFragment()
        }

        return view
    }

    private fun setupSpinners(view: View) {
        // Datos para los Spinners
        val locationOptions = listOf("Mi Ubicación", "Otra Ubicación")
        val craneOptions = listOf(
            "Grua Hidráulica",
            "Grua de Plataforma",
            "Grua de Arrastre",
            "Grua Telescópica",
            "Grua de Carga"
        )

        // Referencias de los Spinners
        val locationSpinner: Spinner = view.findViewById(R.id.spinnerLocation)
        val craneSpinner: Spinner = view.findViewById(R.id.spinnerCraneType)

        // Adaptadores para los Spinners
        val locationAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            locationOptions
        )
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        locationSpinner.adapter = locationAdapter

        val craneAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            craneOptions
        )
        craneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        craneSpinner.adapter = craneAdapter

        // Manejo de selección
        locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedLocation = locationOptions[position]
                // Realiza acciones con el valor seleccionado
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No se seleccionó nada
            }
        }

        craneSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCrane = craneOptions[position]
                // Realiza acciones con el valor seleccionado
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No se seleccionó nada
            }
        }
    }

    private fun navigateToMapFragment() {
        // Obtén una referencia al BottomNavigationView
        db.actualizarrealizarpedido("REALIZANDO")
        val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        // Simula un clic en el ítem de "Map" (R.id.Map)
        bottomNavigationView?.selectedItemId = R.id.Map
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}
