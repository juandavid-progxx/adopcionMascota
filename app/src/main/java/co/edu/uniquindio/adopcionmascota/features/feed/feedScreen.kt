package co.edu.uniquindio.adopcionmascota.features.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FeedScreen(
    viewModel: FeedScreenViewModel = viewModel(),
    onCreatePostClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    // Si hay una mascota seleccionada, mostramos la pantalla de Detalle
    val selectedPet = state.selectedPetForDetail
    if (selectedPet != null) {
        PetDetailScreen(
            pet = selectedPet,
            onBack = { viewModel.onCloseDetail() },
            onAdoptClick = { viewModel.onAdoptClick(it) },
            onToggleFavorite = { viewModel.onToggleFavorite(it) }
        )
        return
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedItem = state.selectedBottomNavItem,
                onItemSelected = { viewModel.onBottomNavItemSelected(it) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onFabClick()
                    onCreatePostClick()
                },
                containerColor = Color(0xFF00BFA5),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(56.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Publicar mascota",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF4F7F5))
        ) {
            // 1. Barra superior de búsqueda
            SearchBarHeader(
                searchQuery = state.searchQuery,
                onSearchChange = { viewModel.onSearchQueryChange(it) }
            )

            // 2. Filtros por tipo de mascota
            PetTypeFilterRow(
                selectedType = state.selectedPetType,
                onTypeSelected = { viewModel.onPetTypeSelected(it) }
            )

            // 3. Filtros por ubicación
            LocationFilterRow(
                selectedLocation = state.selectedLocation,
                onLocationSelected = { viewModel.onLocationSelected(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Selector de visualización (Lista / Mapa)
            ViewModeSegmentedControl(
                currentMode = state.viewMode,
                onModeSelected = { viewModel.onViewModeSelected(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Feed de mascotas o Vista de mapa
            if (state.viewMode == ViewMode.LIST) {
                if (state.filteredPets.isEmpty()) {
                    EmptyPetsView()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        items(
                            items = state.filteredPets,
                            key = { it.id }
                        ) { pet ->
                            PetCardItem(
                                pet = pet,
                                onPetClick = { viewModel.onPetSelectedForDetail(pet) },
                                onAdoptClick = { viewModel.onAdoptClick(pet) },
                                onToggleFavorite = { viewModel.onToggleFavorite(pet) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            } else {
                MapViewSimulated(
                    pets = state.filteredPets,
                    onPetClick = { viewModel.onPetSelectedForDetail(it) }
                )
            }
        }
    }

    // Diálogo de confirmación para adopción
    state.adoptionDialogPet?.let { pet ->
        AlertDialog(
            onDismissRequest = { viewModel.onDismissAdoptionDialog() },
            title = {
                Text(
                    text = "Adoptar a ${pet.name}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004D40)
                )
            },
            text = {
                Text(
                    text = "¿Deseas iniciar el proceso de adopción para ${pet.name}? Nos pondremos en contacto con el refugio '${pet.shelterName}' para coordinar la visita.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConfirmAdoption(pet) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5))
                ) {
                    Text("Confirmar Adopción", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissAdoptionDialog() }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    // Mensaje de éxito post-adopción
    state.adoptionSuccessMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.onDismissSuccessMessage() },
            title = {
                Text(
                    text = "🎉 ¡Felicidades!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
            },
            text = { Text(text = message) },
            confirmButton = {
                Button(
                    onClick = { viewModel.onDismissSuccessMessage() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5))
                ) {
                    Text("Entendido", color = Color.White)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

// -------------------------------------------------------------------
// Componentes UI Reutilizables
// -------------------------------------------------------------------

@Composable
private fun SearchBarHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            placeholder = {
                Text(
                    text = "Buscar mascotas en adopción...",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = Color(0xFF004D40)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpiar",
                            tint = Color.Gray
                        )
                    }
                }
            },
            singleLine = true,
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE8F0EC),
                unfocusedContainerColor = Color(0xFFE8F0EC),
                disabledContainerColor = Color(0xFFE8F0EC),
                focusedBorderColor = Color(0xFF80CBC4),
                unfocusedBorderColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color(0xFF004D40))
                .clickable { /* Abrir opciones de filtro avanzado */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filtros avanzables",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PetTypeFilterRow(
    selectedType: PetType,
    onTypeSelected: (PetType) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
    ) {
        items(PetType.values()) { type ->
            val isSelected = type == selectedType
            val backgroundColor = if (isSelected) Color(0xFFB2DFDB) else Color.White
            val contentColor = if (isSelected) Color(0xFF004D40) else Color.DarkGray
            val borderColor = if (isSelected) Color(0xFF80CBC4) else Color(0xFFE0E0E0)

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onTypeSelected(type) }
                    .border(1.dp, borderColor, RoundedCornerShape(20.dp)),
                color = backgroundColor,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = type.emoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = type.displayName,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationFilterRow(
    selectedLocation: LocationFilter,
    onLocationSelected: (LocationFilter) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
    ) {
        items(LocationFilter.values()) { location ->
            val isSelected = location == selectedLocation
            val backgroundColor = if (isSelected) Color(0xFFB2DFDB) else Color.White
            val contentColor = if (isSelected) Color(0xFF004D40) else Color.DarkGray
            val borderColor = if (isSelected) Color(0xFF80CBC4) else Color(0xFFE0E0E0)

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onLocationSelected(location) }
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = location.iconText, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = location.displayName,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewModeSegmentedControl(
    currentMode: ViewMode,
    onModeSelected: (ViewMode) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(220.dp)
                .height(42.dp),
            shape = CircleShape,
            color = Color(0xFFE8F0EC)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lista
                val isListSelected = currentMode == ViewMode.LIST
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(if (isListSelected) Color.White else Color.Transparent)
                        .clickable { onModeSelected(ViewMode.LIST) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📋 Lista",
                        fontSize = 13.sp,
                        fontWeight = if (isListSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isListSelected) Color(0xFF004D40) else Color.Gray
                    )
                }

                // Mapa
                val isMapSelected = currentMode == ViewMode.MAP
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(if (isMapSelected) Color.White else Color.Transparent)
                        .clickable { onModeSelected(ViewMode.MAP) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🗺️ Mapa",
                        fontSize = 13.sp,
                        fontWeight = if (isMapSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isMapSelected) Color(0xFF004D40) else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun PetCardItem(
    pet: Pet,
    onPetClick: () -> Unit,
    onAdoptClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPetClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Contenedor principal de imagen con esquinas redondeadas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color(pet.colorHex))
            ) {
                // Ilustración / Avatar representativo grande
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = pet.type.emoji,
                        fontSize = 64.sp
                    )
                    Text(
                        text = pet.breed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                }

                // Badge de ubicación
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF004D40),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = pet.city,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF004D40)
                        )
                    }
                }

                // Botón de Favorito en esquina superior derecha
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f))
                        .clickable { onToggleFavorite() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (pet.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (pet.isFavorite) Color.Red else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Información inferior de la tarjeta
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = pet.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            text = "${pet.age} • ${pet.gender} • ${pet.size}",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    // Botón Adoptar
                    Button(
                        onClick = onAdoptClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00BFA5)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        )
                    ) {
                        Text(
                            text = "Adoptar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = pet.description,
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyPetsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🐾", fontSize = 60.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No se encontraron mascotas con los filtros seleccionados",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF004D40),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Intenta cambiando la búsqueda o seleccionando la categoría 'Todos'.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MapViewSimulated(
    pets: List<Pet>,
    onPetClick: (Pet) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE0F2F1)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                tint = Color(0xFF004D40),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Mapa de Mascotas Cercanas",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF004D40)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Mostrando ${pets.size} puntos de adopción en la zona seleccionada",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
            ) {
                items(pets) { pet ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPetClick(pet) },
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = pet.type.emoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = pet.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = pet.city,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    selectedItem: BottomNavItem,
    onItemSelected: (BottomNavItem) -> Unit
) {
    Surface(
        shadowElevation = 10.dp,
        color = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.values().forEach { item ->
                val isSelected = item == selectedItem
                val icon = when (item) {
                    BottomNavItem.HOME -> Icons.Default.Home
                    BottomNavItem.MAP -> Icons.Default.Map
                    BottomNavItem.ALERTS -> Icons.Default.Notifications
                    BottomNavItem.PROFILE -> Icons.Default.Person
                }

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onItemSelected(item) }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFFB2DFDB) else Color.Transparent)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = item.title,
                            tint = if (isSelected) Color(0xFF004D40) else Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF004D40) else Color.Gray
                    )
                }
            }
        }
    }
}
