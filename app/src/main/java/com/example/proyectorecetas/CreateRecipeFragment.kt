package com.example.proyectorecetas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.proyectorecetas.databinding.FragmentCreateRecipeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class CreateRecipeFragment : Fragment() {

    private var _binding: FragmentCreateRecipeBinding? = null
    private val binding get() = _binding!!
    private var selectedCategory: String = "Ensaladas"
    private val difficulties = listOf("Fácil", "Media", "Difícil")
    private var selectedDifficulty = "Media"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var isEditMode = false
    private var recipeId: String? = null

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

        setupDifficultySpinner()

        // 1. Verificar si estamos editando una receta existente
        recipeId = arguments?.getString("recipeId")
        isEditMode = recipeId != null

        // 2. Configurar UI según el modo
        if (isEditMode) {
            binding.btnSaveRecipe.text = "Actualizar Receta"
            loadRecipeData()
        } else {
            binding.btnSaveRecipe.text = "Guardar Receta"
            // Establecer valores por defecto para nueva receta
            binding.switchPublic.isChecked = true
            loadDefaultImage()
        }

        // 3. Configurar listeners
        setupListeners()
    }

    private fun setupListeners() {
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
    }

    private fun loadRecipeData() {
        recipeId?.let { id ->
            db.collection("recipes").document(id)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val recipe = document.toObject(Recipe::class.java)
                        recipe?.let {
                            // Cargar datos en el formulario
                            binding.etTitle.setText(it.title)
                            binding.etDescription.setText(it.description)
                            binding.etIngredients.setText(it.ingredients)
                            binding.etTime.setText(it.time)
                            binding.spinnerDifficulty.setSelection(difficulties.indexOf(it.difficulty))
                            binding.switchPublic.isChecked = it.isPublic

                            // Cargar imagen
                            Glide.with(requireContext())
                                .load(it.imageUrl)
                                .placeholder(R.drawable.macarrones)
                                .error(R.drawable.food)
                                .into(binding.imgRecipe)

                            // Seleccionar categoría
                            when (it.category) {
                                "Ensaladas" -> binding.radioGroup.check(R.id.radioSalads)
                                "Carnes" -> binding.radioGroup.check(R.id.radioMeats)
                                "Bebidas" -> binding.radioGroup.check(R.id.radioDrinks)
                                "Postres" -> binding.radioGroup.check(R.id.radioDesserts)
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Receta no encontrada", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al cargar receta: ${e.message}", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
        }
    }

    private fun loadDefaultImage() {
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
            Toast.makeText(requireContext(), "Debes iniciar sesión para guardar recetas", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar campos requeridos
        if (!validateInput()) {
            return
        }

        // Guardar o actualizar según el modo
        if (isEditMode) {
            updateRecipe(user.uid)
        } else {
            createNewRecipe(user.uid)
        }
    }

    private fun validateInput(): Boolean {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val ingredients = binding.etIngredients.text.toString().trim()
        val time = binding.etTime.text.toString().trim()

        var isValid = true

        if (title.isEmpty()) {
            binding.etTitle.error = "Ingresa un título"
            isValid = false
        }

        if (description.isEmpty()) {
            binding.etDescription.error = "Ingresa la descripción"
            isValid = false
        }

        if (ingredients.isEmpty()) {
            binding.etIngredients.error = "Ingresa los ingredientes"
            isValid = false
        }

        if (time.isEmpty()) {
            binding.etTime.error = "Ingresa el tiempo de preparación"
            isValid = false
        }

        return isValid
    }
    private fun setupDifficultySpinner() {
        val difficultyAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            difficulties
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerDifficulty.adapter = difficultyAdapter

        binding.spinnerDifficulty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedDifficulty = difficulties[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun createNewRecipe(userId: String) {
        val title = binding.etTitle.text.toString().trim()
        val normalizedTitle = title.lowercase().normalize()

        val recipeData = hashMapOf(
            "userId" to userId,
            "title" to binding.etTitle.text.toString().trim(),
            "searchTitle" to normalizedTitle,
            "description" to binding.etDescription.text.toString().trim(),
            "ingredients" to binding.etIngredients.text.toString().trim(),
            "time" to binding.etTime.text.toString().trim(),
            "difficulty" to selectedDifficulty,
            "category" to selectedCategory,
            "imageUrl" to defaultImageUrl,
            "timestamp" to FieldValue.serverTimestamp(),
            "isPublic" to binding.switchPublic.isChecked
        )

        db.collection("recipes")
            .add(recipeData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Receta creada exitosamente", Toast.LENGTH_SHORT).show()
                clearForm()
                // Opcional: Navegar de regreso al home o a la lista de recetas
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al crear receta: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateRecipe(userId: String) {
        recipeId?.let { id ->
            val recipeData = hashMapOf<String, Any>(
                "title" to binding.etTitle.text.toString().trim(),
                "description" to binding.etDescription.text.toString().trim(),
                "ingredients" to binding.etIngredients.text.toString().trim(),
                "time" to binding.etTime.text.toString().trim(),
                "difficulty" to selectedDifficulty,
                "category" to selectedCategory,
                "isPublic" to binding.switchPublic.isChecked,
                "lastUpdated" to FieldValue.serverTimestamp()
            )

            db.collection("recipes")
                .document(id)
                .update(recipeData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Receta actualizada exitosamente", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack() // Volver a la receta
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al actualizar receta: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } ?: run {
            Toast.makeText(requireContext(), "Error: ID de receta no encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    // Funciones auxiliares
    private fun String.normalize(): String {
        return this
            .lowercase()
            .replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u")
            .replace("ñ", "n")
    }

    private fun clearForm() {
        binding.etTitle.text?.clear()
        binding.etDescription.text?.clear()
        binding.etIngredients.text?.clear()
        binding.etTime.text?.clear()
        binding.radioGroup.check(R.id.radioSalads)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}