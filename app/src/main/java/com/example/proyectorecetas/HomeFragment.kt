package com.example.proyectorecetas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectorecetas.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recipeAdapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.salad.setOnClickListener {
            val bundle = Bundle().apply {
                putString("TITTLE", "Salad")
                putString("CATEGORY", "Salad")
            }
            // Navegar a CategoryFragment
            findNavController().navigate(R.id.action_homeFragment_to_categoryFragment, bundle)
        }

        binding.mainDish.setOnClickListener {
            val bundle = Bundle().apply {
                putString("TITTLE", "Main Dish")
                putString("CATEGORY", "Dish")
            }
            findNavController().navigate(R.id.action_homeFragment_to_categoryFragment, bundle)
        }

        binding.drinks.setOnClickListener {
            val bundle = Bundle().apply {
                putString("TITTLE", "Drinks")
                putString("CATEGORY", "Drinks")
            }
            findNavController().navigate(R.id.action_homeFragment_to_categoryFragment, bundle)
        }

        binding.desserts.setOnClickListener {
            val bundle = Bundle().apply {
                putString("TITTLE", "Desserts")
                putString("CATEGORY", "Desserts")
            }
            findNavController().navigate(R.id.action_homeFragment_to_categoryFragment, bundle)
        }


        // Configurar el RecyclerView con orientación horizontal
        /*ARREGLAR O CAMBIAR!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
        * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
/*       binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = recipeAdapter
        }*/
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
