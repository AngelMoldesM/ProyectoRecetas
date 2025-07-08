package com.example.proyectorecetas

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.proyectorecetas.databinding.FragmentRecetaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecipeFragment : Fragment() {

    private var _binding: FragmentRecetaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecetaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recipeId = arguments?.getString("id") ?: ""
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val recipeUserId = arguments?.getString("userId") ?: ""
        binding.difficulty.text = "Dificultad: ${arguments?.getString("difficulty") ?: "Media"}"

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

       /* if (currentUserId == recipeUserId) {
            binding.editButton.visibility = View.VISIBLE
        } else {
            binding.editButton.visibility = View.GONE
        }

        binding.editButton.setOnClickListener {
            val recipeId = arguments?.getString("id") ?: ""
            navigateToEditRecipe(recipeId)
        }*/



        // Configurar UI
        Glide.with(requireContext())
            .load(args?.get("imageUrl"))
            .into(binding.itemImg)

        binding.tittle.text = args?.get("title").toString()
        binding.time.text = "⏱ ${args?.get("time")}"
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
        val db = FirebaseFirestore.getInstance()

        db.collection("recipes").document(recipeId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Receta borrada", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() // Volver a la pantalla anterior
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al borrar: ${e.message}", Toast.LENGTH_SHORT).show()
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