package com.example.proyectorecetas

import kotlinx.serialization.Serializable


@Serializable
data class Recipe(
    val id: String = "",
    val user_id: String = "",
    val title: String = "",
    val description: String = "",
    val ingredients: String = "",
    val time: String = "",
    val difficulty: String = "Media",
    val category: String = "",
    val image_path: String = "",
    val created_at: String="",
    val is_public: Boolean = true,
    val average_rating: Float = 0f,
    val rating_count: Int = 0
)