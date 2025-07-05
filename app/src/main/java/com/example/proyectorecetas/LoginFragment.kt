package com.example.proyectorecetas

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyectorecetas.databinding.FragmentLoginBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        onSignInResult(result)
    }

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
                // Navegar al fragmento de registro
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
            googleSignInButton.setOnClickListener { launchGoogleSignIn() }
        }
    }

    private fun checkCurrentUser() {
        if (auth.currentUser != null) {
            navigateToMainApp()
        }
    }

    private fun handleEmailLogin() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (validateInput(email, password)) {
            showLoading(true)
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    showLoading(false)
                    if (task.isSuccessful) {
                        navigateToMainApp()
                    } else {
                        showSnackbar("Error: ${task.exception?.message}")
                    }
                }
        }
    }

    private fun launchGoogleSignIn() {
        val providers = listOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            val user = auth.currentUser
            user?.let {
                // Para logins con Google, verificar si el usuario ya existe
                checkUserExistsAndSave(it.uid, it.displayName ?: "Usuario", it.email ?: "")
            }
        } else {
            val errorMessage = result.idpResponse?.error?.message ?: "Error desconocido"
            showSnackbar("Error en autenticación: $errorMessage")
        }
    }

    private fun checkUserExistsAndSave(userId: String, username: String, email: String) {
        val userDocRef = db.collection("users").document(userId)

        userDocRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && !document.exists()) {
                    // El usuario no existe, crear nuevo documento
                    saveUserToFirestore(userId, username, email)
                }
                navigateToMainApp()
            } else {
                Log.e("LoginFragment", "Error al verificar usuario", task.exception)
                navigateToMainApp()
            }
        }
    }

    private fun saveUserToFirestore(userId: String, username: String, email: String) {
        val userData = hashMapOf(
            "userId" to userId,
            "username" to username,
            "email" to email,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("users")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                Log.d("LoginFragment", "Usuario guardado en Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("LoginFragment", "Error al guardar usuario", e)
            }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
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
            else -> true
        }
    }

    private fun navigateToMainApp() {
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
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

    companion object {
        const val RESULT_OK = -1
    }
}