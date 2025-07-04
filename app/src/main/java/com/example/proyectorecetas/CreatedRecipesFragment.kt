package com.example.proyectorecetas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectorecetas.databinding.FragmentCreatedRecipesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CreatedRecipesFragment : Fragment() {

    private var _binding: FragmentCreatedRecipesBinding? = null
    private val binding get() = _binding!!
    private lateinit var rvAdapter: RecipeAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
        loadRecipesFromFirestore()
    }

    private fun setupRecyclerView() {
        rvAdapter = RecipeAdapter(emptyList()) { recipe ->
            navigateToRecipeDetail(recipe)
        }
        binding.rvCreatedRecipes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCreatedRecipes.adapter = rvAdapter
    }

    private fun navigateToRecipeDetail(recipe: Recipe) {
        val args = Bundle().apply {
            putString("id", recipe.id)
            putString("img", recipe.imageUrl)
            putString("tittle", recipe.title)
            putString("des", recipe.description)
            putString("ing", recipe.ingredients)
            putString("time", recipe.time)
        }
        findNavController().navigate(R.id.action_createdRecipesFragment_to_recipeFragment, args)
    }

    private fun loadRecipesFromFirestore() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("recipes")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    showError("Error al cargar recetas: ${error.message}")
                    return@addSnapshotListener
                }

                val recipes = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Recipe::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                rvAdapter.updateData(recipes)
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