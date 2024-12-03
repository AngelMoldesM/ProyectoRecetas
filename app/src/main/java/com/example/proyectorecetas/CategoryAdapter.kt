package com.example.proyectorecetas

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyectorecetas.databinding.CategoriaBinding

class CategoryAdapter(var dataList: ArrayList<Recipe>, var context: Context) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(var binding: CategoriaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CategoriaBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(dataList[position].img).into(holder.binding.img)
        holder.binding.tittle.text = dataList[position].tittle

        // Parsear el tiempo de la receta
        val temp = dataList[position].ing.split("\n").dropLastWhile { it.isEmpty() }.toTypedArray()
        holder.binding.time.text = temp[0]

        // Configurar el listener para la navegación al fragmento de receta
        holder.binding.next.setOnClickListener {
            val bundle = Bundle().apply {
                putString("img", dataList[position].img)
                putString("tittle", dataList[position].tittle)
                putString("des", dataList[position].des)
                putString("ing", dataList[position].ing)
            }

            // Usar NavController para navegar al RecipeFragment con el bundle
            val navController = (context as? FragmentActivity)?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment)?.findNavController()
            navController?.navigate(R.id.action_categoryFragment_to_recipeFragment, bundle)
        }
    }
}
