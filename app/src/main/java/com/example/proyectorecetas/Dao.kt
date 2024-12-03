package com.example.proyectorecetas

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface Dao {

    @Query("SELECT * FROM recipe LIMIT 5")
    fun getPopularRecipes(): List<Recipe>

    @Query("SELECT * FROM recipe")
    fun getAllRecipes(): List<Recipe>

    @Insert
    fun insertRecipe(recipe: Recipe)
}