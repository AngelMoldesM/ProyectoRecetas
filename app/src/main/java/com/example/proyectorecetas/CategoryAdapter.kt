package com.example.proyectorecetas

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyectorecetas.databinding.CategoriaBinding

class CategoryAdapter(
    var dataList: List<Recipe>,  // Cambiado a List<Recipe>
    var context: Context
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(var binding: CategoriaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = dataList[position]

        // CORRECCIÓN: Usar propiedades correctas del modelo Recipe
        Glide.with(context).load(recipe.imageUrl).into(holder.binding.img)
        holder.binding.tittle.text = recipe.title
        holder.binding.time.text = "⏱ ${recipe.time}"

        holder.binding.next.setOnClickListener {
            val bundle = Bundle().apply {
                putString("id", recipe.id)
                putString("img", recipe.imageUrl)
                putString("tittle", recipe.title)
                putString("des", recipe.description)
                putString("ing", recipe.ingredients)
                putString("time", recipe.time)
                putString("userId", recipe.userId)
            }

            // Navegación mejorada
            findNavController(holder.itemView).navigate(R.id.recipeFragment, bundle)
        }
    }

    // Función auxiliar para navegación
    private fun findNavController(view: View): NavController {
        return Navigation.findNavController(view.context as FragmentActivity, R.id.nav_host_fragment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CategoriaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dataList.size
}
