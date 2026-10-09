package co.edu.uniquindio.adopcionmascota.features.createPosts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.adopcionmascota.core.util.RequestResult

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePostsScreen(
    viewModel: CreatePostsViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nueva publicación",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.publishPost(onSuccess = onBack) },
                        enabled = !state.isPublishing
                    ) {
                        if (state.isPublishing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Publicar",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF004D40)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF4F7F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 2. Sección de fotografías
            SectionHeader(title = "Fotos *")
            PhotoSelectionSection(
                photos = state.photos,
                onAddPhoto = { viewModel.onAddPhoto() },
                onRemovePhoto = { viewModel.onRemovePhoto(it) }
            )
            state.photosError?.let { ErrorText(text = it) }

            // 3. Selección de categoría
            SectionHeader(title = "Categoría *")
            CategorySelectionSection(
                selectedCategory = state.selectedCategory,
                onCategorySelected = { viewModel.onCategorySelected(it) }
            )
            state.categoryError?.let { ErrorText(text = it) }

            // 4. Campo de título
            SectionHeader(title = "Título *")
            CustomOutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.onTitleChange(it) },
                placeholder = "Ej: Luna busca un hogar lleno de amor",
                isError = state.titleError != null,
                errorMessage = state.titleError
            )

            // 5. Campo de raza aproximada
            SectionHeader(title = "Raza aproximada *")
            CustomOutlinedTextField(
                value = state.breed,
                onValueChange = { viewModel.onBreedChange(it) },
                placeholder = "Ej: Golden Retriever, Mestiza o Desconocida",
                isError = state.breedError != null,
                errorMessage = state.breedError
            )

            // 6. Campo de edad
            SectionHeader(title = "Edad")
            CustomOutlinedTextField(
                value = state.age,
                onValueChange = { viewModel.onAgeChange(it) },
                placeholder = "Ej: 2 años, 5 meses"
            )

            // 7. Selector de tamaño
            SectionHeader(title = "Tamaño *")
            SizeSelectionSection(
                selectedSize = state.selectedSize,
                onSizeSelected = { viewModel.onSizeSelected(it) }
            )
            state.sizeError?.let { ErrorText(text = it) }

            // 8. Interruptor de vacunación
            VaccinationSwitchCard(
                isVaccinated = state.isVaccinated,
                onVaccinatedChange = { viewModel.onVaccinatedChange(it) }
            )

            // 9. Campo de descripción
            SectionHeader(title = "Descripción *")
            DescriptionTextField(
                value = state.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                maxLength = state.maxDescriptionLength,
                isError = state.descriptionError != null,
                errorMessage = state.descriptionError
            )

            // 10. Selección de ubicación en el mapa
            SectionHeader(title = "Ubicación * (lat/lng se guardan automáticamente)")
            LocationSelectionSection(
                location = state.selectedLocation,
                onOpenMap = { viewModel.onOpenMapSelector() }
            )
            state.locationError?.let { ErrorText(text = it) }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón principal al final del formulario
            Button(
                onClick = { viewModel.publishPost(onSuccess = onBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !state.isPublishing,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isPublishing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Publicar Mascota",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }

            // Mensaje global de resultado (si falla la validación)
            if (state.publishResult is RequestResult.Failure) {
                val failureMsg = (state.publishResult as RequestResult.Failure).errorMessage
                Text(
                    text = failureMsg,
                    color = Color.Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Diálogo simulado de selección de mapa
    if (state.showMapSelector) {
        AlertDialog(
            onDismissRequest = { viewModel.onCloseMapSelector() },
            title = {
                Text(
                    text = "Seleccionar Ubicación en Mapa",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004D40)
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE0F2F1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color(0xFF004D40),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Toca un punto en el mapa",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF004D40)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Ubicación fijada: Chapinero, Bogotá\nLat: 4.658420, Lng: -74.065680",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onSelectMapLocation(
                            PostLocation("Chapinero, Bogotá", 4.658420, -74.065680)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFA5))
                ) {
                    Text("Confirmar Ubicación", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onCloseMapSelector() }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

// -------------------------------------------------------------------
// Componentes Auxiliares Formulario
// -------------------------------------------------------------------

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = Color(0xFF004D40)
    )
}

@Composable
private fun ErrorText(text: String) {
    Text(
        text = text,
        color = Color.Red,
        fontSize = 12.sp,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
    )
}

@Composable
private fun PhotoSelectionSection(
    photos: List<String>,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Botón agregar foto con borde discontinuo
        item {
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8F0EC))
                    .drawWithContent {
                        drawContent()
                        val stroke = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        drawRoundRect(
                            color = Color(0xFF80CBC4),
                            style = stroke
                        )
                    }
                    .clickable { onAddPhoto() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Agregar foto",
                        tint = Color(0xFF004D40),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Agregar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004D40)
                    )
                }
            }
        }

        // Fotos agregadas
        items(photos) { photo ->
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFB2DFDB))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "🐕", fontSize = 36.sp)
                    Text(
                        text = photo,
                        fontSize = 10.sp,
                        color = Color.DarkGray,
                        maxLines = 1
                    )
                }

                // Botón eliminar foto
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                        .clickable { onRemovePhoto(photo) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Eliminar",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySelectionSection(
    selectedCategory: PostCategory?,
    onCategorySelected: (PostCategory) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        PostCategory.values().forEach { category ->
            val isSelected = category == selectedCategory
            val bgColor = if (isSelected) Color(0xFF80CBC4) else Color(0xFFE8F0EC)
            val textColor = if (isSelected) Color(0xFF004D40) else Color.DarkGray
            val borderColor = if (isSelected) Color(0xFF004D40) else Color(0xFFD0E0D8)

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onCategorySelected(category) }
                    .border(1.dp, borderColor, RoundedCornerShape(20.dp)),
                color = bgColor,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = category.emoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = category.displayName,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun SizeSelectionSection(
    selectedSize: PetSize?,
    onSizeSelected: (PetSize) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PetSize.values().forEach { size ->
            val isSelected = size == selectedSize
            val bgColor = if (isSelected) Color(0xFF80CBC4) else Color(0xFFE8F0EC)
            val borderColor = if (isSelected) Color(0xFF004D40) else Color(0xFFD0E0D8)

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSizeSelected(size) }
                    .border(1.5.dp, borderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = bgColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = size.emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = size.displayName,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF004D40) else Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
private fun VaccinationSwitchCard(
    isVaccinated: Boolean,
    onVaccinatedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0EC))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFB2DFDB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "Vacunas",
                        tint = Color(0xFF004D40),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "¿Tiene vacunas?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF004D40)
                    )
                    Text(
                        text = "Vacunas al día o parciales",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Switch(
                checked = isVaccinated,
                onCheckedChange = onVaccinatedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF004D40),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }
    }
}

