package com.example.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.db.SavedRecipeEntity
import com.example.model.Recipe
import com.example.viewmodel.ActiveScreen
import com.example.viewmodel.GenerateState
import com.example.viewmodel.RecipeViewModel

// Preset Ingredient Data Struct
data class PresetIngredient(
    val name: String,
    val imageUrl: String,
    val iconEmoji: String,
    val sinhalaName: String
)

val PRESET_INGREDIENTS = listOf(
    PresetIngredient(
        "Apple",
        "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600",
        "🍎",
        "ඇපල්"
    ),
    PresetIngredient(
        "Banana",
        "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=600",
        "🍌",
        "කෙසෙල්"
    ),
    PresetIngredient(
        "Chocolate",
        "https://images.unsplash.com/photo-1511381939415-e44015466834?w=600",
        "🍫",
        "චොකලට්"
    ),
    PresetIngredient(
        "Tomato",
        "https://images.unsplash.com/photo-1595855759920-86582396756a?w=600",
        "🍅",
        "තක්කාලි"
    ),
    PresetIngredient(
        "Chicken",
        "https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=600",
        "🍗",
        "කුකුල් මස්"
    ),
    PresetIngredient(
        "Avocado",
        "https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?w=600",
        "🥑",
        "අලිගැටපේර"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeAppUi(
    viewModel: RecipeViewModel,
    modifier: Modifier = Modifier
) {
    val activeScreen by viewModel.activeScreen.collectAsStateWithLifecycle()
    val generateState by viewModel.generateState.collectAsStateWithLifecycle()
    val isSinhalaMode by viewModel.isSinhalaMode.collectAsStateWithLifecycle()
    val selectedRecipe by viewModel.selectedRecipe.collectAsStateWithLifecycle()
    val currentMainIngredient by viewModel.currentMainIngredient.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = "App Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = if (isSinhalaMode) "කුස්සියේ මායාකාරයා" else "Ingredient Match",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    // Bilingual Mode Toggle Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable { viewModel.toggleSinhalaMode() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("language_toggle")
                    ) {
                        Text(
                            text = if (isSinhalaMode) "සිංහල 🇱🇰" else "English 🇬🇧",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Switch Language",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                navigationIcon = {
                    if (activeScreen != ActiveScreen.HOME) {
                        IconButton(
                            onClick = { viewModel.navigateBack() },
                            modifier = Modifier.testTag("back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (activeScreen != ActiveScreen.RECIPE_DETAILS) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("bottom_nav")
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.SoupKitchen, contentDescription = "Home") },
                        label = { Text(if (isSinhalaMode) "ප්‍රධාන" else "Find Recipes") },
                        selected = activeScreen == ActiveScreen.HOME,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = Color(0xFFDDE5C9), // Beautiful Active sage/taupe pill
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ),
                        onClick = { viewModel.navigateTo(ActiveScreen.HOME) },
                        modifier = Modifier.testTag("nav_home_btn")
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Book, contentDescription = "Saved Cookbook") },
                        label = { Text(if (isSinhalaMode) "මගේ පොත" else "Cookbook") },
                        selected = activeScreen == ActiveScreen.SAVED_RECIPES,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = Color(0xFFDDE5C9), // Beautiful Active sage/taupe pill
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ),
                        onClick = { viewModel.navigateTo(ActiveScreen.SAVED_RECIPES) },
                        modifier = Modifier.testTag("nav_cookbook_btn")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeScreen) {
                ActiveScreen.HOME -> HomeScreen(viewModel, generateState, isSinhalaMode)
                ActiveScreen.SAVED_RECIPES -> SavedRecipesScreen(viewModel, isSinhalaMode)
                ActiveScreen.RECIPE_DETAILS -> {
                    selectedRecipe?.let { recipe ->
                        RecipeDetailsScreen(
                            recipe = recipe,
                            mainIngredient = currentMainIngredient,
                            isSaved = viewModel.isSaved(recipe.id),
                            onToggleSave = { viewModel.toggleSaveRecipe(recipe, currentMainIngredient) },
                            isSinhalaMode = isSinhalaMode
                        )
                    } ?: Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isSinhalaMode) "කරුණාකර නැවත උත්සාහ කරන්න." else "Recipe not found.")
                    }
                }
            }
        }
    }
}

