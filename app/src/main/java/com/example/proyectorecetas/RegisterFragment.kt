package com.example.proyectorecetas

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.proyectorecetas.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val supabase: SupabaseClient
        get() = SupabaseManager.client

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerButton.setOnClickListener {
            registerUser()
        }

        binding.loginLink.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun registerUser() {
        val username = binding.usernameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

        if (!validateInput(username, email, password, confirmPassword)) return

        showLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }

                val currentUser = supabase.auth.currentUserOrNull()

                if (currentUser != null) {
                    saveUserToSupabase(currentUser.id, username, email)
                } else {
                    withContext(Dispatchers.Main) {
                        showSnackbar("Error al registrar usuario: ID nulo")
                        showLoading(false)
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showSnackbar("Error: ${e.message}")
                    showLoading(false)
                }
            }
        }
    }

    private suspend fun saveUserToSupabase(userId: String, username: String, email: String) {
        try {
            // Usa la clase Profile directamente
            val profile = Profile(
                id = userId,
                username = username,
                email = email,
                created_at = Clock.System.now().toString()
            )

            supabase.from("profiles").insert(profile)

            withContext(Dispatchers.Main) {
                navigateToHome()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.e("RegisterFragment", "Error al guardar usuario", e)
                showSnackbar("Error al crear cuenta: ${e.message}")
                showLoading(false)
            }
        }
    }
    private fun validateInput(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            username.isEmpty() -> {
                showSnackbar("Ingresa un nombre de usuario")
                false
            }
            username.length < 3 -> {
                showSnackbar("El nombre debe tener al menos 3 caracteres")
                false
            }
            email.isEmpty() -> {
                showSnackbar("Ingresa tu email")
                false
            }
            password.isEmpty() -> {
                showSnackbar("Ingresa tu contraseña")
                false
            }
            password.length < 6 -> {
                showSnackbar("La contraseña debe tener al menos 6 caracteres")
                false
            }
            confirmPassword != password -> {
                showSnackbar("Las contraseñas no coinciden")
                false
            }
            else -> true
        }
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
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
