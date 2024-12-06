package com.example.gruas

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gruas.databinding.ActivityFragmentsBinding

class PedirGrua : AppCompatActivity() {

    private lateinit var binding: ActivityFragmentsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilita el modo de pantalla completa
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        // Inicializa el binding
        binding = ActivityFragmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura el BottomNavigationView
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Home -> {
                    loadFragment(HomeFragment())
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

        // Carga el fragmento inicial (HomeFragment)
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    // Método para reemplazar fragmentos en el FrameLayout
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}
