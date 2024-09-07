package com.example.journalofdream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.journalofdream.model.Dream
import com.example.journalofdream.model.Location
import com.example.journalofdream.viewmodel.DreamViewModel
import com.example.journalofdream.viewmodel.LocationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Правильные импорты для Material3
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.shadow


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()
            val dreamViewModel: DreamViewModel = viewModel()
            val locationViewModel: LocationViewModel = viewModel()

            NavHost(navController, startDestination = "main") {
                composable("main") { MainScreen(navController) }
                composable("dreams") { DreamsScreen(navController, dreamViewModel) }
                composable("addDream") { AddDreamScreen(navController, dreamViewModel) }
                composable("editDream/{id}") { backStackEntry ->
                    val dreamId = backStackEntry.arguments?.getString("id")?.toInt()
                    if (dreamId != null) {
                        EditDreamScreen(navController, dreamId, dreamViewModel)
                    }
                }
                composable("locations") { LocationListScreen(navController, locationViewModel) }
                composable("addLocation") { AddLocationScreen(navController, locationViewModel) }
                composable("editLocation/{id}") { backStackEntry ->
                    val locationId = backStackEntry.arguments?.getString("id")?.toInt()
                    if (locationId != null) {
                        EditLocationScreen(navController, locationId, locationViewModel)
                    }
                }
                composable("viewLocation/{locationId}") { backStackEntry ->
                    val locationId = backStackEntry.arguments?.getString("locationId")?.toInt()
                        ?: return@composable
                    ViewLocationScreen(navController, locationId, locationViewModel)
                }
            }
        }
    }

}


// Функция для отображения фонового изображения
@Composable
fun BackgroundScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fon2), // Замените на ваше изображение
            contentDescription = null,
            contentScale = ContentScale.Crop, // Масштабирование изображения для заполнения экрана
            modifier = Modifier.fillMaxSize() // Заполнение всей области экрана
        )
    }
}


// Главный экран приложения
@Composable
fun MainScreen(navController: NavHostController) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundScreen()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Анимированный заголовок приложения
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + scaleIn(initialScale = 0.8f)
            ) {
                Text(
                    text = "Дневник сновидений",
                    color = Color.Gray.copy(alpha = 0.95f),
                    modifier = Modifier.padding(bottom = 32.dp),
                    fontSize = 32.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
            // Кнопка для перехода на экран сновидений
            Button(
                onClick = {
                    navController.navigate("dreams")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .shadow(50.dp, shape = RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1A237E).copy(alpha = 0.5f), // Применяем прозрачность
                                    Color(0xFF283593).copy(alpha = 0.5f)  // Применяем прозрачность
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Записать сновидение",
                        color = Color.White,
                        fontSize = 24.sp)
                }
            }



            Spacer(modifier = Modifier.height(16.dp)) // Разделитель между кнопками

            // Кнопка для перехода на экран локаций
            Button(
                onClick = {
                    navController.navigate("locations")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .shadow(50.dp, shape = RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF4A148C).copy(alpha = 0.5f), // Применяем прозрачность
                                    Color(0xFF6A1B9A).copy(alpha = 0.5f)  // Применяем прозрачность
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Локации",
                        color = Color.White,
                        fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp)) // Разделитель между кнопками

            // Кнопка для перехода на экран техник (пока не реализована)
            Button(
                onClick = {
                    // Действие для третьей кнопки (техники)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .shadow(50.dp, shape = RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1B5E20).copy(alpha = 0.5f), // Применяем прозрачность
                                    Color(0xFF2E7D32).copy(alpha = 0.5f)  // Применяем прозрачность
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Техники",
                        color = Color.White,
                        fontSize = 24.sp)
                }
            }

        }
    }
}


