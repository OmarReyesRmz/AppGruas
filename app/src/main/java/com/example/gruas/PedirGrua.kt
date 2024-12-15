package com.example.gruas

import SettingsFragment
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gruas.databinding.ActivityFragmentsBinding

class PedirGrua : AppCompatActivity() {

    private lateinit var binding: ActivityFragmentsBinding
    private lateinit var db: DBsqlite
    private var esCliente: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilita el modo de pantalla completa
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        // Inicializa la base de datos
        db = DBsqlite(this)

        // Determina el tipo de usuario
        esCliente = db.obtenerTipoUsuario() == "cliente" // Suponiendo que "cliente" es el valor esperado

        // Inicializa el binding
        binding = ActivityFragmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ajusta el menú del BottomNavigationView según el tipo de usuario
        if (!esCliente) {
            binding.bottomNavigationView.menu.findItem(R.id.Home).isVisible = false
        }

        // Configura el BottomNavigationView
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            if (!esCliente && item.itemId == R.id.Home) {
                // Ignora la selección de Home si es un conductor
                false
            } else {
                when (item.itemId) {
                    R.id.Home -> {
                        loadFragment(HomeFragment())
                        true
                    }
                    R.id.Map -> {
                        loadFragment(MapFragment())
                        true
                    }
                    R.id.Profile -> {
                        loadFragment(ProfileFragment())
                        true
                    }
                    R.id.Settings -> {
                        loadFragment(SettingsFragment())
                        true
                    }

                    else -> false
                }
            }
        }

        // Carga el fragmento inicial
        if (savedInstanceState == null) {
            if (esCliente) {
                loadFragment(HomeFragment())
            } else {
                loadFragment(MapFragment())
            }
        }
    }

    // Método para reemplazar fragmentos en el FrameLayout
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}
