package com.example.proyectorecetas


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.proyectorecetas.databinding.FragmentCategoriaBinding
import com.google.firebase.firestore.FirebaseFirestore

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
        binding.tittle.text = title ?: "Sin título"

        setupRecyclerView()

        // Configuración del botón de volver
        binding.goBackHome.setOnClickListener {
            activity?.onBackPressed()
        }
    }
    //Configuracion del RecyclerView
    private fun setupRecyclerView() {
        dataList = ArrayList()
        binding.rvCategory.layoutManager = LinearLayoutManager(requireContext())

        rvAdapter = CategoryAdapter(dataList, requireContext())
        binding.rvCategory.adapter = rvAdapter

        val category = arguments?.getString("CATEGORY") ?: ""
        loadRecipesByCategory(category)
    }

    private fun loadRecipesByCategory(category: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("recipes")
            .whereEqualTo("category", category)
            .whereEqualTo("isPublic", true) // Solo recetas públicas
            .get()
            .addOnSuccessListener { snapshot ->
                val recipes = snapshot.documents.mapNotNull {
                    it.toObject(Recipe::class.java)?.copy(id = it.id)
                }
                dataList.clear()
                dataList.addAll(recipes)
                rvAdapter.notifyDataSetChanged()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
