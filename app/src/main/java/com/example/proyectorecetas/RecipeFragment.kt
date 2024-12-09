package com.example.proyectorecetas

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.example.proyectorecetas.databinding.FragmentRecetaBinding
import com.bumptech.glide.Glide

class RecipeFragment : Fragment() {

    private var _binding: FragmentRecetaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
        _binding = FragmentRecetaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener argumentos enviados al fragmento
        val img = arguments?.getString("img")
        val tittle = arguments?.getString("tittle")
        val description = arguments?.getString("des")
        val ingredients = arguments?.getString("ing")?.split("\n".toRegex())?.dropLastWhile { it.isEmpty() }

        // Configurar la UI con los datos recibidos
        Glide.with(requireContext()).load(img).into(binding.itemImg)
        binding.tittle.text = tittle
        binding.stepData.text = description
        binding.time.text = ingredients?.get(0)

        // Añadir ingredientes a la vista
        ingredients?.let {
            var ingredientText = ""
            for (i in 1 until it.size) {
                ingredientText += "🟢 ${it[i]}\n"
            }
            binding.ingData.text = ingredientText
        }

        // Configurar botones de pasos e ingredientes

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
