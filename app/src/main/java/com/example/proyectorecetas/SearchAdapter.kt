package com.example.proyectorecetas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyectorecetas.databinding.RecetasPopularBinding

class SearchAdapter(
    private val onItemClick: (Recipe) -> Unit,
    private val onLoadMore: () -> Unit
) : ListAdapter<Recipe, RecyclerView.ViewHolder>(RecipeDiffCallback) {

    // 1. Eliminar la variable de control del ítem de carga
    // 2. Simplificar getItemCount()
    override fun getItemCount() = super.getItemCount()

    // 3. Eliminar la lógica de tipos de vista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RecipeViewHolder(
        RecetasPopularBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ),
        onItemClick
    )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RecipeViewHolder) {
            getItem(position)?.let { recipe ->
                holder.bind(recipe)
                // 4. Mantener la carga automática si es necesario
                if (position >= itemCount - 3) onLoadMore()
            }
        }
    }

    inner class RecipeViewHolder(
        private val binding: RecetasPopularBinding,
        private val onItemClick: (Recipe) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            with(binding) {
                popularTxt.text = recipe.title
                popularTime.text = recipe.time

                Glide.with(root.context)
                    .load(recipe.image_path)
                    .into(popularImg)

                root.setOnClickListener { onItemClick(recipe) }
            }
        }
    }

    companion object {
        private val RecipeDiffCallback = object : DiffUtil.ItemCallback<Recipe>() {
            override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe) =
                oldItem == newItem
        }
    }
}