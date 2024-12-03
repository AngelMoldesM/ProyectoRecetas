package com.example.proyectorecetas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.proyectorecetas.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recipeAdapter: RecipeAdapter
    private val recipeList = mutableListOf<Recipe>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración del RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            recipeAdapter = RecipeAdapter(recipeList) { recipe ->

                val args = Bundle().apply {
                    putString("img", recipe.img)
                    putString("tittle", recipe.tittle)
                    putString("des", recipe.des)
                    putString("ing", recipe.ing)
                }


                findNavController().navigate(R.id.action_homeFragment_to_recipeFragment, args)
            }
            adapter = recipeAdapter
        }

        loadRecipesFromDatabase()

        // Configurar los clics de los botones para navegar a las categorías
        binding.salad.setOnClickListener {
            val bundle = Bundle().apply {
                putString("TITTLE", "Salad")
                putString("CATEGORY", "Salad")
            }
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
    }

    private fun loadRecipesFromDatabase() {
        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "db_name"
        )
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .createFromAsset("recipe.db")
            .build()


        val recipes = db.getDao().getPopularRecipes()

        // Actualizar la lista de recetas en el RecyclerView
        activity?.runOnUiThread {
            recipeList.clear()
            recipeList.addAll(recipes)
            recipeAdapter.notifyDataSetChanged()
        }

        db.close()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
