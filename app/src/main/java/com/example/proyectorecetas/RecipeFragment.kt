package com.example.proyectorecetas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.proyectorecetas.databinding.FragmentRecetaBinding
import com.google.firebase.auth.FirebaseAuth

class RecipeFragment : Fragment() {

    private var _binding: FragmentRecetaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecetaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recipeId = arguments?.getString("id") ?: ""
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val recipeUserId = arguments?.getString("userId") ?: ""

        binding.editButton.visibility = if (currentUserId == recipeUserId) View.VISIBLE else View.GONE

        // Recibir parámetros
        val args = arguments?.let {
            mapOf(
                "id" to it.getString("id"),
                "imageUrl" to it.getString("img"),
                "title" to it.getString("tittle"),
                "description" to it.getString("des"),
                "ingredients" to it.getString("ing"),
                "time" to it.getString("time")
            )
        }

        if (currentUserId == recipeUserId) {
            binding.editButton.visibility = View.VISIBLE
        } else {
            binding.editButton.visibility = View.GONE
        }

        binding.editButton.setOnClickListener {
            val recipeId = arguments?.getString("id") ?: ""
            navigateToEditRecipe(recipeId)
        }



        // Configurar UI
        Glide.with(requireContext())
            .load(args?.get("imageUrl"))
            .into(binding.itemImg)

        binding.tittle.text = args?.get("title").toString()
        binding.time.text = "⏱ ${args?.get("time")}"
        binding.stepData.text = args?.get("description").toString()

        // Formatear ingredientes
        val ingredientsText = args?.get("ingredients").toString()
            .replace("\\n", "\n")
            .split("\n")
            .filter { it.isNotBlank() }
            .joinToString("\n") { "🟢 $it" }
        binding.ingData.text = ingredientsText

        // Configurar botones
        binding.step.setOnClickListener {
            binding.stepScroll.visibility = View.VISIBLE
            binding.ingScroll.visibility = View.GONE
        }

        binding.ing.setOnClickListener {
            binding.ingScroll.visibility = View.VISIBLE
            binding.stepScroll.visibility = View.GONE
        }

        // Botón de regreso
        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

/*
    private fun navigateToRecipeDetail(recipe: Recipe) {
        val args = Bundle().apply {
            putString("id", recipe.id)
            putString("userId", recipe.userId) // ¡IMPORTANTE!
            // ... otros campos ...
        }
        findNavController().navigate(R.id.action_recipeFragment, args)
    }
*/

    private fun navigateToEditRecipe(recipeId: String) {
        val args = Bundle().apply {
            putString("recipeId", recipeId)
        }
        findNavController().navigate(R.id.action_recipeFragment_to_createRecipeFragment, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}