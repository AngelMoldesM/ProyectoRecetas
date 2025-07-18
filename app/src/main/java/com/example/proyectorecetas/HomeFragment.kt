package com.example.proyectorecetas

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectorecetas.databinding.FragmentHomeBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recipeAdapter: RecipeAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageView.setOnClickListener {
            sharedViewModel.toggleDrawer()
        }

        setupSearchField()
        setupRecyclerView()
        loadPopularRecipes()
        setupCategoryButtons()
        loadUserName()
    }

    private fun setupSearchField() {
        binding.search.setOnClickListener {
            navigateToSearchFragment("")
        }

        binding.search.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val query = binding.search.text.toString().trim()
                navigateToSearchFragment(query)
                true
            } else {
                false
            }
        }

        binding.imageView3.setOnClickListener {
            val query = binding.search.text.toString().trim()
            navigateToSearchFragment(query)
        }
    }

    private fun navigateToSearchFragment(query: String) {
        val bundle = Bundle().apply {
            putString("searchQuery", query)
        }
        findNavController().navigate(R.id.action_homeFragment_to_searchFragment, bundle)
    }

    private fun loadUserName() {
        val user = SupabaseManager.client.auth.currentUserOrNull()
        if (user != null) {
            viewLifecycleOwner.lifecycleScope.launch() {
                try {
                    val profile = SupabaseManager.client.postgrest["profiles"]
                        .select {
                            filter { eq("id", user.id) }
                            limit(1)
                        }
                        .decodeSingleOrNull<Profile>()

                    if (_binding != null) {
                        profile?.username?.let {
                            binding.textView2.text = getString(R.string.hello_user, it)
                        } ?: run {
                            binding.textView2.text = getString(R.string.hello_chef)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error loading profile", e)
                    binding.textView2.text = getString(R.string.hello_chef)
                }
            }
        } else {
            binding.textView2.text = getString(R.string.hello_chef)
        }
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
            findNavController().navigate(R.id.action_homeFragment_to_recipeFragment, args)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            setHasFixedSize(true)  // Mejora el rendimiento
            isNestedScrollingEnabled = false  // Importante para scroll suave
            adapter = recipeAdapter
        }
    }

    private fun loadPopularRecipes() {
        lifecycleScope.launch {
            try {
                val recipes = SupabaseManager.client.postgrest["recipes"]
                    .select {
                        filter{ eq("is_public", true) }
                        order("created_at", Order.DESCENDING)
                        limit(5)
                    }
                    .decodeList<Recipe>()

                recipeAdapter.updateData(recipes)
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error loading recipes", e)
            }
        }
    }

    private fun setupCategoryButtons() {
        binding.salad.setOnClickListener { navigateToCategory("Ensaladas") }
        binding.mainDish.setOnClickListener { navigateToCategory("Carnes") }
        binding.drinks.setOnClickListener { navigateToCategory("Bebidas") }
        binding.desserts.setOnClickListener { navigateToCategory("Postres") }
    }

    private fun navigateToCategory(category: String) {
        val args = Bundle().apply {
            putString("TITTLE", category)
            putString("CATEGORY", category)
        }
        findNavController().navigate(R.id.action_homeFragment_to_categoryFragment, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
