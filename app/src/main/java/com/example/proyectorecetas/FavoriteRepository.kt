package com.example.proyectorecetas

import android.util.Log
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement


class FavoriteRepository {

    suspend fun addFavorite(userId: String, recipeId: String){
        try {
            SupabaseManager.client.postgrest["favorites"].insert(
                Favorite(
                    user_id=userId,
                    recipe_id=recipeId
                )
            )
        }catch (e:Exception){
            Log.e("FavoriteRepository", "Error adding favorite", e)
        }
    }

    suspend fun removeFavorite(userId: String, recipeId: String){
        try{
            SupabaseManager.client.postgrest["favorites"].delete {
                filter {
                    eq("user_id", userId)
                    eq("recipe_id", recipeId)
                }
            }
        }catch (e:Exception){
            Log.e("FavoriteRepository", "Error removing favorite", e)
        }
    }

    suspend fun isFavorite(userId: String, recipeId: String): Boolean{
        if (userId.isBlank() || recipeId.isBlank()) {
            return false
        }

        return try {
            val count = SupabaseManager.client.from("favorites")
                .select {
                    count(Count.EXACT)
                    filter {
                        eq("user_id", userId)
                        eq("recipe_id", recipeId)
                    }
                }.countOrNull()
            count!! > 0
        } catch (e: Exception) {
            Log.e("FavoriteRepo", "Error checking favorite", e)
            false
        }
    }

    suspend fun getUserFavorites(userId: String): List<Recipe> {
        return try {
            val response: PostgrestResult = SupabaseManager.client.from("favorites")
                .select(columns = Columns.list("recipes (*)")) {
                    filter {
                        eq("user_id", userId)
                    }
                }
            val rawList = response.decodeList<JsonObject>()

            rawList.mapNotNull { json ->
                json["recipes"]?.let { recipeElement ->
                    Json.decodeFromJsonElement<Recipe>(recipeElement)
                }
            }

        } catch (e: Exception) {
            Log.e("FavoriteRepository", "Error getting user favorites", e)
            emptyList()
        }
    }
}