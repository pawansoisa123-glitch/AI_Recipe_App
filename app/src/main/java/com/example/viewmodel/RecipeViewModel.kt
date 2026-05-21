package com.example.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.db.SavedRecipeEntity
import com.example.model.Recipe
import com.example.model.RecipeListResponse
import com.example.repository.RecipeRepository
import com.example.utils.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface GenerateState {
    object Idle : GenerateState
    object Loading : GenerateState
    data class Success(val response: RecipeListResponse) : GenerateState
    data class Error(val message: String) : GenerateState
}

enum class ActiveScreen {
    HOME,
    SAVED_RECIPES,
    RECIPE_DETAILS
}

class RecipeViewModel(
    private val repository: RecipeRepository
) : ViewModel() {

    // UI View State
    private val _generateState = MutableStateFlow<GenerateState>(GenerateState.Idle)
    val generateState: StateFlow<GenerateState> = _generateState.asStateFlow()

    // Navigation and screen management
    private val _activeScreen = MutableStateFlow(ActiveScreen.HOME)
    val activeScreen: StateFlow<ActiveScreen> = _activeScreen.asStateFlow()

    // Selected Recipe for single detail view
    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()

    // Selected main ingredient from active success list or view state
    private val _currentMainIngredient = MutableStateFlow("")
    val currentMainIngredient: StateFlow<String> = _currentMainIngredient.asStateFlow()

    // Language setting (Togglable between English and Sinhala outputs)
    private val _isSinhalaMode = MutableStateFlow(false)
    val isSinhalaMode: StateFlow<Boolean> = _isSinhalaMode.asStateFlow()

    // Observed from Local Room database
    val savedRecipes: StateFlow<List<SavedRecipeEntity>> = repository.savedRecipes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current custom selected image bitmap for visual display
    private val _selectedImageBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedImageBitmap: StateFlow<Bitmap?> = _selectedImageBitmap.asStateFlow()

    // Active ingredient name for custom analysis
    private val _typedIngredient = MutableStateFlow("")
    val typedIngredient: StateFlow<String> = _typedIngredient.asStateFlow()

    fun toggleSinhalaMode() {
        _isSinhalaMode.value = !_isSinhalaMode.value
    }

    fun setTypedIngredient(text: String) {
        _typedIngredient.value = text
    }

    fun updateSelectedImageUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val bitmap = ImageUtils.uriToBitmap(context, uri)
            _selectedImageBitmap.value = bitmap
        }
    }

    fun clearCustomImage() {
        _selectedImageBitmap.value = null
    }

    fun navigateTo(screen: ActiveScreen) {
        _activeScreen.value = screen
    }

    fun showRecipeDetails(recipe: Recipe, mainIngredient: String) {
        _selectedRecipe.value = recipe
        _currentMainIngredient.value = mainIngredient
        _activeScreen.value = ActiveScreen.RECIPE_DETAILS
    }

    fun showSavedRecipeDetails(saved: SavedRecipeEntity) {
        val recipe = Recipe(
            id = saved.id,
            name = saved.name,
            category = saved.category,
            difficulty = saved.difficulty,
            prepTime = saved.prepTime,
            cookTime = saved.cookTime,
            rationale = saved.rationale,
            ingredients = saved.ingredients,
            steps = saved.steps,
            funFact = saved.funFact
        )
        _selectedRecipe.value = recipe
        _currentMainIngredient.value = saved.mainIngredient
        _activeScreen.value = ActiveScreen.RECIPE_DETAILS
    }

    fun navigateBack() {
        when (_activeScreen.value) {
            ActiveScreen.RECIPE_DETAILS -> {
                _activeScreen.value = ActiveScreen.HOME
            }
            ActiveScreen.SAVED_RECIPES -> {
                _activeScreen.value = ActiveScreen.HOME
            }
            else -> {}
        }
    }

    fun generateForPreset(name: String) {
        _generateState.value = GenerateState.Loading
        _selectedImageBitmap.value = null // clear any custom images
        _typedIngredient.value = ""
        viewModelScope.launch {
            try {
                val result = repository.generateRecipes(
                    bitmap = null,
                    inputText = "Preset focus ingredient: $name",
                    isSinhalaMode = _isSinhalaMode.value
                )
                _generateState.value = GenerateState.Success(result)
            } catch (e: Exception) {
                _generateState.value = GenerateState.Error(e.localizedMessage ?: "Failed to cook up recommendations.")
            }
        }
    }

    fun generateRecipes() {
        val bitmap = _selectedImageBitmap.value
        val text = _typedIngredient.value.trim()

        if (bitmap == null && text.isEmpty()) {
            _generateState.value = GenerateState.Error("Please select a preset, type an ingredient, or upload an image first.")
            return
        }

        _generateState.value = GenerateState.Loading
        viewModelScope.launch {
            try {
                val promptText = if (text.isNotEmpty()) text else null
                val result = repository.generateRecipes(bitmap, promptText, _isSinhalaMode.value)
                _generateState.value = GenerateState.Success(result)
            } catch (e: Exception) {
                _generateState.value = GenerateState.Error(e.localizedMessage ?: "Could not fetch details. Please verify your internet connection or API settings.")
            }
        }
    }

    fun isSaved(id: String): Boolean {
        return savedRecipes.value.any { it.id == id }
    }

    fun toggleSaveRecipe(recipe: Recipe, mainIngredient: String) {
        viewModelScope.launch {
            val alreadySaved = isSaved(recipe.id)
            if (alreadySaved) {
                repository.deleteRecipeById(recipe.id)
            } else {
                val entity = SavedRecipeEntity(
                    id = recipe.id,
                    name = recipe.name,
                    category = recipe.category,
                    difficulty = recipe.difficulty,
                    prepTime = recipe.prepTime,
                    cookTime = recipe.cookTime,
                    rationale = recipe.rationale,
                    mainIngredient = mainIngredient,
                    ingredients = recipe.ingredients,
                    steps = recipe.steps,
                    funFact = recipe.funFact
                )
                repository.saveRecipe(entity)
            }
        }
    }

    class Factory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RecipeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