@Composable
private fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = placeholder, color = Color.Gray, fontSize = 14.sp) },
            isError = isError,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color(0xFF004D40),
                unfocusedBorderColor = Color(0xFFC0D0C8)
            )
        )
        errorMessage?.let { ErrorText(text = it) }
    }
}

@Composable
private fun DescriptionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    maxLength: Int,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column {
        Box {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                placeholder = {
                    Text(
                        text = "Describe el temperamento, cuidados, necesidades y características de la mascota...",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                },
                isError = isError,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF004D40),
                    unfocusedBorderColor = Color(0xFFC0D0C8)
                )
            )

            // Contador de caracteres en la esquina inferior derecha
            Text(
                text = "${value.length}/$maxLength",
                fontSize = 11.sp,
                color = if (value.length >= maxLength) Color.Red else Color.Gray,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            )
        }
        errorMessage?.let { ErrorText(text = it) }
    }
}

@Composable
private fun LocationSelectionSection(
    location: PostLocation?,
    onOpenMap: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Tarjeta de mapa
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { onOpenMap() },
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Mapa",
                        tint = Color(0xFF004D40),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = "Toca para elegir en mapa",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF004D40)
                        )
                    }
                }
            }
        }

        // Tarjeta de confirmación verde agua
        location?.let { loc ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFB2DFDB))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Ubicación confirmada",
                        tint = Color(0xFF004D40),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${loc.address} · ${String.format("%.6f", loc.latitude)}, ${String.format("%.6f", loc.longitude)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF004D40)
                    )
                }
            }
        }
    }
}
