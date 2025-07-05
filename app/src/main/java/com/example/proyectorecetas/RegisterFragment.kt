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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

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

        if (validateInput(username, email, password, confirmPassword)) {
            showLoading(true)
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        saveUserToFirestore(username, email)
                    } else {
                        showLoading(false)
                        showSnackbar("Error: ${task.exception?.message}")
                    }
                }
        }
    }

    private fun saveUserToFirestore(username: String, email: String) {
        val user = auth.currentUser
        user?.let {
            val userData = hashMapOf(
                "userId" to user.uid,
                "username" to username,
                "email" to email,
                "createdAt" to FieldValue.serverTimestamp()
            )

            db.collection("users")
                .document(user.uid)
                .set(userData)
                .addOnSuccessListener {
                    Log.d("RegisterFragment", "Usuario guardado en Firestore")
                    navigateToHome()
                }
                .addOnFailureListener { e ->
                    Log.e("RegisterFragment", "Error al guardar usuario", e)
                    showSnackbar("Error al crear cuenta: ${e.message}")
                    showLoading(false)
                }
        } ?: run {
            showSnackbar("Error: Usuario no autenticado")
            showLoading(false)
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