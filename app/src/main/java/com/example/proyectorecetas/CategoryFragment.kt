package com.example.proyectorecetas


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectorecetas.databinding.FragmentCategoriaBinding
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        binding.tittle.text = title ?: "Without title"


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
        // Ejemplo para usar supabase con coroutines (asegúrate de estar en un contexto coroutine)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val recipes = SupabaseManager.client.postgrest["recipes"]
                    .select {
                        filter {
                            eq("category", category)
                            eq("is_public", true)
                        }
                    }
                    .decodeList<Recipe>()

                withContext(Dispatchers.Main) {
                    dataList.clear()
                    dataList.addAll(recipes)
                    rvAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("CategoryFragment", "Error loading recipes", e)
                // Mostrar mensaje o estado error si quieres
            }
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
