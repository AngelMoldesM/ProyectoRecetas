package com.example.proyectorecetas

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.example.proyectorecetas.databinding.FragmentCreateRecipeBinding

class CreateRecipeFragment : Fragment() {

    private var _binding: FragmentCreateRecipeBinding? = null
    private val binding get() = _binding!!
    private var selectedCategory: String = "Ensaladas"

    // Recurso de la imagen predeterminada en drawable
    private val defaultImageResId = R.drawable.food

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar imagen predeterminada desde drawable
        loadImageFromDrawable()

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
    //Cambiar por URL que funcione!!!!
    // Función para cargar la imagen predeterminada desde drawable
    private fun loadImageFromDrawable() {
        try {
            val drawable: Drawable? = ContextCompat.getDrawable(requireContext(), defaultImageResId)
            binding.imgRecipe.setImageDrawable(drawable) // Asegúrate de tener un ImageView con id imgRecipe
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Función para guardar la receta en la base de datos
    private fun saveRecipe() {
        val title = binding.etTitle.text.toString()
        val description = binding.etDescription.text.toString()
        val ingredients = binding.etIngredients.text.toString()

        if (title.isNotBlank() && description.isNotBlank() && ingredients.isNotBlank()) {
            // Usamos la imagen de drawable al guardar la receta
            val newRecipe = Recipe(
                img = defaultImageResId.toString(), // Usamos el id del drawable
                tittle = title,
                des = description,
                ing = ingredients,
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
