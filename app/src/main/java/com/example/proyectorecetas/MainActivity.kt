package com.example.proyectorecetas

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.example.proyectorecetas.databinding.ActivityMainBinding
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var shouldUpdateBottomNav = true
    private val SharedViewModel: SharedViewModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        applyAccessibilitySettings()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        SharedViewModel.drawerState.onEach { isOpen ->
            if (isOpen) {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            } else {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        }.launchIn(lifecycleScope)


        // Comprobar si hay usuario activo al iniciar
        checkUserSession()
    }

    private fun checkUserSession() {
        lifecycleScope.launch {
            val user = SupabaseManager.client.auth.currentUserOrNull()
            if (user == null) {
                // No hay usuario logueado, navegar a login
                navController.navigate(R.id.loginFragment)
            }
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.searchFragment, R.id.createdRecipesFragment),
            binding.drawerLayout
        )

        binding.navView.setupWithNavController(navController)
        setupBottomNavigation()
        setupDrawerMenu()
    }

    private fun setupBottomNavigation() {
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
            findItem(R.id.nav_home).setOnMenuItemClickListener {
                navController.navigate(R.id.homeFragment)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            findItem(R.id.nav_search).setOnMenuItemClickListener {
                navController.navigate(R.id.searchFragment)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            findItem(R.id.nav_favorites).setOnMenuItemClickListener {
                navController.navigate(R.id.favoritesFragment)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
        }
    }

    private fun applyAccessibilitySettings() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val textSizeStyle = when (prefs.getString("font_size", "medium")) {
            "small" -> R.style.FontSizeSmall
            "large" -> R.style.FontSizeLarge
            "xlarge" -> R.style.FontSizeXLarge
            else -> R.style.FontSizeMedium
        }
        setTheme(textSizeStyle)

        if (prefs.getBoolean("high_contrast", false)) {
            setTheme(R.style.HighContrastTheme)
        }
    }

    private fun logoutUser() {
        lifecycleScope.launch {
            try {
                SupabaseManager.client.auth.signOut()
                navController.navigate(R.id.loginFragment)
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error al cerrar sesión", e)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

    @Deprecated("Use OnBackPressedDispatcher instead")
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
