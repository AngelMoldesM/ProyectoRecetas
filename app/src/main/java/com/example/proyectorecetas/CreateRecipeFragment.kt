package com.example.proyectorecetas


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.bumptech.glide.Glide
import com.example.proyectorecetas.databinding.FragmentCreateRecipeBinding

class CreateRecipeFragment : Fragment() {

    private var _binding: FragmentCreateRecipeBinding? = null
    private val binding get() = _binding!!
    private var selectedCategory: String = "Ensaladas"

    // URL de la imagen predeterminada
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

        // Configurar RadioButtonGroup para la categoría
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedCategory = when (checkedId) {
                R.id.radioSalads -> "Ensaladas"
                R.id.radioMeats -> "Carnes"
                R.id.radioDrinks -> "Bebidas"
                R.id.radioDesserts -> "Postres"
                else -> "Ensaladas"
            }
        }

        // Botón para guardar receta
        binding.btnSaveRecipe.setOnClickListener {
            saveRecipe()
        }
    }

    // Función para cargar la imagen desde una URL usando Glide
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

    // Función para guardar la receta en la base de datos
    private fun saveRecipe() {
        val title = binding.etTitle.text.toString()
        val description = binding.etDescription.text.toString()
        val ingredients = binding.etIngredients.text.toString()
        val time = binding.etTime.text.toString()

        if (title.isNotBlank() && description.isNotBlank() && ingredients.isNotBlank() && time.isNotBlank()) {

            val combinedIngredients = "$time\n$ingredients"

            val newRecipe = Recipe(
                img = defaultImageUrl,
                tittle = title,
                des = description,
                ing = combinedIngredients,
                category = selectedCategory
            )

            // Insertar receta en la base de datos
            val db = Room.databaseBuilder(
                requireContext(),
                AppDatabase::class.java,
                "recipe.db"
            ).allowMainThreadQueries().build()

            db.getDao().insertRecipe(newRecipe)

            Toast.makeText(requireContext(), "Receta guardada", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
