package com.example.proyectorecetas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyectorecetas.databinding.RecetasPopularBinding

class RecipeAdapter(
    private val recipeList: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(private val binding: RecetasPopularBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: Recipe) {
            // Bind data to views
            binding.popularTxt.text = recipe.tittle
            binding.popularTime.text = "⌚ ${recipe.category}"

            // Cargar imagen usando Glide
            Glide.with(binding.popularImg.context)
                .load(recipe.img)
                .into(binding.popularImg)

            // Hacer clic en el item para ejecutar la acción
            binding.root.setOnClickListener { onItemClick(recipe) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = RecetasPopularBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipeList[position])
        val params = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(16, 16, 16, 16)  // Aplicar margen a todos los lados
        holder.itemView.layoutParams = params
    }

    override fun getItemCount(): Int = recipeList.size
}
