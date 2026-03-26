package com.dreamjournal.journalofdream.ui.theme

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.R
import com.dreamjournal.journalofdream.model.Category
import com.dreamjournal.journalofdream.model.Dream
import com.dreamjournal.journalofdream.ui.dreams.DreamListItem
import com.dreamjournal.journalofdream.util.localizeCategory
import com.dreamjournal.journalofdream.viewmodel.CategoryViewModel
import com.dreamjournal.journalofdream.viewmodel.DreamViewModel
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight


private val GoldLight = Color(0xFFF0D68C)
private val GoldDark = Color(0xFFD4A76A)
private val SoftWhite = Color.White.copy(alpha = 0.78f)
private val PlayfairFamily = FontFamily(
    Font(R.font.playfair_display_bold, FontWeight.Bold)
)

fun dateToMonthKey(date: String): String {
    return try {
        if (date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            date.substring(0, 7)
        } else {
            val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
            val cal = java.util.Calendar.getInstance()
            cal.time = sdf.parse(date)!!
            String.format("%04d-%02d", cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1)
        }
    } catch (e: Exception) {
        "0000-00"
    }
}

fun dateToSortKey(date: String): String {
    return try {
        if (date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            date
        } else {
            val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
            val cal = java.util.Calendar.getInstance()
            cal.time = sdf.parse(date)!!
            String.format(
                "%04d-%02d-%02d",
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH) + 1,
                cal.get(java.util.Calendar.DAY_OF_MONTH)
            )
        }
    } catch (e: Exception) {
        "0000-00-00"
    }
}

