package com.example.proyectorecetas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.proyectorecetas.databinding.FragmentCategoriaBinding

class CategoryFragment : Fragment() {

    private lateinit var rvAdapter: CategoryAdapter
    private lateinit var dataList: ArrayList<Recipe>
    private var _binding: FragmentCategoriaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar el layout del fragmento
        _binding = FragmentCategoriaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar el título
        val title = arguments?.getString("TITTLE")
        activity?.title = title

        setupRecyclerView()

      /*  // Configuración del botón de "volver"
        binding.goBackHome.setOnClickListener {
            activity?.onBackPressed() // Regresar a la actividad anterior
        }*/
    }

    private fun setupRecyclerView() {
        dataList = ArrayList()
        binding.rvCategory.layoutManager = LinearLayoutManager(requireContext())

        val db = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "db_name"
        )
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .createFromAsset("recipe.db")
            .build()

        val daoObject = db.getDao()
        val recipes = daoObject.getAll()

        for (i in recipes!!.indices) {
            if (recipes[i]!!.category.contains(arguments?.getString("CATEGORY")!!)) {
                dataList.add(recipes[i]!!)
            }
        }

        rvAdapter = CategoryAdapter(dataList, requireContext())
        binding.rvCategory.adapter = rvAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Liberar la referencia al binding
    }
}
