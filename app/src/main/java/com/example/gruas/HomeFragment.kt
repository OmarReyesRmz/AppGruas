package com.example.gruas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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

        // Inicializar los Spinners
        val spinnerLocation: Spinner = view.findViewById(R.id.spinnerLocation)
        val spinnerCraneType: Spinner = view.findViewById(R.id.spinnerCraneType)

        // Inicializar los EditText
        val editTextCarModel: EditText = view.findViewById(R.id.editTextCarModel)
        val editTextLicensePlate: EditText = view.findViewById(R.id.editTextLicensePlate)
        val editTextComments: EditText = view.findViewById(R.id.editTextComments)

        // Configurar los Spinners (si tienes lógica específica para ellos)
        setupSpinners(view)

        val callCraneButton: AppCompatButton = view.findViewById(R.id.call_crane_button)
        callCraneButton.setOnClickListener {
            if (db.obtenerRealizadoPedido() == "NINGUNO") {
                // Verificar que todos los campos estén llenos
                val areFieldsFilled = spinnerLocation.selectedItem != null &&
                        spinnerCraneType.selectedItem != null &&
                        editTextCarModel.text.isNotBlank() &&
                        editTextLicensePlate.text.isNotBlank() &&
                        editTextComments.text.isNotBlank()

                if (!areFieldsFilled) {
//                    Toast.makeText(
//                        requireContext(),
//                        "Por favor, completa todos los campos",
//                        Toast.LENGTH_SHORT
//                    ).show()
                    showCustomDialog("Porfavor, llena todos los campos para poder pedir tu grua")
                } else {
                    db.actualizartipodegrua(spinnerCraneType.selectedItem.toString())
                    db.actualizarmodeloauto(editTextCarModel.text.toString())
                    db.actualizarplacas(editTextLicensePlate.text.toString())
                    db.actualizarcomentarios(editTextComments.text.toString())
                    navigateToMapFragment()
                }
            }else{
                showCustomDialog("Estas en un pedido en este momento, espera que termine")
            }
        }

        return view
    }


    private fun setupSpinners(view: View) {
        // Datos para los Spinners
        val locationOptions = listOf("Mi Ubicación")
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

    private fun showCustomDialog(message: String, onButtonClick: (() -> Unit)? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_alert, null)
        val dialog = android.app.AlertDialog.Builder(requireContext()).setView(dialogView).create()

        // Configura la animación del diálogo
        dialog.window?.apply {
            //setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL) // Posicionar arriba y centrado horizontalmente
            //attributes.y = 50 // Margen desde el borde superior (ajusta según necesites)
            setWindowAnimations(R.style.DialogAnimation) // Animaciones personalizadas
            //WindowManager.LayoutParams.MATCH_PARENT // Ancho del diálogo (puedes usar WRAP_CONTENT)
            //WindowManager.LayoutParams.WRAP_CONTENT // Alto del diálogo
            //setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            //setBackgroundDrawable(ColorDrawable(Color.parseColor("#80000000"))) // Fondo oscuro translúcido
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
}