fun formatMonthKey(key: String, monthNames: List<String>): String {
    return try {
        val parts = key.split("-")
        val year = parts[0]
        val month = parts[1].toInt() - 1
        "${monthNames[month]} $year"
    } catch (e: Exception) {
        key
    }
}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DreamsScreen(
    navController: NavHostController,
    dreamViewModel: DreamViewModel,
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val monthNames = listOf(
        stringResource(R.string.month_jan), stringResource(R.string.month_feb), stringResource(R.string.month_mar),
        stringResource(R.string.month_apr), stringResource(R.string.month_may), stringResource(R.string.month_jun),
        stringResource(R.string.month_jul), stringResource(R.string.month_aug), stringResource(R.string.month_sep),
        stringResource(R.string.month_oct), stringResource(R.string.month_nov), stringResource(R.string.month_dec)
    )
    val allDreams by dreamViewModel.dreams.observeAsState(emptyList())
    val categories by categoryViewModel.allCategories.observeAsState(emptyList())
    val categoryColorMap = remember(categories) { categories.associate { it.name to it.color } }
    val syncError by dreamViewModel.syncError.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(syncError) {
        syncError?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Long)
            dreamViewModel.clearSyncError()
        }
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var monthMode by remember { mutableStateOf(true) }
    var sortByDate by rememberSaveable { mutableStateOf(true) }
    var openedMonth by rememberSaveable { mutableStateOf<String?>(null) }

    BackHandler(enabled = openedMonth != null) { openedMonth = null }

    val searchResults by produceState(
        initialValue = allDreams,
        key1 = searchQuery.text,
        key2 = selectedCategory,
        key3 = allDreams
    ) {
        if (searchQuery.text.isBlank() && selectedCategory == null) {
            value = allDreams
        } else {
            delay(250)
            value = allDreams.filter { dream ->
                (selectedCategory == null || dream.category == selectedCategory?.name) &&
                    (searchQuery.text.isBlank() ||
                        dream.title.contains(searchQuery.text, ignoreCase = true) ||
                        dream.content.contains(searchQuery.text, ignoreCase = true))
            }
        }
    }

    val allMonthKeys = remember(allDreams) {
        allDreams
            .map { dateToMonthKey(it.date) }
            .toSortedSet(compareByDescending { it })
    }

    val dreamsByMonth = remember(searchResults) {
        searchResults.groupBy { dateToMonthKey(it.date) }
    }

    val headerTitle = if (openedMonth != null) formatMonthKey(openedMonth!!, monthNames) else stringResource(R.string.app_name)
    val modeButtonText = if (sortByDate) stringResource(R.string.dreams_sort_az) else stringResource(R.string.dreams_sort_date)

    val arrowRotation by animateFloatAsState(
        targetValue = if (categoryExpanded) 180f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "categoryArrow"
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
                    .imePadding()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
            ) {
                Spacer(Modifier.padding(top = 4.dp))

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, start = 14.dp, end = 14.dp)
                ) {
                    var headerFontSize by remember(headerTitle, openedMonth, maxWidth) {
                        mutableStateOf(if (openedMonth == null) 26.sp else 24.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (openedMonth != null) {
                            IconButton(
                                onClick = { openedMonth = null },
                                modifier = Modifier.size(42.dp)
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = stringResource(R.string.btn_back),
                                    tint = GoldLight,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.size(42.dp)
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = stringResource(R.string.btn_back),
                                    tint = GoldLight,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Text(
                            text = headerTitle,
                            color = GoldLight,
                            fontFamily = PlayfairFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = headerFontSize,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            onTextLayout = { result ->
                                if (result.didOverflowWidth && headerFontSize > 17.sp) {
                                    headerFontSize = (headerFontSize.value * 0.93f).sp
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 2.dp, end = 2.dp)
                        )

                        TextButton(
                            onClick = {
                                if (openedMonth == null) {
                                    monthMode = !monthMode
                                } else {
                                    sortByDate = !sortByDate
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(11.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .height(32.dp)
                        ) {
                            Text(
                                text = modeButtonText,
                                color = SoftWhite,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }

                        Spacer(Modifier.size(2.dp))

                        IconButton(
                            onClick = { navController.navigate("addDream") },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.btn_add_dream),
                                tint = GoldLight,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.size(8.dp))

                DreamSearchField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        if (it.text.isNotBlank()) {
                            openedMonth = null
                            monthMode = false
                        }
                    },
                    modifier = Modifier.padding(horizontal = 14.dp)
                )

                Spacer(Modifier.size(10.dp))

                DreamCategoryField(
                    selectedCategory = selectedCategory,
                    categoryExpanded = categoryExpanded,
                    onOpen = { categoryExpanded = true },
                    onDismiss = { categoryExpanded = false },
                    categories = categories,
                    arrowRotation = arrowRotation,
                    onSelectCategory = {
                        selectedCategory = it
                        categoryExpanded = false
                    },
                    modifier = Modifier.padding(horizontal = 14.dp)
                )

                Spacer(Modifier.size(12.dp))

                AnimatedContent(
                    targetState = openedMonth,
                    label = "monthTransition",
                    transitionSpec = {
                        if (targetState != null) {
                            slideInVertically(
                                animationSpec = spring(stiffness = 400f),
                                initialOffsetY = { it / 2 }
                            ) + fadeIn() togetherWith
                                slideOutVertically(
                                    animationSpec = spring(stiffness = 400f),
                                    targetOffsetY = { -it / 4 }
                                ) + fadeOut()
                        } else {
                            slideInVertically(
                                animationSpec = spring(stiffness = 400f),
                                initialOffsetY = { -it / 4 }
                            ) + fadeIn() togetherWith
                                slideOutVertically(
                                    animationSpec = spring(stiffness = 400f),
                                    targetOffsetY = { it / 2 }
                                ) + fadeOut()
                        }
                    }
                ) { month ->
                    // Блокируем только клики на карточки снов во время перехода ВПЕРЁД
                    // (когда открываем месяц). При возврате назад блокировки нет.
                    val blockDreamClicks = transition.isRunning && month != null
                    if (month != null) {
                        val dreamsInMonth = (dreamsByMonth[month] ?: emptyList()).filter { dream ->
                            (selectedCategory == null || dream.category == selectedCategory?.name) &&
                                (searchQuery.text.isBlank() ||
                                    dream.title.contains(searchQuery.text, ignoreCase = true) ||
                                    dream.content.contains(searchQuery.text, ignoreCase = true))
                        }

                        if (dreamsInMonth.isEmpty()) {
                            DreamEmptyCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 18.dp),
                                title = stringResource(R.string.dreams_empty_month),
                                subtitle = ""
                            )
                        } else {
                            val sortedInMonth = remember(dreamsInMonth, sortByDate) {
                                if (sortByDate) {
                                    dreamsInMonth.sortedByDescending { dateToSortKey(it.date) + " " + it.time }
                                } else {
                                    dreamsInMonth.sortedWith(
                                        compareBy(
                                            { it.category.ifBlank { context.getString(R.string.dreams_no_category) } },
                                            { dateToSortKey(it.date) + " " + it.time }
                                        )
                                    )
                                }
                            }

                            DreamsListContent(
                                dreams = sortedInMonth,
                                sortByDate = sortByDate,
                                navController = navController,
                                dreamViewModel = dreamViewModel,
                                categoryColorMap = categoryColorMap,
                                noCategoryText = context.getString(R.string.dreams_no_category),
                                modifier = Modifier.padding(horizontal = 14.dp),
                                clicksEnabled = !blockDreamClicks
                            )
                        }
                    } else {
                        when {
                            searchResults.isEmpty() -> {
                                DreamEmptyCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 18.dp),
                                    title = if (searchQuery.text.isNotBlank() || selectedCategory != null)
                                        stringResource(R.string.dreams_not_found)
                                    else
                                        stringResource(R.string.dreams_empty),
                                    subtitle = if (searchQuery.text.isBlank() && selectedCategory == null)
                                        stringResource(R.string.dreams_empty_hint)
                                    else
                                        ""
                                )
                            }
                            monthMode -> {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(start = 2.dp, end = 2.dp, top = 2.dp, bottom = 40.dp)
                                ) {
                                    gridItems(allMonthKeys.toList()) { monthKey ->
                                        DreamMonthCard(
                                            monthKey = monthKey,
                                            count = dreamsByMonth[monthKey]?.size ?: 0,
                                            monthNames = monthNames,
                                            onClick = { openedMonth = monthKey }
                                        )
                                    }
                                }
                            }
                            else -> {
                                val sortedResults = remember(searchResults, sortByDate) {
                                    if (sortByDate) {
                                        searchResults.sortedByDescending { dateToSortKey(it.date) + " " + it.time }
                                    } else {
                                        searchResults.sortedWith(
                                            compareBy(
                                                { it.category.ifBlank { context.getString(R.string.dreams_no_category) } },
                                                { dateToSortKey(it.date) + " " + it.time }
                                            )
                                        )
                                    }
                                }

                                DreamsListContent(
                                    dreams = sortedResults,
                                    sortByDate = sortByDate,
                                    navController = navController,
                                    dreamViewModel = dreamViewModel,
                                    categoryColorMap = categoryColorMap,
                                    noCategoryText = context.getString(R.string.dreams_no_category),
                                    modifier = Modifier.padding(horizontal = 14.dp)
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
private fun DreamSearchField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF6E4BA0).copy(alpha = 0.28f),
                        Color(0xFF413167).copy(alpha = 0.28f)
                    )
                )
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Search,
                contentDescription = stringResource(R.string.dreams_search),
                tint = Color.White.copy(alpha = 0.68f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.size(10.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (value.text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.dreams_search),
                            color = Color.White.copy(alpha = 0.50f),
                            fontSize = 18.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun DreamCategoryField(
    selectedCategory: Category?,
    categoryExpanded: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    categories: List<Category>,
    arrowRotation: Float,
    onSelectCategory: (Category?) -> Unit,
    modifier: Modifier = Modifier
) {
    val noCategory = stringResource(R.string.dreams_no_category)
    val localizedText = selectedCategory?.name?.let {
        localizeCategory(
            it,
            noCategory,
            stringResource(R.string.cat_nightmares),
            stringResource(R.string.cat_lucid),
            stringResource(R.string.cat_plot),
            stringResource(R.string.cat_personal)
        )
    } ?: stringResource(R.string.dreams_all)

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF5A2E78).copy(alpha = 0.32f),
                            Color(0xFF2F224D).copy(alpha = 0.36f),
                            Color(0xFF6B478F).copy(alpha = 0.32f)
                        )
                    )
                )
                .clickable { onOpen() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = localizedText,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 17.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = GoldLight,
                modifier = Modifier.rotate(arrowRotation)
            )
        }

        DropdownMenu(
            expanded = categoryExpanded,
            onDismissRequest = onDismiss
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.dreams_all)) },
                onClick = { onSelectCategory(null) }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Text(
                            localizeCategory(
                                category.name,
                                noCategory,
                                stringResource(R.string.cat_nightmares),
                                stringResource(R.string.cat_lucid),
                                stringResource(R.string.cat_plot),
                                stringResource(R.string.cat_personal)
                            )
                        )
                    },
                    onClick = { onSelectCategory(category) }
                )
            }
        }
    }
}

