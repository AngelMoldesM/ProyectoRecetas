package com.example.proyectorecetas

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectorecetas.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recipeAdapter: RecipeAdapter
    private val db = FirebaseFirestore.getInstance()
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

        // Configurar el clic del botón del drawer
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

        // Permitir escribir en el campo de búsqueda
        binding.search.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val query = binding.search.text.toString().trim()
                navigateToSearchFragment(query)
                true
            } else {
                false
            }
        }

        // Icono de búsqueda
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
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    val username = when {
                        document.exists() -> document.getString("username") ?: "Chef"
                        else -> "Chef"
                    }
                    binding.textView2.text = getString(R.string.hello_user, username)
                }
                .addOnFailureListener {
                    binding.textView2.text = getString(R.string.hello_chef)
                }
        } ?: run {
            binding.textView2.text = getString(R.string.hello_chef)
        }
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter(emptyList()) { recipe ->
            val args = Bundle().apply {
                putString("id", recipe.id)
                putString("img", recipe.imageUrl)
                putString("tittle", recipe.title)
                putString("des", recipe.description)
                putString("ing", recipe.ingredients)
                putString("time", recipe.time)
                putString("difficulty", recipe.difficulty)
                putString("userId", recipe.userId)
            }
            findNavController().navigate(R.id.action_homeFragment_to_recipeFragment, args)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = recipeAdapter
        }
    }

    private fun loadPopularRecipes() {
        db.collection("recipes")
            .whereEqualTo("isPublic", true)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Manejar error
                    return@addSnapshotListener
                }

                val recipes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Recipe::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                recipeAdapter.updateData(recipes)
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