package com.example.proyectorecetas

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.proyectorecetas.databinding.FragmentRecetaBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeFragment : Fragment() {

    private var _binding: FragmentRecetaBinding? = null
    private val binding get() = _binding!!
    private val ratingRepository = RatingRepository()
    private val favoriteRepository = FavoriteRepository()
    private lateinit var btnFavorite: ImageButton
    private var isFavorite = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecetaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: ""
        val recipeUserId = arguments?.getString("userId") ?: ""
        binding.difficulty.text = "Dificultad: ${arguments?.getString("difficulty") ?: "Media"}"
        btnFavorite = binding.btnFavorite
        val recipeId = arguments?.getString("id") ?: ""

        if (recipeId.isBlank()) {
            //Toast.makeText(context, "Receta inválida", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }


        binding.fabOptions.visibility = if (currentUserId == recipeUserId) View.VISIBLE else View.GONE

        setupOptionsMenu()

        // Recibir parámetros
        val args = arguments?.let {
            mapOf(
                "id" to it.getString("id"),
                "imageUrl" to it.getString("img"),
                "title" to it.getString("tittle"),
                "description" to it.getString("des"),
                "ingredients" to it.getString("ing"),
                "time" to it.getString("time")
            )
        }


        // Configurar UI
        Glide.with(requireContext())
            .load(args?.get("imageUrl"))
            .into(binding.itemImg)

        binding.tittle.text = args?.get("title").toString()
        binding.time.text = "${args?.get("time")}"
        binding.stepData.text = args?.get("description").toString()

        // Formatear ingredientes
        val ingredientsText = args?.get("ingredients").toString()
            .replace("\\n", "\n")
            .split("\n")
            .filter { it.isNotBlank() }
            .joinToString("\n") { "🟢 $it" }
        binding.ingData.text = ingredientsText

        // Configurar botones
        binding.step.setOnClickListener {
            binding.stepScroll.visibility = View.VISIBLE
            binding.ingScroll.visibility = View.GONE
        }

        binding.ing.setOnClickListener {
            binding.ingScroll.visibility = View.VISIBLE
            binding.stepScroll.visibility = View.GONE
        }

        // Botón de regreso
        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        lifecycleScope.launch {
            isFavorite = currentUserId.isNotEmpty() && favoriteRepository.isFavorite(currentUserId, recipeId)
            updateFavoriteIcon()
        }

        btnFavorite.setOnClickListener {
            if (currentUserId.isEmpty()) {
                Toast.makeText(context, "Debes iniciar sesión para guardar favoritos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                if (isFavorite) {
                    favoriteRepository.removeFavorite(currentUserId, recipeId)
                } else {
                    favoriteRepository.addFavorite(currentUserId, recipeId)
                }
                isFavorite = !isFavorite
                updateFavoriteIcon()
            }
        }

        setupRatingBar()
        loadRating()
    }


    private fun setupOptionsMenu() {
        var isMenuVisible = false

        binding.fabOptions.setOnClickListener {
            isMenuVisible = !isMenuVisible
            binding.optionsMenu.visibility = if (isMenuVisible) View.VISIBLE else View.GONE
        }

        binding.btnEdit.setOnClickListener {
            val recipeId = arguments?.getString("id") ?: ""
            navigateToEditRecipe(recipeId)
            binding.optionsMenu.visibility = View.GONE
        }

        binding.btnDelete.setOnClickListener {
            confirmDeleteRecipe()
            binding.optionsMenu.visibility = View.GONE
        }

        binding.btnShare.setOnClickListener {
            shareRecipe()
            binding.optionsMenu.visibility = View.GONE
        }
    }

    private fun loadRating() {
        val recipeId = arguments?.getString("id") ?: return
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id

        lifecycleScope.launch {
            // Obtener valoración del usuario actual
            val userRating = userId?.let { ratingRepository.getUserRating(recipeId, it) } ?: 0
            binding.ratingBar.rating = userRating.toFloat()

            // Obtener promedio general
            val recipe = SupabaseManager.client.postgrest["recipes"].select {
                filter { eq("id", recipeId) }

            }.decodeSingle<Recipe>()
            binding.tvAverageRating.text = "%.1f".format(recipe.average_rating)
            binding.tvRatingCount.text = "(${recipe.rating_count})"
        }
    }

    private fun setupRatingBar() {
        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (fromUser) {
                val recipeId = arguments?.getString("id") ?: return@setOnRatingBarChangeListener
                val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return@setOnRatingBarChangeListener

                lifecycleScope.launch {
                    ratingRepository.submitRating(recipeId, userId, rating.toInt())
                    // Actualizar UI
                    loadRating()
                }
            }
        }
    }

    private fun updateFavoriteIcon() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            btnFavorite.setImageResource(R.drawable.ic_favorite_border)
        }
    }

    private fun confirmDeleteRecipe() {
        AlertDialog.Builder(requireContext())
            .setTitle("Borrar receta")
            .setMessage("¿Estás seguro de que quieres borrar esta receta? Esta acción no se puede deshacer.")
            .setPositiveButton("Borrar") { _, _ ->
                deleteRecipe()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteRecipe() {
        val recipeId = arguments?.getString("id") ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                SupabaseManager.client
                    .from("recipes")
                    .delete {
                        filter {
                            eq("id", recipeId)
                        }
                    }

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Receta borrada", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error al borrar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun shareRecipe() {
        val title = arguments?.getString("tittle") ?: "Receta"
        val description = arguments?.getString("des") ?: ""

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Mira esta receta: $title")
            putExtra(Intent.EXTRA_TEXT, "$title\n\n$description")
        }

        startActivity(Intent.createChooser(shareIntent, "Compartir receta"))
    }


    private fun navigateToEditRecipe(recipeId: String) {
        val args = Bundle().apply {
            putString("recipeId", recipeId)
        }
        findNavController().navigate(R.id.action_recipeFragment_to_createRecipeFragment, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}