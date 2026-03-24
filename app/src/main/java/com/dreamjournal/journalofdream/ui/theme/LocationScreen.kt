package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.R
import com.dreamjournal.journalofdream.model.LocationWithDreams
import com.dreamjournal.journalofdream.viewmodel.LocationViewModel

private val GoldLight = Color(0xFFF0D68C)
private val GoldDark = Color(0xFFD4A76A)
private val SoftWhite = Color.White.copy(alpha = 0.78f)
private val PlayfairFamily = FontFamily(
    Font(R.font.playfair_display_bold, FontWeight.Bold)
)

enum class LocationSort {
    BY_DREAMS,
    BY_NAME,
    BY_LAST_DREAM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationListScreen(
    navController: NavHostController,
    locationViewModel: LocationViewModel
) {
    val pluralOne = stringResource(R.string.plural_dreams_one)
    val pluralFew = stringResource(R.string.plural_dreams_few)
    val pluralMany = stringResource(R.string.plural_dreams_many)
    val locationsWithDreams by locationViewModel.getAllLocationsWithDreams().observeAsState(emptyList())
    val syncError by locationViewModel.syncError.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentSort by remember { mutableStateOf(LocationSort.BY_DREAMS) }
    var showSortMenu by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<LocationWithDreams?>(null) }

