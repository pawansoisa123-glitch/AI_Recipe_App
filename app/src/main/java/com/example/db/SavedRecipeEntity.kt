package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_recipes")
data class SavedRecipeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val difficulty: String,
    val prepTime: String,
    val cookTime: String,
    val rationale: String,
    val mainIngredient: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val funFact: String,
    val savedAt: Long = System.currentTimeMillis()
)
