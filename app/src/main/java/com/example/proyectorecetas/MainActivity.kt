package com.example.proyectorecetas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.proyectorecetas.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener el NavController desde el NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Configurar el BottomNavigationView con el NavController
        val bottomNavigationView = binding.bottomNavigation
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        // Configurar el listener para los cambios de destino hacia Home
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.HomeFragment -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }
                R.id.createRecipeFragment -> {
                    navController.navigate(R.id.createRecipeFragment)
                    true
                }
                R.id.createdRecipesFragment -> {
                    navController.navigate(R.id.createdRecipesFragment)
                    true
                }
                else -> false
            }
        }

    }
}
