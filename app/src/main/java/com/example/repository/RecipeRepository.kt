package com.example.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GenerationConfig
import com.example.api.InlineData
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.db.SavedRecipeDao
import com.example.db.SavedRecipeEntity
import com.example.model.RecipeListResponse
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class RecipeRepository(
    private val dao: SavedRecipeDao
) {
    val savedRecipes: Flow<List<SavedRecipeEntity>> = dao.getAllSavedRecipes()

    private val moshi = Moshi.Builder().build()
    private val responseAdapter = moshi.adapter(RecipeListResponse::class.java)

    suspend fun saveRecipe(entity: SavedRecipeEntity) = withContext(Dispatchers.IO) {
        dao.saveRecipe(entity)
    }

    suspend fun deleteRecipeById(id: String) = withContext(Dispatchers.IO) {
        dao.deleteRecipeById(id)
    }

    fun isRecipeSaved(id: String): Flow<Boolean> {
        return dao.isRecipeSaved(id)
    }

    /**
     * Identifies food ingredients and generates step-by-step cooking recipes
     * across different categories (meal, drink, dessert, ice cream, cake).
     */
    suspend fun generateRecipes(
        bitmap: Bitmap?,
        inputText: String?,
        isSinhalaMode: Boolean
    ): RecipeListResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API Key is missing or unresolved. Please configure the GEMINI_API_KEY in the Secrets panel in Google AI Studio.")
        }

        // Build prompt instructions
        val languageInstruction = if (isSinhalaMode) {
            "Select and write all recipe details (name, rationale, ingredients, steps, funFact) STRICTLY in Sinhala language, while keeping the JSON property keys in English."
        } else {
            "Write all recipe details in English."
        }

        val prompt = """
            You are an expert culinary chef, world-class baker, dessert connoisseur, and drink mixologist.
            Analyze the provided ingredient input. The input can be a photo of an ingredient or food, or a text description, or both.
            
            First, identify the main ingredient (e.g., "Apple", "Banana", "Tomato", "Chicken", "Potato", etc.).
            Then, generate exactly 5 distinct, highly creative, and delicious recipes that can be made using this main ingredient.
            
            To satisfy the user requests, create exactly one recipe for each of the following 5 categories:
            1. "Meal" (e.g., a savory cooking option like Curry, Pasta, Salad, Stew)
            2. "Drink" (e.g., a beverage, juice, smoothie, tea, milkshake, secondary ingredient blend)
            3. "Dessert" (e.g., crumble, tart, pie, pudding, flan)
            4. "Ice Cream" (e.g., custom ice cream, gelato, sorbet, sherbet, popsicle)
            5. "Cake" (e.g., sponge cake, cupcake, muffin, coffee cake, pound cake)
            
            $languageInstruction
            
            Your response MUST be a single, valid JSON object matching this exact schema:
            {
              "mainIngredient": "Name of the identified ingredient (in English, e.g. Apple)",
              "recipes": [
                {
                  "id": "uniquely_generated_id_string (e.g. apple_cinnamon_cake)",
                  "name": "Recipe Name",
                  "category": "Strictly one of: Meal, Drink, Dessert, Ice Cream, Cake",
                  "difficulty": "Strictly one of: Easy, Medium, Hard",
                  "prepTime": "Prep time (e.g., 15 mins)",
                  "cookTime": "Cook time (e.g., 20 mins)",
                  "rationale": "One-sentence chef's rationale on why this main ingredient fits this recipe perfectly",
                  "ingredients": [
                    "List of other required ingredients with precise measurements"
                  ],
                  "steps": [
                    "Step 1 details",
                    "Step 2 details",
                    ...
                  ],
                  "funFact": "A fun historic fact, nutritional tip, or baking secret regarding this card"
                }
              ]
            }
            
            Ensure the JSON is strictly correctly formatted, without any markdown formatting around it (no ```json code blocks or additional comments). Just return the direct JSON raw string.
        """.trimIndent()

        // Construct parts
        val parts = mutableListOf<Part>()
        if (bitmap != null) {
            val base64Image = bitmap.toBase64()
            parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image)))
        }

        val textInput = if (!inputText.isNullOrEmpty()) {
            "The ingredients or items are: $inputText. $prompt"
        } else {
            prompt
        }
        parts.add(Part(text = textInput))

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = parts)),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.7f
            )
        )

        try {
            val rpcResponse = RetrofitClient.service.generateContent(apiKey, request)
            val jsonText = rpcResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IllegalStateException("Gemini returned an empty response. Please try again.")

            Log.d("RecipeRepository", "Raw JSON response: $jsonText")

            val cleanedJson = cleanMarkdown(jsonText)
            val responseObj = responseAdapter.fromJson(cleanedJson)
                ?: throw IllegalStateException("Failed to parse the culinary list. Ensure the AI returned details in correct schema.")

            // Ensure we have exactly 5 items or whatever is returned
            if (responseObj.recipes.isEmpty()) {
                throw IllegalStateException("No recipes were formed. Let's try analyzing the picture once more.")
            }

            responseObj
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Failed calling Gemini", e)
            throw e
        }
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun cleanMarkdown(json: String): String {
        var clean = json.trim()
        if (clean.startsWith("```json")) {
            clean = clean.substring(7)
        } else if (clean.startsWith("```")) {
            clean = clean.substring(3)
        }
        if (clean.endsWith("```")) {
            clean = clean.substring(0, clean.length - 3)
        }
        return clean.trim()
    }
}
