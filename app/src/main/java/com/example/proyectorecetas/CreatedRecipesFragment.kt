package com.example.proyectorecetas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.proyectorecetas.databinding.FragmentCreatedRecipesBinding
import com.example.proyectorecetas.RecipeFragment

class CreatedRecipesFragment : Fragment() {

    private var _binding: FragmentCreatedRecipesBinding? = null
    private val binding get() = _binding!!
    private lateinit var rvAdapter: RecipeAdapter
    private lateinit var recipeList: ArrayList<Recipe>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatedRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recipeList = ArrayList()
        // Aquí pasamos la función de onItemClick
        rvAdapter = RecipeAdapter(recipeList) { recipe ->
            // Acción cuando se hace clic en una receta
            val recipeFragment = RecipeFragment().apply {
                arguments = Bundle().apply {
                    putString("img", recipe.img)
                    putString("tittle", recipe.tittle)
                    putString("des", recipe.des)
                    putString("ing", recipe.ing)
                }
            }

            // Realiza la transacción del fragmento
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, recipeFragment)
                .addToBackStack(null) //
                .commit()
        }

        // Configuración del RecyclerView
        binding.rvCreatedRecipes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCreatedRecipes.adapter = rvAdapter

        loadRecipesFromDatabase()
    }

    private fun loadRecipesFromDatabase() {
        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "recipe.db"
        ).allowMainThreadQueries().build()

        // Obtención de las recetas desde la base de datos
        val recipes = db.getDao().getAllRecipes()
        recipeList.clear()
        recipeList.addAll(recipes)
        rvAdapter.notifyDataSetChanged()

        db.close() // Cerrar la base de datos después de la consulta
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