@Composable
private fun DreamsListContent(
    dreams: List<Dream>,
    sortByDate: Boolean,
    navController: NavHostController,
    dreamViewModel: DreamViewModel,
    categoryColorMap: Map<String, String>,
    noCategoryText: String,
    modifier: Modifier = Modifier,
    clicksEnabled: Boolean = true
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        if (!sortByDate) {
            val grouped = dreams.groupBy { it.category.ifBlank { noCategoryText } }
            grouped.forEach { (category, dreamsInGroup) ->
                item(key = "group_$category") {
                    Text(
                        text = category,
                        color = GoldLight,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp, bottom = 4.dp, top = 2.dp)
                    )
                }
                items(dreamsInGroup, key = { it.localId }) { dream ->
                    DreamListItem(
                        dream = dream,
                        navController = navController,
                        onDelete = { dreamViewModel.deleteDream(it) },
                        categoryColor = categoryColorMap[dream.category],
                        clickEnabled = clicksEnabled
                    )
                }
            }
        } else {
            items(dreams, key = { it.localId }) { dream ->
                DreamListItem(
                    dream = dream,
                    navController = navController,
                    onDelete = { dreamViewModel.deleteDream(it) },
                    categoryColor = categoryColorMap[dream.category],
                    clickEnabled = clicksEnabled
                )
            }
        }
    }
}

