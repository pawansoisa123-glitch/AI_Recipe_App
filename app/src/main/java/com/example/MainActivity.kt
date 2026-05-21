package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.db.AppDatabase
import com.example.repository.RecipeRepository
import com.example.ui.RecipeAppUi
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.RecipeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Core Room Database and Repository Pattern
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = RecipeRepository(database.savedRecipeDao())
        
        // Construct the State ViewModel with Custom Factory Pattern
        val viewModel = ViewModelProvider(
            this, 
            RecipeViewModel.Factory(repository)
        )[RecipeViewModel::class.java]
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RecipeAppUi(viewModel = viewModel)
                }
            }
        }
    }
}
