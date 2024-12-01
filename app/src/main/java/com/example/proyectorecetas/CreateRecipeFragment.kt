package com.example.proyectorecetas

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.example.proyectorecetas.databinding.FragmentCreateRecipeBinding
import java.io.InputStream

class CreateRecipeFragment : Fragment() {

    private var _binding: FragmentCreateRecipeBinding? = null
    private val binding get() = _binding!!
    private var selectedCategory: String = "Ensaladas"

    // Ruta de la imagen predeterminada
    private val defaultImagePath = "default_image.jpg" // Nombre del archivo en assets

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar imagen predeterminada desde assets
        loadImageFromAssets()

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

    // Función para cargar la imagen predeterminada desde assets
    private fun loadImageFromAssets() {
        try {
            val assetManager = requireContext().assets
            val inputStream: InputStream = assetManager.open(defaultImagePath)
            val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
            binding.imgRecipe.setImageBitmap(bitmap) // Asegúrate de tener un ImageView con id imgRecipe
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
            // Usamos la imagen predeterminada desde assets
            val newRecipe = Recipe(
                img = defaultImagePath, // Usamos la ruta de la imagen en assets
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
