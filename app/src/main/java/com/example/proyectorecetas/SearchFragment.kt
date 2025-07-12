package com.example.proyectorecetas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectorecetas.databinding.FragmentSearchBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var adapter: SearchAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentSearchBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val initialQuery = arguments?.getString("searchQuery").orEmpty()

        setupUI(initialQuery)
        setupRecyclerView()
        setupObservers()
        setupListeners()

        if (initialQuery.isNotEmpty()) viewModel.searchRecipes(initialQuery)

        binding.DrawerButton.setOnClickListener {
            sharedViewModel.toggleDrawer()
        }

    }

    private fun setupUI(initialQuery: String) {
        binding.searchBar.setText(initialQuery)
        binding.searchBar.setOnClickListener { showSearchInput() }
    }

    private fun showSearchInput() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_search_input, null)
        val editText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editText)

        // Establecer texto actual si existe
        editText.setText(binding.searchBar.text)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Buscar recetas")
            .setView(dialogView)
            .setPositiveButton("Buscar") { _, _ ->
                val input = editText.text?.toString()?.trim() ?: ""
                if (input.isNotEmpty()) {
                    performSearch(input)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupRecyclerView() {
        adapter = SearchAdapter(
            onItemClick = ::navigateToRecipeDetail,
            onLoadMore = viewModel::loadMore
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SearchFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.searchResults.observe(viewLifecycleOwner) { recipes ->
            adapter.submitList(recipes)
            if (recipes.isEmpty()) showEmptyState()
        }
    }

    private fun showEmptyState() {
        // Implementar vista de estado vacío si es necesario
        binding.emptyState.visibility = View.VISIBLE
    }

    private fun setupListeners() {
        binding.fabFilters.setOnClickListener { showFilterBottomSheet() }
    }

    private fun performSearch(query: String) {
        binding.searchBar.setText(query)
        viewModel.searchRecipes(query)
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun showFilterBottomSheet() {
        FilterBottomSheet { categories, difficulties ->
            viewModel.searchRecipes(viewModel.currentQuery, categories, difficulties)
        }.show(parentFragmentManager, "FilterBottomSheet")
    }

    private fun navigateToRecipeDetail(recipe: Recipe) {
        findNavController().navigate(
            R.id.action_global_recipeFragment,
            Bundle().apply {
                putString("id", recipe.id)
                putString("img", recipe.imageUrl)
                putString("tittle", recipe.title)
                putString("des", recipe.description)
                putString("ing", recipe.ingredients)
                putString("time", recipe.time)
                putString("difficulty", recipe.difficulty)
                putString("userId", recipe.userId)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}