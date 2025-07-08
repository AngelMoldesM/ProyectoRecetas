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
) : ListAdapter<Recipe, RecyclerView.ViewHolder>(RECIPE_COMPARATOR) {

    companion object {
        private val RECIPE_COMPARATOR = object : DiffUtil.ItemCallback<Recipe>() {
            override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
                return oldItem == newItem
            }
        }

        private const val TYPE_RECIPE = 0
        private const val TYPE_LOADING = 1
    }

    private var loadingItemVisible = false

    override fun getItemViewType(position: Int): Int {
        return if (position < super.getItemCount()) {
            TYPE_RECIPE
        } else {
            TYPE_LOADING
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (loadingItemVisible) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_RECIPE -> {
                val binding = RecetasPopularBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                RecipeViewHolder(binding, onItemClick)
            }
            else -> {
                // Crear una vista simple para el loading
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RecipeViewHolder -> {
                getItem(position)?.let { recipe ->
                    holder.bind(recipe)
                    // Solo cargar más si estamos cerca del final
                    if (position >= itemCount - 3) {
                        onLoadMore()
                    }
                }
            }
        }
    }

    inner class RecipeViewHolder(
        private val binding: RecetasPopularBinding, // Usar RecetasPopularBinding
        private val onItemClick: (Recipe) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.popularTxt.text = recipe.title
            binding.popularTime.text = "⏱ ${recipe.time} | ${recipe.category}"
            binding.popularDifficulty.text = "Dificultad: ${recipe.difficulty}"

            Glide.with(binding.root.context)
                .load(recipe.imageUrl)
                .into(binding.popularImg)

            binding.root.setOnClickListener { onItemClick(recipe) }
        }
    }

    inner class LoadingViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {
        // Sin binding para el loading
    }


}