package com.example.proyectorecetas

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// Error: Las propiedades no coinciden con el uso en otras clases
data class Recipe(
    @DocumentId var id: String = "",
    var userId: String = "",
    var title: String = "",
    var searchTitle: String = "",
    var description: String = "",
    var ingredients: String = "",
    var time: String = "",
    var difficulty: String = "Media",
    var category: String = "",
    var imageUrl: String = "",
    var timestamp: Timestamp = Timestamp.now(),
    var isPublic: Boolean = true
)