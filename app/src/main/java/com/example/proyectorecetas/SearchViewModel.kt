package com.example.proyectorecetas

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val PAGE_SIZE = 10

    private val _searchResults = MutableLiveData<List<Recipe>>()
    val searchResults: LiveData<List<Recipe>> get() = _searchResults

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error


    var currentQuery = ""
        private set
    var currentCategoryFilters = emptyList<String>()
    var currentDifficultyFilters = emptyList<String>()
    private var currentOffset = 0
    private var isLastPage = false

    fun searchRecipes(query: String, categories: List<String>, difficulties: List<String>) {
        viewModelScope.launch {
            _error.value = ""
            _loading.value = true
            try {
                currentQuery = query
                currentOffset = 0
                currentCategoryFilters = categories
                currentDifficultyFilters = difficulties
                isLastPage = false

                val results = fetchRecipes(
                    query = query,
                    offset = 0,
                    categoryFilters = currentCategoryFilters,
                    difficultyFilters = currentDifficultyFilters
                )

                _searchResults.value = results
                isLastPage = results.size < PAGE_SIZE
            } catch (e: Exception) {
                _error.value = "Búsqueda fallida: ${e.localizedMessage}"
                Log.e("SearchError", "searchRecipes", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadMore() {
        if (isLastPage || _loading.value == true) return

        viewModelScope.launch {
            _loading.value = true
            try {
                val newResults = fetchRecipes(
                    query = currentQuery,
                    offset = currentOffset + PAGE_SIZE,
                    categoryFilters = currentCategoryFilters,
                    difficultyFilters = currentDifficultyFilters
                )

                val updatedList = (_searchResults.value ?: emptyList()) + newResults
                _searchResults.value = updatedList

                isLastPage = newResults.size < PAGE_SIZE
                if (!isLastPage) {
                    currentOffset += PAGE_SIZE
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar más resultados: ${e.localizedMessage}"
                Log.e("SearchError", "loadMore", e)
            } finally {
                _loading.value = false
            }
        }
    }


    private suspend fun fetchRecipes(
        query: String,
        offset: Int,
        categoryFilters: List<String>,
        difficultyFilters: List<String>
    ): List<Recipe> {
        return try {
            SupabaseManager.client.postgrest["recipes"]
                .select {
                    filter { eq("is_public", true) }
                    if (query.isNotBlank()) {
                        filter { ilike("title", "%$query%") }
                    }

                    if (categoryFilters.isNotEmpty()) {
                        filter { isIn("category", categoryFilters)  }
                    }

                    if (difficultyFilters.isNotEmpty()) {
                        filter { isIn("difficulty", difficultyFilters) }
                    }

                    order("created_at", Order.DESCENDING)
                    range(offset.toLong(), (offset + PAGE_SIZE - 1).toLong())
                }
                .decodeList<Recipe>()
        } catch (e: Exception) {
            Log.e("FetchError", "Error fetching recipes", e)
            emptyList()
        }
    }
}