// --- Home Screen ---

@Composable
fun HomeScreen(
    viewModel: RecipeViewModel,
    generateState: GenerateState,
    isSinhalaMode: Boolean
) {
    val context = LocalContext.current
    val typedIngredient by viewModel.typedIngredient.collectAsStateWithLifecycle()
    val selectedImageBitmap by viewModel.selectedImageBitmap.collectAsStateWithLifecycle()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateSelectedImageUri(context, it) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = if (isSinhalaMode) "ඔබේ කෑම මේසය හැඩකරන්න" else "Culinary Magic!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isSinhalaMode) {
                            "ඇපල් ගෙඩියක්, කෙසෙල් ගෙඩියක් හෝ ඕනෑම කෑමක් ඡායාරූප ගත කරන්න. එමඟින් සාදා ගත හැකි කෑම, බීම, කේක්, අයිස්ක්‍රීම් සහ රසකැවිලි වට්ටෝරු පියවරෙන් පියව ලැබෙනු ඇත."
                        } else {
                            "Snap a photo of any ingredient (like an apple) to find all possible recipes, drinks, desserts, ice creams, and cakes you can craft instantly!"
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Image Selection Area and Custom text query input
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isSinhalaMode) "ඡායාරූපයක් එක් කරන්න" else "Upload or Take Ingredient Photo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Photo box button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { filePickerLauncher.launch("image/*") }
                        .testTag("image_picker_area"),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageBitmap != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                bitmap = selectedImageBitmap!!.asImageBitmap(),
                                contentDescription = "Active upload image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Dismiss icon button
                            IconButton(
                                onClick = { viewModel.clearCustomImage() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear image",
                                    tint = Color.White
                                )
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload Cloud Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = if (isSinhalaMode) "ඡායාරූපයක් තෝරා ගැනීමට මෙතන ක්ලික් කරන්න" else "Select Food Image from Gallery",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "JPEG, PNG supported",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Optional text descriptor input field
                OutlinedTextField(
                    value = typedIngredient,
                    onValueChange = { viewModel.setTypedIngredient(it) },
                    label = { Text(if (isSinhalaMode) "අතිරේක තොරතුරු (उदा: 'ඇපල් සහ චොක්ලට්')" else "Ingredients / Details (e.g., 'Apple with nuts')") },
                    placeholder = { Text(if (isSinhalaMode) "ළඟ ඇති වෙනත් දේවල්" else "e.g., Apple, nuts, milk") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ingredient_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Button(
                    onClick = { viewModel.generateRecipes() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("generate_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Analyze")
                        Text(
                            text = if (isSinhalaMode) "වට්ටෝරු සාදන්න ✨" else "Generate Recipes ✨",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Preset Quick Start Section
        item {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = if (isSinhalaMode) "ඉක්මන් පියවර - ද්‍රව්‍යයක් තෝරන්න" else "Quick Scan - Popular Presets",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(210.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    userScrollEnabled = false
                ) {
                    items(PRESET_INGREDIENTS) { preset ->
                        PresetCard(preset, isSinhalaMode) {
                            viewModel.generateForPreset(preset.name)
                        }
                    }
                }
            }
        }

        // Output Status Indicator and Recipes list
        item {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Handle states
        when (generateState) {
            is GenerateState.Idle -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dining,
                            contentDescription = "Dining icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isSinhalaMode) "සුවඳවත් වට්ටෝරුවක් සාදන්න සූදානම් කරන්න..." else "Cooking stove is ready! Upload or tap a preset to begin.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            is GenerateState.Loading -> {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp
                        )
                        Icon(
                            imageVector = Icons.Default.SoupKitchen,
                            contentDescription = "Cooking progress",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = if (isSinhalaMode) "මීළඟ වට්ටෝරු සූදානම් කෙරෙමින් පවතී..." else "Master chef is analyzing ingredient culinary potential...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (isSinhalaMode) "කරුණාකර මොහොතක් රැඳී සිටින්න. (තත්පර 15 - 30ක් ගත හැක)" else "Baking, churning, and filtering recipes... (Takes 15-30s)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            is GenerateState.Success -> {
                val data = generateState.response
                item {
                    Text(
                        text = (if (isSinhalaMode) "හඳුනාගත් ප්‍රධාන ද්‍රව්‍යය: " else "Main Ingredient Identified: ") + data.mainIngredient,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(data.recipes) { recipe ->
                    RecipeItemCard(recipe, data.mainIngredient, isSinhalaMode) {
                        viewModel.showRecipeDetails(recipe, data.mainIngredient)
                    }
                }
            }
            is GenerateState.Error -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error Logo",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = if (isSinhalaMode) "යා කිරීමේ දෝෂයක්!" else "API Connection Issue!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = generateState.message,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (isSinhalaMode) {
                                    "ප්‍රයෝජනවත් උපදෙස: Google AI Studio හි Secrets panel එකට ගොස් ඔබේ GEMINI_API_KEY එක නිවැරදිව ඇතුළත් කර ඇත්දැයි පරීක්ෂා කර බලා නැවත උත්සාහ කරන්න."
                                } else {
                                    "Tip: Ensure you have added your GEMINI_API_KEY inside the Secrets panel in AI Studio. Android APK builds require active keys for direct REST capabilities."
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Preset Ingredient Card ---

@Composable
fun PresetCard(
    preset: PresetIngredient,
    isSinhalaMode: Boolean,
    onClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() }
            .testTag("preset_${preset.name.lowercase()}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
            ) {
                AsyncImage(
                    model = preset.imageUrl,
                    contentDescription = preset.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isSinhalaMode) preset.sinhalaName else preset.name,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = preset.iconEmoji,
                fontSize = 14.sp
            )
        }
    }
}

// --- Recipe Feed List Card ---

@Composable
fun RecipeItemCard(
    recipe: Recipe,
    mainIngredient: String,
    isSinhalaMode: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("recipe_item_${recipe.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category graphic visualizer
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(getCategoryColor(recipe.category).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(recipe.category),
                    contentDescription = recipe.category,
                    tint = getCategoryColor(recipe.category),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Category small badge
                Text(
                    text = recipe.category.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = getCategoryColor(recipe.category)
                )

                Text(
                    text = recipe.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Cook Time",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = recipe.cookTime,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SignalCellularAlt,
                            contentDescription = "Difficulty",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = recipe.difficulty,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// --- Recipe Details Screen ---

@Composable
fun RecipeDetailsScreen(
    recipe: Recipe,
    mainIngredient: String,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    isSinhalaMode: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Panel
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = getCategoryColor(recipe.category).copy(alpha = 0.08f)
                ),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, getCategoryColor(recipe.category).copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category tag
                        SuggestionChip(
                            onClick = {},
                            label = { Text(recipe.category.uppercase()) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                labelColor = getCategoryColor(recipe.category)
                            ),
                            border = BorderStroke(1.dp, getCategoryColor(recipe.category))
                        )

                        // Favorite Bookmark button
                        IconButton(
                            onClick = { onToggleSave() },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .size(40.dp)
                                .testTag("favorite_button")
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Favorite Recipe",
                                tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = recipe.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Description Rationale
                    Text(
                        text = recipe.rationale,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Recipe Metadata Cards Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isSinhalaMode) "පෙර සූදානම" else "Prep Time",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = recipe.prepTime,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isSinhalaMode) "පිසීමේ කාලය" else "Cook Time",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = recipe.cookTime,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isSinhalaMode) "අසීරුතාවය" else "Difficulty",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = recipe.difficulty,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 1: Ingredients Needed (besides the main ingredient)
        item {
            Text(
                text = if (isSinhalaMode) "අවශ්‍ය අනෙකුත් ද්‍රව්‍යයන්" else "Other Ingredients Required",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = (if (isSinhalaMode) "ප්‍රධාන ද්‍රව්‍යය වන " else "Add these in addition to your ") + "($mainIngredient)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(recipe.ingredients) { ingredient ->
            var checkedByItem by remember { mutableStateOf(false) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (checkedByItem) Color(0xFFE4E9D6) else Color(0xFFF7F9EF)
                    )
                    .border(
                        1.dp,
                        if (checkedByItem) Color(0xFFC8D1B3) else Color(0xFFE4E9D6),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { checkedByItem = !checkedByItem }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Checkbox(
                    checked = checkedByItem,
                    onCheckedChange = { checkedByItem = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = ingredient,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (checkedByItem) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Section 2: Step-by-Step Directions
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isSinhalaMode) "සාදන ආකාරය පියවරෙන් පියවර" else "Step-by-Step Directions",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(recipe.steps.zip(1..recipe.steps.size)) { (stepText, index) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        BorderStroke(1.dp, Color(0xFFE4E2D3)),
                        RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp)
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(38.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isSinhalaMode) "පියවර $index" else "Step $index of ${recipe.steps.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stepText,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Section 3: Fun Fact / Tips callout card
        item {
            if (recipe.funFact.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Tips",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                               text = if (isSinhalaMode) "ඵලදායී උපදෙසක් / රසවත් කරුණක්" else "Chef's Baking Secret",
                               fontWeight = FontWeight.Bold,
                               fontSize = 14.sp,
                               color = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = recipe.funFact,
                                fontSize = 13.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- Cookbook Offline Saved Screen ---

@Composable
fun SavedRecipesScreen(
    viewModel: RecipeViewModel,
    isSinhalaMode: Boolean
) {
    val savedRecipes by viewModel.savedRecipes.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Welcome Header
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (isSinhalaMode) "මගේ සුරැකි වට්ටෝරු පොත" else "My Saved Cookbook 📖",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isSinhalaMode) {
                        "ඔබ තරු ලකුණු කර සුරැකි සියලුම වට්ටෝරුවන් නොබැඳිව (offline) මෙහිදී නිදහසේ කියවීමට නැවත ලබාගත හැක."
                    } else {
                        "All recipe cards you bookmark are securely cached in your local SQLite and available 100% offline!"
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (savedRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Empty Cookbook",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(72.dp)
                    )
                    Text(
                        text = if (isSinhalaMode) "ඔබේ සුරැකි පොත හිස්ව පවතී" else "Your cookbook is empty",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isSinhalaMode) "වට්ටෝරුවක් සාදා එහි ඇති Bookmark බොත්තම ක්ලික් කරන්න." else "Bookmark a generated card from details screen and it will pop up here.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(savedRecipes) { entity ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.showSavedRecipeDetails(entity) }
                            .testTag("saved_recipe_item_${entity.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(getCategoryColor(entity.category).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(entity.category),
                                    contentDescription = entity.category,
                                    tint = getCategoryColor(entity.category),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${entity.category.uppercase()} • Made from ${entity.mainIngredient}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = getCategoryColor(entity.category)
                                )
                                Text(
                                    text = entity.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = { viewModel.toggleSaveRecipe(
                                    recipe = Recipe(
                                        id = entity.id,
                                        name = entity.name,
                                        category = entity.category,
                                        difficulty = entity.difficulty,
                                        prepTime = entity.prepTime,
                                        cookTime = entity.cookTime,
                                        rationale = entity.rationale,
                                        ingredients = entity.ingredients,
                                        steps = entity.steps,
                                        funFact = entity.funFact
                                    ),
                                    mainIngredient = entity.mainIngredient
                                ) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Category Helpers ---

fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category.lowercase()) {
        "meal" -> Icons.Default.Restaurant
        "drink" -> Icons.Default.LocalDrink
        "dessert" -> Icons.Default.Icecream
        "ice cream" -> Icons.Default.Icecream
        "cake" -> Icons.Default.Cake
        else -> Icons.Default.Dining
    }
}

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "meal" -> Color(0xFF7D5F3D) // Warm clay / Earthy terracotta
        "drink" -> Color(0xFF55624C) // Deep sage / Olive
        "dessert" -> Color(0xFFB48A45) // Warm amber / Honey
        "ice cream" -> Color(0xFF918671) // Toasted Taupe
        "cake" -> Color(0xFF8E7558) // Warm Cocoa
        else -> Color(0xFF55624C) // Natural Sage
    }
}
