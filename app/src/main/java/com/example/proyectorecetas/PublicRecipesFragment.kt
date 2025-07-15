package com.example.proyectorecetas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectorecetas.databinding.FragmentPublicRecipesBinding
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PublicRecipesFragment : Fragment() {

    private lateinit var binding: FragmentPublicRecipesBinding
    private lateinit var adapter: RecipeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPublicRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadPublicRecipes()
    }

    private fun setupRecyclerView() {
        adapter = RecipeAdapter(emptyList()) { recipe ->
            navigateToRecipeDetail(recipe)
        }
        binding.rvPublicRecipes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPublicRecipes.adapter = adapter
    }

    private fun loadPublicRecipes() {
        lifecycleScope.launch {
            try {
                val response = SupabaseManager.client
                    .postgrest["recipes"]
                    .select {
                        filter { eq("is_public", true )}
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<Recipe>()


                withContext(Dispatchers.Main) {
                    adapter.updateData(response)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Mostrar error si se desea
            }
        }
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
        findNavController().navigate(R.id.action_publicRecipesFragment_to_recipeFragment, args)
    }
}
