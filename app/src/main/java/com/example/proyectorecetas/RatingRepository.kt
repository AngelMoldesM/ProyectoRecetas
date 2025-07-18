package com.example.proyectorecetas

import android.util.Log
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

// RatingRepository.kt
class RatingRepository {

    suspend fun submitRating(recipeId: String, userId: String, stars: Int) {
        try {
            // Actualizar o crear rating
            SupabaseManager.client.postgrest["ratings"].upsert(
                Rating(
                    recipe_id = recipeId,
                    user_id = userId,
                    rating = stars
                )
            ) {
                onConflict = "recipe_id,user_id"
            }


            // Calcular nuevo promedio
            updateRecipeAverage(recipeId)
        } catch (e: Exception) {
            Log.e("RatingRepo", "Error submitting rating", e)
        }
    }

    private suspend fun updateRecipeAverage(recipeId: String) {
        // Calcular nuevo promedio
        val response = SupabaseManager.client.postgrest["ratings"]
            .select(columns = Columns.raw("rating")) {
                filter { eq("recipe_id", recipeId) }
            }


        val ratings = response.decodeList<Rating>()
        val average = ratings.map { it.rating }.average().toFloat()

        // Actualizar receta
        SupabaseManager.client.postgrest["recipes"].update(
            {
                set("average_rating", average)
                set("rating_count", ratings.size)
            }
        ) {
            filter { eq("id", recipeId) }
        }
    }

    suspend fun getUserRating(recipeId: String, userId: String): Int? {
        if (recipeId.isBlank() || userId.isBlank()) {
            return null
        }

        return try {
            SupabaseManager.client.postgrest["ratings"]
                .select {
                    filter {
                        eq("recipe_id", recipeId)
                        eq("user_id", userId)
                    }
                    limit(1)
                }
                .decodeSingleOrNull<Rating>()
                ?.rating
        } catch (e: Exception) {
            Log.e("RatingRepo", "Error getting user rating", e)
            null
        }
    }
}