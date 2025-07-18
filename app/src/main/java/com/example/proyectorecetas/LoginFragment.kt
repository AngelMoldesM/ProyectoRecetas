package com.example.proyectorecetas

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.proyectorecetas.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        checkCurrentUser()
    }

    private fun setupUI() {
        binding.apply {
            loginButton.setOnClickListener { handleEmailLogin() }
            registerButton.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
            googleSignInButton.setOnClickListener { launchGoogleSignIn() }
        }
    }

    private fun checkCurrentUser() {
        val currentUser = SupabaseManager.client.auth.currentUserOrNull()
        if (currentUser != null) {
            navigateToMainApp()
        }
    }

    private fun handleEmailLogin() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (validateInput(email, password)) {
            showLoading(true)
            lifecycleScope.launch {
                try {
                    // Iniciar sesión
                    SupabaseManager.client.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }

                    // Obtener usuario autenticado
                    val user = SupabaseManager.client.auth.currentUserOrNull()
                    user?.let {
                        // Verificar si el perfil existe
                        checkProfileExists(userId = it.id)
                    }

                    navigateToMainApp()
                } catch (e: Exception) {
                    showSnackbar("Error: ${e.message}")
                } finally {
                    showLoading(false)
                }
            }
        }
    }

    private fun checkProfileExists(userId: String) {
        lifecycleScope.launch {
            try {
                val profile = SupabaseManager.client.postgrest["profiles"]
                    .select {
                        filter { eq("id", userId) }
                        limit(1)
                    }
                    .decodeSingleOrNull<Profile>()
                if (profile == null) {
                    // Si no existe perfil, navegar a pantalla de completar perfil
                    navigateToProfileSetup()
                }
            } catch (e: Exception) {
                Log.e("Login", "Error checking profile", e)
            }
        }
    }

    private fun launchGoogleSignIn() {
        // TODO: Implementar lógica de Google Sign-In
        // Y luego verificar el perfil de la misma manera
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                showSnackbar("Enter your email")
                false
            }
            password.isEmpty() -> {
                showSnackbar("Enter your password")
                false
            }
            password.length < 6 -> {
                showSnackbar("The password must be at least 6 characters long")
                false
            }
            else -> true
        }
    }

    private fun navigateToMainApp() {
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
    }

    private fun navigateToProfileSetup() {
        findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}