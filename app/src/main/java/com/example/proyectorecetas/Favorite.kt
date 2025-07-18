package com.example.proyectorecetas

import kotlinx.serialization.Serializable

@Serializable
data class Favorite(
    val id:String = "",
    val user_id:String = "",
    val recipe_id:String = "",
    val created_at:String = ""

)