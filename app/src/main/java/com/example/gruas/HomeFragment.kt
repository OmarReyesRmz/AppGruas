package com.example.gruas

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        // Referencias de los Spinners
        val locationSpinner: Spinner = view.findViewById(R.id.spinnerLocation)
        val craneSpinner: Spinner = view.findViewById(R.id.spinnerCraneType)

        // 1. Spinner de Ubicación (se mantiene igual)
        val locationOptions = listOf("Mi Ubicación")
        val locationAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            locationOptions
        )
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        locationSpinner.adapter = locationAdapter

        // 2. Spinner de Grúas (nueva implementación con API)
        setupCraneSpinner(craneSpinner)

        // Manejo de selección (se mantiene similar pero adaptado)
        locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedLocation = locationOptions[position]
                // Acciones con la ubicación seleccionada
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupCraneSpinner(spinner: Spinner) {
        // Mostrar datos estáticos iniciales (como fallback rápido)
        val fallbackOptions = listOf(
            "Grua Hidráulica",
            "Grua de Plataforma",
            "Grua de Arrastre",
            "Grua Telescópica",
            "Grua de Carga"
        )

        val tempAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            fallbackOptions
        )
        tempAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = tempAdapter

        // Cargar datos reales desde API
        RetrofitClient.instance.getGruas().enqueue(object : Callback<List<Gruas>> {
            override fun onResponse(call: Call<List<Gruas>>, response: Response<List<Gruas>>) {
                if (response.isSuccessful) {
                    val gruas = response.body() ?: emptyList()

                    // Filtrar tipos de grúas únicos
                    val tiposUnicos = gruas
                        .map { it.tipo_grua }
                        .distinct() // Esto elimina duplicados
                        .sorted() // Opcional: orden alfabético

                    // Si encontramos tipos únicos, actualizamos el Spinner
                    if (tiposUnicos.isNotEmpty()) {
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            tiposUnicos
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = adapter
                    }

                    // Si no hay grúas, mantener las opciones por defecto
                    if (tiposUnicos.isNotEmpty()) {
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            tiposUnicos
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = adapter
                    }

                    // Manejar selección con datos reales
                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            val selectedGrua = gruas.getOrNull(position)
                            selectedGrua?.let {
                                // Aquí tienes acceso a todos los datos de la grúa seleccionada
                                Log.d("GruaSeleccionada", "ID: ${it.id_grua}, Tipo: ${it.tipo_grua}")
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                }
            }

            override fun onFailure(call: Call<List<Gruas>>, t: Throwable) {
                Log.e("API Error", "No se pudieron cargar las grúas", t)
                // Mantener las opciones por defecto que ya estaban configuradas
            }
        })
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