// Экран для отображения списка снов
@Composable
fun DreamsScreen(navController: NavHostController, dreamViewModel: DreamViewModel) {
    // Получаем список снов из ViewModel как LiveData и конвертируем в Compose State
    val allDreams by dreamViewModel.allDreams.observeAsState(listOf())

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundScreen()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp) // Увеличенный отступ сверху для всей колонки
        ) {
            // Кнопка "Назад" с отступом
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp) // Отступ снизу после кнопки "Назад"
            ) {
                Text("Назад", color = Color.White)
            }

            // Заголовок экрана
            Text(
                text = "Сны",
                style = androidx.compose.ui.text.TextStyle(fontSize = 24.sp, color = Color.White),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Кнопка для добавления нового сновидения
            Button(
                onClick = { navController.navigate("addDream") },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Записать сновидение")
            }

            // Список снов
            allDreams.forEach { dream ->
                Button(
                    onClick = { navController.navigate("editDream/${dream.id}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = "${dream.date}: ${dream.title}", modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}


// Экран для добавления нового сновидения
@Composable
fun AddDreamScreen(navController: NavHostController, dreamViewModel: DreamViewModel) {
    var dreamText by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // Добавляет отступы для правильного отображения с клавиатурой
    ) {
        BackgroundScreen() // Фоновое изображение

        // Кнопка "Отмена" в правом верхнем углу
        Button(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 16.dp), // Отступ сверху и справа
            shape = RoundedCornerShape(50), // Стилизация кнопки
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.5f))
        ) {
            Text("Отмена", color = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp)) // Разделитель для кнопок

            // Оставляем текстовое поле как в оригинальном коде
            BasicTextField(
                value = dreamText,
                onValueChange = { dreamText = it },
                textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp)) // Разделитель для кнопок

            // Кнопка "Сохранить"
            Button(
                onClick = {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val today = dateFormat.format(Date())

                    val newDream = Dream(title = "Сон $today", content = dreamText.text, date = today)
                    dreamViewModel.add(newDream)
                    keyboardController?.hide()
                    navController.popBackStack()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(50), // Стилизация кнопки
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.5f))
            ) {
                Text("Сохранить", color = Color.White)
            }
        }
    }
}


// Экран для редактирования существующего сновидения
@Composable
fun EditDreamScreen(navController: NavHostController, dreamId: Int, dreamViewModel: DreamViewModel) {
    // Ищем сон по его ID
    val dream = dreamViewModel.allDreams.value?.find { it.id == dreamId }
    var dreamText by remember { mutableStateOf(TextFieldValue(dream?.content ?: "")) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        BackgroundScreen()

        // Кнопка "Отмена" в правом верхнем углу с дополнительным отступом сверху
        Button(
            onClick = {
                navController.popBackStack()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 16.dp)
        ) {
            Text("Отмена", color = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            BasicTextField(
                value = dreamText,
                onValueChange = { dreamText = it },
                textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    dreamViewModel.updateDream(dream!!.copy(content = dreamText.text))
                    keyboardController?.hide()
                    navController.popBackStack()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Сохранить изменения")
            }

            Button(
                onClick = {
                    dreamViewModel.deleteDream(dream!!)
                    navController.popBackStack()
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Удалить")
            }
        }
    }
}


// Экран для отображения и управления локациями
@Composable
fun LocationsScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundScreen() // Фоновое изображение

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
        ) {
            // Кнопка "Назад" с отступом
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 16.dp)
            ) {
                Text("Назад", color = Color.White)
            }

            // Заголовок экрана "Локации"
            Text(
                text = "Локации",
                style = TextStyle(fontSize = 24.sp, color = Color.White),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Кнопка для добавления новой локации
            Button(
                onClick = { /* Действие для добавления новой локации */ },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Добавить новую локацию")
            }

            // Список локаций
            val locations = remember { mutableStateListOf<String>() }
            for (location in locations) {
                Text(text = location, modifier = Modifier.padding(bottom = 8.dp))
            }
        }
    }
}



