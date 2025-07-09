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

    private var loadingItemVisible = false

    override fun getItemViewType(position: Int) = when {
        position < super.getItemCount() -> TYPE_RECIPE
        else -> TYPE_LOADING
    }

    override fun getItemCount() = super.getItemCount() + if (loadingItemVisible) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TYPE_RECIPE -> RecipeViewHolder(
            RecetasPopularBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onItemClick
        )
        else -> LoadingViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_loading, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RecipeViewHolder) {
            getItem(position)?.let { recipe ->
                holder.bind(recipe)
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
                popularTime.text = "⏱ ${recipe.time} | ${recipe.category}"
                popularDifficulty.text = "Dificultad: ${recipe.difficulty}"

                Glide.with(root.context)
                    .load(recipe.imageUrl)
                    .into(popularImg)

                root.setOnClickListener { onItemClick(recipe) }
            }
        }
    }

    inner class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view)

    companion object {
        private const val TYPE_RECIPE = 0
        private const val TYPE_LOADING = 1

        private val RecipeDiffCallback = object : DiffUtil.ItemCallback<Recipe>() {
            override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe) =
                oldItem == newItem
        }
    }
}