package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Recipe(
    val id: String,
    val name: String,
    val category: String, // e.g., "Meal", "Drink", "Dessert", "Ice Cream", "Cake"
    val difficulty: String, // "Easy", "Medium", "Hard"
    val prepTime: String,
    val cookTime: String,
    val rationale: String, // Why this is great for this ingredient
    val ingredients: List<String>, // Other ingredients needed
    val steps: List<String>, // Step by step instructions
    val funFact: String // Brief advice or fun fact
)

@JsonClass(generateAdapter = true)
data class RecipeListResponse(
    val mainIngredient: String,
    val recipes: List<Recipe>
)