@Composable
fun DreamMonthCard(
    monthKey: String,
    count: Int,
    monthNames: List<String>,
    onClick: () -> Unit
) {
    val pluralOne = stringResource(R.string.plural_dreams_one)
    val pluralFew = stringResource(R.string.plural_dreams_few)
    val pluralMany = stringResource(R.string.plural_dreams_many)

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val cardWidth = maxWidth
        val titleFont = (cardWidth.value * 0.082f).sp
        val numberFont = (cardWidth.value * 0.32f).sp
        val bottomFont = (cardWidth.value * 0.11f).sp

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.88f),
            onClick = onClick,
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.month_ram),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .fillMaxHeight(0.52f)
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatMonthKey(monthKey, monthNames),
                        color = GoldLight,
                        fontFamily = PlayfairFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = titleFont,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                    )

                    Text(
                        text = count.toString(),
                        color = GoldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = numberFont,
                        lineHeight = numberFont,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    Text(
                        text = pluralDreams(count, pluralOne, pluralFew, pluralMany),
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = bottomFont,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
private fun DreamEmptyCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String
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
            .padding(horizontal = 12.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🌙",
            fontSize = 42.sp
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = title,
            color = GoldLight,
            fontFamily = PlayfairFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.size(10.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 14.sp
            )
        }
    }
}
