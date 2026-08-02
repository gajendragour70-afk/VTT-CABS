package com.vttcabs.customer.ui.screens.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FavouritePlace(
    val id: String,
    val name: String,
    val address: String,
    val type: String, // "home", "work", "other"
    val latitude: Double,
    val longitude: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritePlacesScreen(
    places: List<FavouritePlace> = listOf(
        FavouritePlace("1", "Home", "123 MG Road, Bangalore", "home", 12.9716, 77.5946),
        FavouritePlace("2", "Work", "456 Indiranagar, Bangalore", "work", 12.9716, 77.6406),
        FavouritePlace("3", "Gym", "789 Koramangala, Bangalore", "other", 12.9352, 77.6245),
        FavouritePlace("4", "Parents", "101 Jayanagar, Bangalore", "other", 12.9250, 77.5833)
    ),
    onSelectPlace: (FavouritePlace) -> Unit = {},
    onEditPlace: (FavouritePlace) -> Unit = {},
    onDeletePlace: (String) -> Unit = {},
    onAddPlace: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf<FavouritePlace?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredPlaces = places.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.address.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favourite Places") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPlace
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Place")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search places...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            // Places List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Home and Work Section
                val homePlace = filteredPlaces.find { it.type == "home" }
                val workPlace = filteredPlaces.find { it.type == "work" }
                val otherPlaces = filteredPlaces.filter { it.type == "other" }

                if (homePlace != null) {
                    item {
                        Text(
                            text = "Quick Access",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    item {
                        PlaceCard(
                            place = homePlace,
                            icon = Icons.Default.Home,
                            onSelect = { onSelectPlace(homePlace) },
                            onEdit = { onEditPlace(homePlace) },
                            onDelete = { showDeleteDialog = homePlace }
                        )
                    }
                }

                if (workPlace != null) {
                    item {
                        PlaceCard(
                            place = workPlace,
                            icon = Icons.Default.Work,
                            onSelect = { onSelectPlace(workPlace) },
                            onEdit = { onEditPlace(workPlace) },
                            onDelete = { showDeleteDialog = workPlace }
                        )
                    }
                }

                if (otherPlaces.isNotEmpty()) {
                    item {
                        Text(
                            text = "Other Places",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(otherPlaces) { place ->
                        PlaceCard(
                            place = place,
                            icon = Icons.Default.Star,
                            onSelect = { onSelectPlace(place) },
                            onEdit = { onEditPlace(place) },
                            onDelete = { showDeleteDialog = place }
                        )
                    }
                }

                if (filteredPlaces.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.LocationOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) 
                                        "No places found" 
                                    else 
                                        "No favourite places added",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Spacer for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Delete Confirmation Dialog
    showDeleteDialog?.let { place ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Place") },
            text = { Text("Are you sure you want to remove \"${place.name}\" from your favourites?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePlace(place.id)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PlaceCard(
    place: FavouritePlace,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = place.name,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = place.address,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
