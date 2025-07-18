package com.example.proyectorecetas

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.proyectorecetas.databinding.FragmentFavoritesBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch


class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val favoriteRepository = FavoriteRepository()
    private lateinit var recipeAdapter: RecipeAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.imageView.setOnClickListener {
            sharedViewModel.toggleDrawer()
        }

        setupRecyclerView()
        loadFavorites()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(emptyList()) { recipe ->
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
            findNavController().navigate(R.id.action_favoritesFragment_to_recipeFragment, args)
        }
        binding.recyclerViewFavorites.adapter = recipeAdapter
        binding.recyclerViewFavorites.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun loadFavorites() {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: run {
            showEmptyState()
            return
        }

        lifecycleScope.launch {
            try {
                val recipes = favoriteRepository.getUserFavorites(userId)
                if (recipes.isEmpty()) {
                    showEmptyState()
                } else {
                    recipeAdapter.updateData(recipes)
                    binding.emptyState.visibility = View.GONE
                }
            } catch (e: Exception) {
                showEmptyState()
                Log.e("FavoritesFragment", "Error loading favorites", e)
            }
        }
    }


    private fun showEmptyState() {
        binding.emptyState.visibility = View.VISIBLE
        binding.emptyStateText.text = "Aún no tienes recetas favoritas"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}