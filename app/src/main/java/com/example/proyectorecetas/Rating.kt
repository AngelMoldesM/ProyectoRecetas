package com.example.proyectorecetas

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Rating(
    val id: String = "",
    val recipe_id: String = "",
    val user_id: String = "",
    val rating: Int,
    val created_at:String = Clock.System.now().toString()
)