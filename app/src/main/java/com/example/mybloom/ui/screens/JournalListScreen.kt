package com.example.mybloom.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.mybloom.R
import com.example.mybloom.config.ThemeManager
import com.example.mybloom.entities.DiscoveryEntity
import com.example.mybloom.ui.theme.PlantdiscoveryTheme
import com.example.mybloom.viewmodel.JournalViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalListScreen(
    viewModel: JournalViewModel,
    onAddClick: () -> Unit,
    onCardClick: (Int) -> Unit,
    onSignOut: () -> Unit,
    onThemeChange: (String) -> Unit = {} // Callback pour changer le thème
) {
    val discoveries by viewModel.discoveries.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredDiscoveries by viewModel.filteredDiscoveries.collectAsStateWithLifecycle()

    // User info
    val currentUser = FirebaseAuth.getInstance().currentUser
    val displayName = currentUser?.displayName ?: "Plant Lover"
    val email = currentUser?.email ?: ""
    var showMenu by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearchBar) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.searchDiscoveries(it) },
                            onClose = {
                                showSearchBar = false
                                viewModel.clearSearch()
                            }
                        )
                    } else {
                        Column {
                            Text(
                                stringResource(R.string.my_garden),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.plants_discovered, discoveries.size),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Search button
                    if (!showSearchBar && discoveries.isNotEmpty()) {
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.search),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Profile menu
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = stringResource(R.string.profile),
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // User info
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .widthIn(min = 200.dp)
                            ) {
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            HorizontalDivider()

                            // Theme selector
                            var showThemeDialog by remember { mutableStateOf(false) }

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Palette,
                                            contentDescription = "Theme"
                                        )
                                        Text(stringResource(R.string.theme))
                                    }
                                },
                                onClick = {
                                    showThemeDialog = true
                                }
                            )

                            HorizontalDivider()

                            // Sign out
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
                                            contentDescription = stringResource(R.string.sign_out),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            stringResource(R.string.sign_out),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onSignOut()
                                }
                            )

                            // Theme selection dialog
                            if (showThemeDialog) {
                                ThemeSelectionDialog(
                                    onDismiss = { showThemeDialog = false },
                                    onThemeSelected = { themeCode ->
                                        onThemeChange(themeCode)
                                        showThemeDialog = false
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                modifier = Modifier.padding(bottom = 16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                icon = {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add),
                        modifier = Modifier.size(24.dp)
                    )
                },
                text = {
                    Text(
                        stringResource(R.string.add_plant_button),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        val displayList = if (searchQuery.isNotBlank()) filteredDiscoveries else discoveries

        AnimatedContent(
            targetState = displayList.isEmpty(),
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "content_transition"
        ) { isEmpty ->
            if (isEmpty) {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(32.dp),
                    isSearching = searchQuery.isNotBlank()
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = padding.calculateTopPadding() + 16.dp,
                        bottom = padding.calculateBottomPadding() + 100.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayList, key = { it.id }) { discovery ->
                        PlantCard(
                            discovery = discovery,
                            onClick = { onCardClick(discovery.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeSelectionDialog(
    onDismiss: () -> Unit,
    onThemeSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val currentTheme = remember { ThemeManager.getTheme(context) }
    var selectedTheme by remember { mutableStateOf(currentTheme) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.choose_theme)) },
        text = {
            Column {
                ThemeManager.getAvailableThemes().forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedTheme = theme.code
                                ThemeManager.setTheme(context, theme.code)
                                onThemeSelected(theme.code)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTheme == theme.code,
                            onClick = {
                                selectedTheme = theme.code
                                ThemeManager.setTheme(context, theme.code)
                                onThemeSelected(theme.code)
                            }
                        )
                        Spacer(Modifier.width(12.dp))
                        Icon(
                            imageVector = theme.icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = theme.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    isSearching: Boolean = false
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSearching) Icons.Default.SearchOff else Icons.Default.Spa,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(if (isSearching) R.string.no_plants_found else R.string.no_plants_yet),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(if (isSearching) R.string.try_different_keywords else R.string.start_your_garden),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PlantCard(
    discovery: DiscoveryEntity,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(140.dp)
            ) {
                val imageFile = File(discovery.localImagePath)
                if (imageFile.exists()) {
                    Image(
                        painter = rememberAsyncImagePainter(imageFile),
                        contentDescription = discovery.plantName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                )
                            )
                        )
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            stringResource(R.string.ai_badge),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = discovery.plantName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                if (discovery.aiFact.isNotBlank()) {
                    Text(
                        text = discovery.aiFact,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            .format(discovery.timestamp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.view_details),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 8.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ========== PREVIEWS ==========

@Preview(name = "Light Mode - With Plants", showBackground = true)
@Composable
private fun JournalListScreenPreviewLight() {
    PlantdiscoveryTheme(darkTheme = false) {
        PlantCard(
            discovery = DiscoveryEntity(
                id = 1,
                userId = "preview_user",
                plantName = "Monstera Deliciosa",
                aiFact = "This tropical plant is known for its large, perforated leaves that develop naturally as it matures.",
                localImagePath = "",
                timestamp = System.currentTimeMillis()
            ),
            onClick = {}
        )
    }
}

@Preview(name = "Dark Mode - Card", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PlantCardPreviewDark() {
    PlantdiscoveryTheme(darkTheme = true) {
        PlantCard(
            discovery = DiscoveryEntity(
                id = 1,
                userId = "preview_user",
                plantName = "Snake Plant",
                aiFact = "One of the best air-purifying plants according to NASA. Releases oxygen at night!",
                localImagePath = "",
                timestamp = System.currentTimeMillis()
            ),
            onClick = {}
        )
    }
}

@Preview(name = "Empty State", showBackground = true)
@Composable
private fun EmptyStatePreview() {
    PlantdiscoveryTheme {
        EmptyState(modifier = Modifier.fillMaxSize())
    }
}

@Preview(name = "Search Empty State", showBackground = true)
@Composable
private fun SearchEmptyStatePreview() {
    PlantdiscoveryTheme {
        EmptyState(
            modifier = Modifier.fillMaxSize(),
            isSearching = true
        )
    }
}