@Composable
fun LocationListScreen(navController: NavHostController, locationViewModel: LocationViewModel) {
    val allLocations by locationViewModel.allLocations.observeAsState(listOf())

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundScreen()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
        ) {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text("Назад", color = Color.White)
            }

            Text(
                text = "Локации",
                style = TextStyle(fontSize = 24.sp, color = Color.White),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = { navController.navigate("addLocation") },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Добавить новую локацию")
            }

            for (location in allLocations) {
                Button(
                    onClick = { navController.navigate("viewLocation/${location.id}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = location.name, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}



@Composable
fun AddLocationScreen(navController: NavHostController, locationViewModel: LocationViewModel) {
    var locationName by remember { mutableStateOf(TextFieldValue("")) }
    var locationDescription by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        BackgroundScreen()

        // Кнопка "Отмена" в правом верхнем углу
        Button(
            onClick = {
                navController.popBackStack()
            }, modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 16.dp)
        ) {
            Text("Отмена", color = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Заголовок для названия локации
            Text(
                text = "Название локации",
                style = TextStyle(color = Color.White, fontSize = 18.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Поле для ввода названия локации
            BasicTextField(
                value = locationName,
                onValueChange = { locationName = it },
                textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f)) // затемненное текстовое поле
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Заголовок для описания локации
            Text(
                text = "Описание локации",
                style = TextStyle(color = Color.White, fontSize = 18.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Поле для ввода описания локации с фиксированной высотой
            BasicTextField(
                value = locationDescription,
                onValueChange = { locationDescription = it },
                textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)  // Умеренная фиксированная высота
                    .background(Color.Black.copy(alpha = 0.5f)) // затемненное текстовое поле
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка "Сохранить"
            Button(
                onClick = {
                    val newLocation = Location(
                        name = locationName.text, description = locationDescription.text
                    )
                    locationViewModel.addLocation(newLocation)
                    keyboardController?.hide()
                    navController.popBackStack()
                }, modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Сохранить")
            }
        }
    }
}


@Composable
fun EditLocationScreen(
    navController: NavHostController, locationId: Int, locationViewModel: LocationViewModel
) {
    val location = locationViewModel.allLocations.value?.find { it.id == locationId }
    var locationName by remember { mutableStateOf(TextFieldValue(location?.name ?: "")) }
    var locationDescription by remember {
        mutableStateOf(
            TextFieldValue(
                location?.description ?: ""
            )
        )
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        BackgroundScreen()

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 16.dp)
        ) {
            Text("Отмена", color = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            BasicTextField(
                value = locationName,
                onValueChange = { locationName = it },
                textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = locationDescription,
                onValueChange = { locationDescription = it },
                textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (location != null) {
                        val updatedLocation = location.copy(
                            name = locationName.text, description = locationDescription.text
                        )
                        locationViewModel.updateLocation(updatedLocation)
                    }
                    keyboardController?.hide()
                    navController.popBackStack()
                }, modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Сохранить изменения")
            }

            Button(
                onClick = {
                    if (location != null) {
                        locationViewModel.deleteLocation(location)
                    }
                    navController.popBackStack()
                }, modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Удалить")
            }
        }
    }
}



@Composable
fun ViewLocationScreen(
    navController: NavHostController,
    locationId: Int,
    locationViewModel: LocationViewModel = viewModel()
) {
    val location by locationViewModel.getLocationById(locationId).observeAsState()

    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    location?.let {
        if (!isEditing) {
            name = it.name
            description = it.description
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundScreen()

        // Кнопка "Назад" в правом верхнем углу
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 16.dp)
        ) {
            Text("Назад", color = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Отображение названия и описания локации без редактирования
            Text(
                text = name,
                style = TextStyle(color = Color.White, fontSize = 24.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = description,
                style = TextStyle(color = Color.White, fontSize = 18.sp),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Кнопки "Редактировать" и "Удалить"
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Button(onClick = { isEditing = true }) {
                    Text("Редактировать")
                }

                Button(
                    onClick = {
                        location?.let {
                            locationViewModel.deleteLocation(it)
                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("Удалить")
                }
            }

            if (isEditing) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Название локации",
                    style = TextStyle(color = Color.White, fontSize = 18.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Описание локации",
                    style = TextStyle(color = Color.White, fontSize = 18.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                BasicTextField(
                    value = description,
                    onValueChange = { description = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопка "Сохранить изменения"
                Button(
                    onClick = {
                        val updatedLocation = location?.copy(
                            name = name,
                            description = description
                        )
                        if (updatedLocation != null) {
                            locationViewModel.updateLocation(updatedLocation)
                        }
                        isEditing = false // Завершаем режим редактирования
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Сохранить изменения")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Список снов, связанных с этой локацией
            Text(
                text = "Связанные сны:",
                style = TextStyle(color = Color.White, fontSize = 18.sp),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            location?.let {
                val relatedDreams by locationViewModel.getDreamsByLocation(it.id).observeAsState(listOf())
                relatedDreams.forEach { dream ->
                    Text(text = "${dream.date}: ${dream.title}", color = Color.White, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}






@Composable
fun LocationDetailsScreen(
    navController: NavHostController,
    locationId: Int,
    locationViewModel: LocationViewModel = viewModel()
) {
    val location by locationViewModel.getLocationById(locationId).observeAsState()

    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    location?.let {
        name = it.name
        description = it.description
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (isEditing) "Редактировать локацию" else "Просмотр локации",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )


        TextField(
            value = name,
            onValueChange = { if (isEditing) name = it },
            label = { Text("Название локации") },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Gray.copy(alpha = 0.2f))
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = description,
            onValueChange = { if (isEditing) description = it },
            label = { Text("Описание локации") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color.Gray.copy(alpha = 0.2f))
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isEditing) {
            Button(onClick = { isEditing = true }) {
                Text("Редактировать")
            }
        } else {
            Row {
                Button(onClick = {
                    locationViewModel.updateLocation(Location(locationId, name, description))
                    navController.popBackStack()
                }) {
                    Text("Сохранить")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = { isEditing = false }) {
                    Text("Отмена")
                }
            }
        }
    }
}


// Превью экрана для быстрой проверки интерфейса в Android Studio
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val navController = rememberNavController()
    MainScreen(navController)
}
