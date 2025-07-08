package com.example.proyectorecetas

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    private var currentDifficulties = emptyList<String>()
    private var currentCategoryFilters = emptyList<String>()
    private var currentDifficultyFilters = emptyList<String>()

    private var lastVisibleDocument: DocumentSnapshot? = null
    private var isLastPage = false
    var currentQuery = ""
        private set
    private var currentFilters = emptyList<String>()

    fun searchRecipes(
        query: String,
        categoryFilters: List<String> = emptyList(),
        difficultyFilters: List<String> = emptyList()) {
        viewModelScope.launch {
            try {
                _loading.value = true
                currentQuery = query
                currentCategoryFilters = categoryFilters
                currentDifficultyFilters = difficultyFilters
                lastVisibleDocument = null
                isLastPage = false

                currentDifficulties = difficultyFilters
                val results = performSearch(query, categoryFilters, difficultyFilters, null)
                _searchResults.value = results
                _error.value = ""
            } catch (e: Exception) {
                _error.value = "Error al buscar: ${e.message}"
                Log.e("SearchError", "Error en searchRecipes: ${e.message}", e)
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
                val results = performSearch(currentQuery,currentCategoryFilters,currentDifficultyFilters, lastVisibleDocument)
                val currentList = _searchResults.value ?: emptyList()
                _searchResults.value = currentList + results
                _error.value = ""
            } catch (e: Exception) {
                _error.value = "Error al cargar más: ${e.message}"
                Log.e("SearchError", "Error en loadMore: ${e.message}", e)
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun performSearch(
        query: String,
        categoryFilters: List<String>,
        difficultyFilters: List<String>,
        startAfter: DocumentSnapshot?
    ): List<Recipe> {
        try {
            Log.d("SearchDebug", "Iniciando búsqueda - Query: '$query', Filtros: $categoryFilters")

            var baseQuery: Query = db.collection("recipes")
                .whereEqualTo("isPublic", true)

            // 1. Aplicar filtros
            if (categoryFilters.isNotEmpty()) {
                Log.d("SearchDebug", "Aplicando filtros: $categoryFilters")
                baseQuery = baseQuery.whereIn("category", categoryFilters)
            }

            if (difficultyFilters.isNotEmpty()) {
                baseQuery = baseQuery.whereIn("difficulty", difficultyFilters)
            }

            // 2. Manejar búsqueda de texto - USANDO "title" EN LUGAR DE "search_title"
            if (query.isNotBlank()) {
                Log.d("SearchDebug", "Aplicando búsqueda de texto: '$query'")
                baseQuery = baseQuery
                    .orderBy("searchTitle")  // CAMBIO IMPORTANTE: Usar "title"
                    .startAt(query)
                    .endAt("$query\uf8ff")
            } else {
                Log.d("SearchDebug", "Ordenando por fecha (sin texto)")
                baseQuery = baseQuery.orderBy("timestamp", Query.Direction.DESCENDING)
            }

            // 3. Paginación
            var paginatedQuery = baseQuery.limit(PAGE_SIZE.toLong())
            startAfter?.let {
                Log.d("SearchDebug", "Continuando paginación desde documento: ${it.id}")
                paginatedQuery = paginatedQuery.startAfter(it)
            }

            // Ejecutar consulta
            Log.d("SearchDebug", "Consultando Firestore...")
            val snapshot = paginatedQuery.get().await()
            Log.d("SearchDebug", "Consulta completada - Documentos encontrados: ${snapshot.documents.size}")

            val recipes = snapshot.documents.mapNotNull {
                try {
                    val recipe = it.toObject(Recipe::class.java)?.copy(id = it.id)
                    if (recipe == null) {
                        Log.w("SearchDebug", "Documento ${it.id} no pudo convertirse a Recipe")
                    }
                    recipe
                } catch (e: Exception) {
                    Log.e("SearchDebug", "Error convirtiendo documento ${it.id}: ${e.message}")
                    null
                }
            }

            // 4. Actualizar estado de paginación
            if (snapshot.documents.isNotEmpty()) {
                lastVisibleDocument = snapshot.documents.last()
                Log.d("SearchDebug", "Último documento visible: ${lastVisibleDocument?.id}")
            }
            isLastPage = recipes.size < PAGE_SIZE
            Log.d("SearchDebug", "isLastPage: $isLastPage")

            return recipes
        } catch (e: Exception) {
            Log.e("SearchDebug", "Error en performSearch: ${e.message}", e)
            throw e
        }
    }

    fun getSearchSuggestions(query: String) {
        viewModelScope.launch {
            if (query.length < 2) {
                _suggestions.value = emptyList()
                return@launch
            }

            try {
                val snapshot = db.collection("recipes")
                    .whereGreaterThanOrEqualTo("searchTitle", query)
                    .whereLessThanOrEqualTo("searchTitle", query + "\uf8ff")
                    .limit(5)
                    .get()
                    .await()

                val titles = snapshot.documents.mapNotNull {
                    it.getString("searchTitle")
                }.distinct()

                _suggestions.value = titles
            } catch (e: Exception) {
                _suggestions.value = emptyList()
                Log.e("SearchError", "Error en getSearchSuggestions: ${e.message}", e)
            }
        }
    }

    fun clearSuggestions() {
        _suggestions.value = emptyList()
    }
}