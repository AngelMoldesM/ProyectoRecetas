package com.example.proyectorecetas


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectorecetas.databinding.FragmentPublicRecipesBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PublicRecipesFragment : Fragment() {

    private lateinit var binding: FragmentPublicRecipesBinding
    private lateinit var adapter: RecipeAdapter
    private val db = FirebaseFirestore.getInstance()

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
        db.collection("recipes")
            .whereEqualTo("isPublic", true)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val recipes = snapshot?.documents?.mapNotNull {
                    it.toObject(Recipe::class.java)?.copy(id = it.id)
                } ?: emptyList()

                adapter.updateData(recipes)
            }
    }

    private fun navigateToRecipeDetail(recipe: Recipe) {
        val args = Bundle().apply {
            putString("id", recipe.id)
            putString("img", recipe.imageUrl)
            putString("tittle", recipe.title)
            putString("des", recipe.description)
            putString("ing", recipe.ingredients)
            putString("time", recipe.time)
            putString("userId", recipe.userId)
        }
        findNavController().navigate(R.id.action_publicRecipesFragment_to_recipeFragment, args)
    }
}