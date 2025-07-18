package com.example.proyectorecetas


import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val created_at: String=""
)