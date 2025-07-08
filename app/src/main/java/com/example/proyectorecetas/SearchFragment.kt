package com.example.proyectorecetas

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectorecetas.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var adapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener el término de búsqueda inicial
        val initialQuery = arguments?.getString("searchQuery") ?: ""

        setupUI(initialQuery)
        setupRecyclerView()
        setupObservers()
        setupListeners()

        if (initialQuery.isNotEmpty()) {
            viewModel.searchRecipes(initialQuery)
        }
    }

    private fun setupUI(initialQuery: String) {
        binding.searchView.setText(initialQuery)
        binding.searchBar.setText( initialQuery)

        if (initialQuery.isNotEmpty()) {
            showKeyboard()
            viewModel.searchRecipes(initialQuery)
        }
    }

    private fun showKeyboard() {
        binding.searchView.show()
        binding.searchView.editText.requestFocus()

        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchView.editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setupRecyclerView() {
        adapter = SearchAdapter(
            onItemClick = { recipe ->
                navigateToRecipeDetail(recipe)
            },
            onLoadMore = {
                viewModel.loadMore()
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SearchFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.searchResults.observe(viewLifecycleOwner) { recipes ->
            adapter.submitList(recipes)

            // Reemplazar emptyState con manejo alternativo
            if (recipes.isEmpty()) {
                Toast.makeText(requireContext(), "No se encontraron recetas", Toast.LENGTH_SHORT).show()
            }
        }

        // Eliminar sección de suggestionsContainer
        viewModel.suggestions.observe(viewLifecycleOwner) { suggestions ->
            // Opcional: Implementar sugerencias de otra forma si es necesario
        }
    }

    private fun setupListeners() {
        binding.searchBar.setOnClickListener {
            binding.searchView.show()
        }

        binding.searchView.editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.searchView.text.toString().trim()
                if (query.isNotBlank()) {
                    binding.searchBar.setText(query)
                    viewModel.searchRecipes(query)
                }
                binding.searchView.hide()
                true
            } else {
                false
            }
        }

        binding.searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable?) {
                val query = editable?.toString()?.trim() ?: ""
                if (query.isNotBlank()) {
                    viewModel.getSearchSuggestions(query)
                } else {
                    viewModel.clearSuggestions()
                }
            }
        })

        binding.fabFilters.setOnClickListener {
            showFilterBottomSheet()
        }

        binding.searchView.addTransitionListener { _, _, newState ->
            if (newState == com.google.android.material.search.SearchView.TransitionState.HIDDEN) {
                hideKeyboard()
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
    }

    private fun showFilterBottomSheet() {
        val bottomSheet = FilterBottomSheet { categories, difficulties ->
            viewModel.searchRecipes(viewModel.currentQuery, categories, difficulties)
        }
        bottomSheet.show(parentFragmentManager, "FilterBottomSheet")
    }

    private fun navigateToRecipeDetail(recipe: Recipe) {
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
        // Usar el NavController del fragmento actual
        findNavController().navigate(R.id.action_global_recipeFragment, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}