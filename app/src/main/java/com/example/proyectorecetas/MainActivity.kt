package com.example.proyectorecetas

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.example.proyectorecetas.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private var shouldUpdateBottomNav = true

    override fun onCreate(savedInstanceState: Bundle?) {
        applyAccessibilitySettings()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupNavigation()
        loadUserData()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Configurar AppBarConfiguration
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.searchFragment, R.id.createdRecipesFragment),
            binding.drawerLayout
        )

        // Configurar ActionBar con NavController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Configurar NavigationView
        binding.navView.setupWithNavController(navController)

        // Configurar BottomNavigationView
        setupBottomNavigation()

        // Manejar ítems especiales del drawer
        setupDrawerMenu()
    }

    private fun setupBottomNavigation() {
        // Configurar listener para BottomNavigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (!shouldUpdateBottomNav) {
                shouldUpdateBottomNav = true
                return@setOnItemSelectedListener false
            }

            when (item.itemId) {
                R.id.homeFragment -> {
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

        // Actualizar selección cuando cambia el destino
        navController.addOnDestinationChangedListener { _, destination, _ ->
            shouldUpdateBottomNav = false

            when (destination.id) {
                R.id.homeFragment -> binding.bottomNavigation.selectedItemId = R.id.homeFragment
                R.id.createRecipeFragment -> binding.bottomNavigation.selectedItemId = R.id.createRecipeFragment
                R.id.createdRecipesFragment -> binding.bottomNavigation.selectedItemId = R.id.createdRecipesFragment
            }
        }
    }

    private fun setupDrawerMenu() {
        binding.navView.menu.apply {
            findItem(R.id.nav_settings).setOnMenuItemClickListener {
                navController.navigate(R.id.nav_settings)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }

            findItem(R.id.nav_logout).setOnMenuItemClickListener {
                logoutUser()
                true
            }
        }
    }

    private fun applyAccessibilitySettings() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        // Aplicar tamaño de texto
        val textSizeStyle = when(prefs.getString("font_size", "medium")) {
            "small" -> R.style.FontSizeSmall
            "large" -> R.style.FontSizeLarge
            "xlarge" -> R.style.FontSizeXLarge
            else -> R.style.FontSizeMedium
        }
        setTheme(textSizeStyle)

        // Aplicar alto contraste si está activado
        if (prefs.getBoolean("high_contrast", false)) {
            setTheme(R.style.HighContrastTheme)
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser ?: return
        val header = binding.navView.getHeaderView(0)

    }

    private fun logoutUser() {
        auth.signOut()
        navController.navigate(R.id.loginFragment)
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}