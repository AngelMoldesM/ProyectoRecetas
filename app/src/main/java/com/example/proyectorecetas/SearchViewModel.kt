package com.example.proyectorecetas

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SearchViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val PAGE_SIZE = 10

    private val _searchResults = MutableLiveData<List<Recipe>>()
    val searchResults: LiveData<List<Recipe>> get() = _searchResults

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _suggestions = MutableLiveData<List<String>>()
    val suggestions: LiveData<List<String>> get() = _suggestions

    private var lastVisibleDocument: DocumentSnapshot? = null
    private var isLastPage = false

    var currentQuery = ""
        private set
    private var currentCategoryFilters = emptyList<String>()
    private var currentDifficultyFilters = emptyList<String>()

    fun searchRecipes(
        query: String,
        categoryFilters: List<String> = emptyList(),
        difficultyFilters: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = ""

                currentQuery = query
                currentCategoryFilters = categoryFilters
                currentDifficultyFilters = difficultyFilters
                lastVisibleDocument = null
                isLastPage = false

                _searchResults.value = performSearch(query, categoryFilters, difficultyFilters)
            } catch (e: Exception) {
                handleError("searchRecipes", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadMore() {
        if (isLastPage || _loading.value == true) return

        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = ""

                val newResults = performSearch(
                    currentQuery,
                    currentCategoryFilters,
                    currentDifficultyFilters,
                    lastVisibleDocument
                )

                _searchResults.value = (_searchResults.value ?: emptyList()) + newResults
            } catch (e: Exception) {
                handleError("loadMore", e)
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun performSearch(
        query: String,
        categoryFilters: List<String>,
        difficultyFilters: List<String>,
        startAfter: DocumentSnapshot? = null
    ): List<Recipe> {
        return try {
            val baseQuery = buildBaseQuery(categoryFilters, difficultyFilters, query)
            val paginatedQuery = applyPagination(baseQuery, startAfter)

            val snapshot = paginatedQuery.get().await()
            val recipes = snapshot.documents.mapNotNull { it.toRecipe() }

            updatePaginationState(snapshot, recipes.size)
            recipes
        } catch (e: Exception) {
            Log.e("SearchDebug", "Error in performSearch", e)
            emptyList()
        }
    }

    private fun buildBaseQuery(
        categoryFilters: List<String>,
        difficultyFilters: List<String>,
        query: String
    ): Query {
        var baseQuery = db.collection("recipes")
            .whereEqualTo("isPublic", true)

        if (categoryFilters.isNotEmpty()) {
            baseQuery = baseQuery.whereIn("category", categoryFilters)
        }

        if (difficultyFilters.isNotEmpty()) {
            baseQuery = baseQuery.whereIn("difficulty", difficultyFilters)
        }

        return if (query.isNotBlank()) {
            baseQuery
                .orderBy("searchTitle")
                .startAt(query)
                .endAt("$query\uf8ff")
        } else {
            baseQuery.orderBy("timestamp", Query.Direction.DESCENDING)
        }
    }

    private fun applyPagination(baseQuery: Query, startAfter: DocumentSnapshot?) =
        startAfter?.let { baseQuery.startAfter(it).limit(PAGE_SIZE.toLong()) }
            ?: baseQuery.limit(PAGE_SIZE.toLong())

    private fun updatePaginationState(snapshot: QuerySnapshot, resultCount: Int) {
        if (snapshot.documents.isNotEmpty()) {
            lastVisibleDocument = snapshot.documents.last()
        }
        isLastPage = resultCount < PAGE_SIZE
    }

    private fun DocumentSnapshot.toRecipe(): Recipe? {
        return try {
            toObject(Recipe::class.java)?.copy(id = id)
        } catch (e: Exception) {
            Log.e("SearchDebug", "Error converting document $id", e)
            null
        }
    }

    fun getSearchSuggestions(query: String) {
        if (query.length < 2) {
            _suggestions.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val snapshot = db.collection("recipes")
                    .whereGreaterThanOrEqualTo("searchTitle", query)
                    .whereLessThanOrEqualTo("searchTitle", "$query\uf8ff")
                    .limit(5)
                    .get()
                    .await()

                _suggestions.value = snapshot.documents
                    .mapNotNull { it.getString("searchTitle") }
                    .distinct()
            } catch (e: Exception) {
                handleError("getSearchSuggestions", e)
                _suggestions.value = emptyList()
            }
        }
    }

    private fun handleError(context: String, e: Exception) {
        _error.value = "Error in $context: ${e.localizedMessage}"
        Log.e("SearchError", "Error in $context", e)
    }

    fun clearSuggestions() {
        _suggestions.value = emptyList()
    }
}