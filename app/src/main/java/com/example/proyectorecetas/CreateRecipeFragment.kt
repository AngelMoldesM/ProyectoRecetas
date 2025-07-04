package com.example.proyectorecetas

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.proyectorecetas.databinding.FragmentCreateRecipeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class CreateRecipeFragment : Fragment() {

    private var _binding: FragmentCreateRecipeBinding? = null
    private val binding get() = _binding!!
    private var selectedCategory: String = "Ensaladas"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val defaultImageUrl = "https://hips.hearstapps.com/hmg-prod/images/elote-secondary-6464fa8a21969.jpg?crop=1xw:1xh;center,top&resize=980:*"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadImageFromUrl()

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedCategory = when (checkedId) {
                R.id.radioSalads -> "Ensaladas"
                R.id.radioMeats -> "Carnes"
                R.id.radioDrinks -> "Bebidas"
                R.id.radioDesserts -> "Postres"
                else -> "Ensaladas"
            }
        }

        binding.btnSaveRecipe.setOnClickListener {
            saveRecipeToFirestore()
        }

        // Añadir Switch para visibilidad
        binding.switchPublic.isChecked = true
    }

    private fun loadImageFromUrl() {
        try {
            Glide.with(requireContext())
                .load(defaultImageUrl)
                .placeholder(R.drawable.macarrones)
                .error(R.drawable.food)
                .into(binding.imgRecipe)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveRecipeToFirestore() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión para crear recetas", Toast.LENGTH_SHORT).show()
            return
        }

        val title = binding.etTitle.text.toString()
        val description = binding.etDescription.text.toString()
        val ingredients = binding.etIngredients.text.toString()
        val time = binding.etTime.text.toString()

        if (title.isBlank() || description.isBlank() || ingredients.isBlank() || time.isBlank()) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val isPublic = binding.switchPublic.isChecked

        val recipeData = hashMapOf(
            "userId" to user.uid,
            "title" to title,
            "description" to description,
            "ingredients" to ingredients,
            "time" to time,
            "category" to selectedCategory,
            "imageUrl" to defaultImageUrl,
            "timestamp" to FieldValue.serverTimestamp(),
            "isPublic" to isPublic
        )

        db.collection("recipes")
            .add(recipeData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Receta creada exitosamente", Toast.LENGTH_SHORT).show()
                clearForm()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al crear receta: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun clearForm() {
        binding.etTitle.text.clear()
        binding.etDescription.text.clear()
        binding.etIngredients.text.clear()
        binding.etTime.text.clear()
        binding.radioGroup.check(R.id.radioSalads)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}