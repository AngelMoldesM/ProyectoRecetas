package com.example.proyectorecetas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.proyectorecetas.databinding.FragmentRecetaBinding

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
        binding.time.text = "⏱ ${args?.get("time")}"
        binding.stepData.text = args?.get("description").toString()

        // Formatear ingredientes
        val ingredientsText = args?.get("ingredients").toString()
            .split("\n")
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}