    LaunchedEffect(syncError) {
        syncError?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Long)
            locationViewModel.clearSyncError()
        }
    }

    val sortedLocations = remember(locationsWithDreams, currentSort) {
        when (currentSort) {
            LocationSort.BY_DREAMS -> locationsWithDreams.sortedByDescending { it.dreams.size }
            LocationSort.BY_NAME -> locationsWithDreams.sortedBy { it.location.name.lowercase() }
            LocationSort.BY_LAST_DREAM -> locationsWithDreams.sortedByDescending { lwd ->
                lwd.dreams.maxOfOrNull { it.date } ?: ""
            }
        }
    }

    val sortLabel = when (currentSort) {
        LocationSort.BY_DREAMS -> stringResource(R.string.locations_sort_popular)
        LocationSort.BY_NAME -> stringResource(R.string.locations_sort_alpha)
        LocationSort.BY_LAST_DREAM -> stringResource(R.string.locations_sort_last)
    }

    val arrowRotation by animateFloatAsState(
        targetValue = if (showSortMenu) 180f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "arrow"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.new_fon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(paddingValues)
                    .padding(horizontal = 14.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back),
                            tint = GoldLight,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = stringResource(R.string.locations_title),
                        color = GoldLight,
                        fontFamily = PlayfairFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 29.sp,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = { navController.navigate("addLocation") }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.locations_add),
                            tint = GoldLight,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                GlassTopInfoBar(
                    totalText = stringResource(R.string.locations_total, locationsWithDreams.size),
                    sortLabel = sortLabel,
                    arrowRotation = arrowRotation,
                    onSortClick = { showSortMenu = true },
                    menuExpanded = showSortMenu,
                    onDismissMenu = { showSortMenu = false },
                    onPickSort = { picked ->
                        currentSort = picked
                        showSortMenu = false
                    },
                    currentSort = currentSort
                )

                Spacer(Modifier.height(12.dp))

                if (locationsWithDreams.isEmpty()) {
                    EmptyLocationsCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(sortedLocations, key = { it.location.id }) { lwd ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.EndToStart) {
                                        pendingDelete = lwd
                                    }
                                    false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    val scale by animateFloatAsState(
                                        targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1.18f else 0.84f,
                                        label = "deleteScale"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(26.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(
                                                        Color(0x80201010),
                                                        Color(0xFFD63A3A)
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.btn_delete),
                                            tint = Color.White,
                                            modifier = Modifier
                                                .padding(end = 24.dp)
                                                .scale(scale)
                                                .size(28.dp)
                                        )
                                    }
                                }
                            ) {
                                LocationCard(
                                    locationWithDreams = lwd,
                                    onClick = { navController.navigate("viewLocation/${lwd.location.id}") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { lwd ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = {
                Text(
                    text = stringResource(R.string.dialog_delete_location_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                val count = lwd.dreams.size
                Text(
                    text = "\"${lwd.location.name}\" будет удалена." +
                            if (count > 0) "\n\n$count ${dreamWord(count, pluralOne, pluralFew, pluralMany)} останутся, но привязка к локации исчезнет."
                            else "",
                    color = Color.White.copy(alpha = 0.82f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        locationViewModel.deleteLocation(lwd.location)
                        pendingDelete = null
                    }
                ) {
                    Text(stringResource(R.string.btn_delete), color = Color(0xFFFF8A80))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.btn_cancel), color = GoldLight)
                }
            }
        )
    }
}

@Composable
private fun GlassTopInfoBar(
    totalText: String,
    sortLabel: String,
    arrowRotation: Float,
    onSortClick: () -> Unit,
    menuExpanded: Boolean,
    onDismissMenu: () -> Unit,
    onPickSort: (LocationSort) -> Unit,
    currentSort: LocationSort
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF26174B).copy(alpha = 0.55f),
                        Color(0xFF120B2D).copy(alpha = 0.72f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        GoldLight.copy(alpha = 0.28f),
                        Color.White.copy(alpha = 0.10f),
                        GoldDark.copy(alpha = 0.18f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = totalText,
                color = Color.White.copy(alpha = 0.62f),
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )

            Box {
                TextButton(
                    onClick = onSortClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = sortLabel,
                        color = SoftWhite,
                        fontSize = 13.sp
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = GoldLight,
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .size(18.dp)
                            .rotate(arrowRotation)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = onDismissMenu
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.locations_sort_popular)) },
                        onClick = { onPickSort(LocationSort.BY_DREAMS) },
                        trailingIcon = {
                            if (currentSort == LocationSort.BY_DREAMS) {
                                Text("✓", color = Color(0xFF7E57C2))
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.locations_sort_alpha)) },
                        onClick = { onPickSort(LocationSort.BY_NAME) },
                        trailingIcon = {
                            if (currentSort == LocationSort.BY_NAME) {
                                Text("✓", color = Color(0xFF7E57C2))
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.locations_sort_last)) },
                        onClick = { onPickSort(LocationSort.BY_LAST_DREAM) },
                        trailingIcon = {
                            if (currentSort == LocationSort.BY_LAST_DREAM) {
                                Text("✓", color = Color(0xFF7E57C2))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LocationCard(
    locationWithDreams: LocationWithDreams,
    onClick: () -> Unit
) {
    val dreamCount = locationWithDreams.dreams.size
    val location = locationWithDreams.location
    val lastDreamDate = locationWithDreams.dreams
        .mapNotNull { it.date.ifBlank { null } }
        .maxOrNull()

    val orbRes = when {
        dreamCount >= 5 -> R.drawable.fire_round_lvl3
        dreamCount >= 1 -> R.drawable.fire_round_lvl2
        else -> R.drawable.fire_round_lvl1
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2C1A58).copy(alpha = 0.78f),
                                Color(0xFF1A113E).copy(alpha = 0.88f),
                                Color(0xFF120B2D).copy(alpha = 0.92f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                GoldLight.copy(alpha = 0.20f),
                                Color.White.copy(alpha = 0.08f),
                                GoldDark.copy(alpha = 0.16f)
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(90.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x66C59BFF),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 18.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = location.name.ifEmpty { "(Без названия)" },
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = if (lastDreamDate != null) {
                            stringResource(R.string.locations_last_dream, lastDreamDate)
                        } else {
                            stringResource(R.string.locations_no_dreams)
                        },
                        color = Color.White.copy(alpha = 0.52f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (location.description.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = location.description,
                            color = Color.White.copy(alpha = 0.70f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Box(
                    modifier = Modifier.size(74.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(orbRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = dreamCount.toString(),
                        color = Color.White,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyLocationsCard(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF241748).copy(alpha = 0.58f),
                        Color(0xFF120B2D).copy(alpha = 0.76f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        GoldLight.copy(alpha = 0.24f),
                        Color.White.copy(alpha = 0.10f),
                        GoldDark.copy(alpha = 0.14f)
                    )
                ),
                shape = RoundedCornerShape(30.dp)
            )
            .padding(horizontal = 22.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(88.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.fire_round_lvl1),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            Icon(
                painter = painterResource(R.drawable.location_icon_gold),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.locations_empty),
            color = GoldLight,
            fontFamily = PlayfairFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.locations_empty_hint),
            color = Color.White.copy(alpha = 0.72f),
            fontSize = 14.sp
        )
    }
}

fun dreamWord(count: Int, one: String, few: String, many: String): String {
    val lastTwo = count % 100
    val lastOne = count % 10
    return when {
        lastTwo in 11..19 -> many
        lastOne == 1 -> one
        lastOne in 2..4 -> few
        else -> many
    }
}
