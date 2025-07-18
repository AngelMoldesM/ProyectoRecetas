package com.example.proyectorecetas

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.proyectorecetas.databinding.FragmentCreateRecipeBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class CreateRecipeFragment : Fragment() {

    private var _binding: FragmentCreateRecipeBinding? = null
    private val binding get() = _binding!!
    private var selectedCategory: String = "Ensaladas"
    private val difficulties = listOf("Fácil", "Media", "Difícil")
    private var selectedDifficulty = "Media"
    private var isEditMode = false
    private var recipeId: String? = null

    private val defaultImageUrl =
        "https://hips.hearstapps.com/hmg-prod/images/elote-secondary-6464fa8a21969.jpg?crop=1xw:1xh;center,top&resize=980:*"

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

        recipeId = arguments?.getString("recipeId")
        isEditMode = recipeId != null

        if (isEditMode) {
            binding.btnSaveRecipe.text = getString(R.string.update_recipe)
            loadRecipeData()
        } else {
            binding.btnSaveRecipe.text = getString(R.string.save_recipe)
            binding.switchPublic.isChecked = true
            loadDefaultImage()
        }

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
            if (!validateInput()) return@setOnClickListener

            val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
            if (userId == null) {
                Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEditMode) {
                updateRecipe(userId)
            } else {
                createNewRecipe(userId)
            }
        }
    }

    private fun loadRecipeData() {
        lifecycleScope.launch {
            try {
                val response = SupabaseManager.client.postgrest["recipes"]
                    .select { filter { eq("id", recipeId!!) } }
                    .decodeSingle<Recipe>()

                response.let {
                    binding.etTitle.setText(it.title)
                    binding.etDescription.setText(it.description)
                    binding.etIngredients.setText(it.ingredients)
                    binding.etTime.setText(it.time)
                    binding.spinnerDifficulty.setSelection(difficulties.indexOf(it.difficulty))
                    binding.switchPublic.isChecked = it.is_public

                    Glide.with(requireContext())
                        .load(it.image_path)
                        .placeholder(R.drawable.macarrones)
                        .error(R.drawable.food)
                        .into(binding.imgRecipe)

                    when (it.category) {
                        "Ensaladas" -> binding.radioGroup.check(R.id.radioSalads)
                        "Carnes" -> binding.radioGroup.check(R.id.radioMeats)
                        "Bebidas" -> binding.radioGroup.check(R.id.radioDrinks)
                        "Postres" -> binding.radioGroup.check(R.id.radioDesserts)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading recipe: ${e.message}", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun createNewRecipe(userId: String) {
        lifecycleScope.launch {
            try {
                val recipe = Recipe(
                    user_id = userId,
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    ingredients = binding.etIngredients.text.toString(),
                    time = binding.etTime.text.toString(),
                    difficulty = selectedDifficulty,
                    category = selectedCategory,
                    image_path = defaultImageUrl,
                    is_public = binding.switchPublic.isChecked,
                    created_at = Clock.System.now().toString()
                )

                SupabaseManager.client.postgrest["recipes"].insert(recipe)
                Toast.makeText(requireContext(), "Recipe created", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error creating recipe: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateRecipe(userId: String) {
        lifecycleScope.launch {
            try {
                SupabaseManager.client.postgrest["recipes"].update(
                    {
                        set("title", binding.etTitle.text.toString().trim())
                        set("description", binding.etDescription.text.toString().trim())
                        set("ingredients", binding.etIngredients.text.toString().trim())
                        set("time", binding.etTime.text.toString().trim())
                        set("difficulty", selectedDifficulty)
                        set("category", selectedCategory)
                        set("is_public", binding.switchPublic.isChecked)
                    }
                ) {
                    filter {
                        eq("id", recipeId!!)
                        eq("user_id", userId)
                    }
                }
                Toast.makeText(requireContext(), "Recipe updated", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error updating recipe: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupDifficultySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            difficulties
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerDifficulty.adapter = adapter
        binding.spinnerDifficulty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                selectedDifficulty = difficulties[pos]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true
        if (binding.etTitle.text.isNullOrEmpty()) {
            binding.etTitle.error = "Enter a title"
            isValid = false
        }
        if (binding.etDescription.text.isNullOrEmpty()) {
            binding.etDescription.error = "Enter a description"
            isValid = false
        }
        if (binding.etIngredients.text.isNullOrEmpty()) {
            binding.etIngredients.error = "Enter ingredients"
            isValid = false
        }
        if (binding.etTime.text.isNullOrEmpty()) {
            binding.etTime.error = "Enter time"
            isValid = false
        }
        return isValid
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
