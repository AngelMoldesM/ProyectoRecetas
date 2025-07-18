package com.example.proyectorecetas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.proyectorecetas.databinding.FragmentCreatedRecipesBinding
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreatedRecipesFragment : Fragment() {

    private var _binding: FragmentCreatedRecipesBinding? = null
    private val binding get() = _binding!!
    private lateinit var rvAdapter: RecipeAdapter

    private val supabase: SupabaseClient
        get() = SupabaseManager.client

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatedRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadRecipesFromSupabase()
    }

    private fun setupRecyclerView() {
        rvAdapter = RecipeAdapter(emptyList()) { recipe ->
            navigateToRecipeDetail(recipe)
        }
        binding.rvCreatedRecipes.layoutManager = GridLayoutManager(requireContext(), 2)

        binding.rvCreatedRecipes.adapter = rvAdapter
    }

    private fun navigateToRecipeDetail(recipe: Recipe) {
        val args = Bundle().apply {
            putString("id", recipe.id)
            putString("img", recipe.image_path)
            putString("tittle", recipe.title)
            putString("des", recipe.description)
            putString("ing", recipe.ingredients)
            putString("time", recipe.time)
            putString("difficulty", recipe.difficulty)
            putString("userId", recipe.user_id)
        }
        findNavController().navigate(R.id.action_createdRecipesFragment_to_recipeFragment, args)
    }

    private fun loadRecipesFromSupabase() {
        val userId = supabase.auth.currentUserOrNull()?.id

        if (userId == null) {
            showError("User not authenticated")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = supabase
                    .from("recipes")
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<Recipe>()

                withContext(Dispatchers.Main) {
                    rvAdapter.updateData(result)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Error loading recipes: ${e.message}")
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
