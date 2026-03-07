package com.example.journalofdream.ui.theme

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.journalofdream.ui.auth.PinMode
import com.example.journalofdream.ui.auth.PinScreen
import com.example.journalofdream.ui.auth.hasPin
import com.example.journalofdream.ui.auth.isPinEnabled
import com.example.journalofdream.ui.auth.removePin
import com.example.journalofdream.ui.auth.setPinEnabled
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.journalofdream.ui.common.BackgroundScreen
import com.example.journalofdream.util.cancelQuoteAlarms
import com.example.journalofdream.util.scheduleDailyReminder
import com.example.journalofdream.util.scheduleQuoteAlarms

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController, isAdminMode: Boolean = false) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var notificationHour by remember { mutableStateOf(prefs.getInt("notification_hour", 8)) }
    var notificationMinute by remember { mutableStateOf(prefs.getInt("notification_minute", 0)) }
    var notificationsEnabled by remember { mutableStateOf(prefs.getBoolean("notifications_enabled", true)) }
    var motivationalQuotes by remember { mutableStateOf(prefs.getBoolean("motivational_quotes", true)) }

    // PIN состояние — вынесено наверх чтобы показывать поверх всего экрана
    var pinEnabled by remember { mutableStateOf(isPinEnabled(context)) }
    var showSetPin by remember { mutableStateOf(false) }
    var showChangePin by remember { mutableStateOf(false) }

    // Цитаты из Firestore (только для AdminMode — переход на отдельный экран)
    // Управление цитатами вынесено в QuotesAdminScreen

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundScreen()

            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                // Секция уведомлений
                SettingsSectionTitle("🔔 Уведомления")

                // Включить/выключить уведомления
                SettingsToggleRow(
                    icon = Icons.Default.Notifications,
                    title = "Напоминания о записи сна",
                    subtitle = "Ежедневное напоминание записать сон",
                    checked = notificationsEnabled,
                    onCheckedChange = { enabled ->
                        notificationsEnabled = enabled
                        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
                        if (enabled) {
                            scheduleDailyReminder(context, notificationHour, notificationMinute)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Время уведомления
                SettingsClickRow(
                    icon = Icons.Default.DateRange,
                    title = "Время уведомления",
                    subtitle = String.format("%02d:%02d", notificationHour, notificationMinute),
                    enabled = notificationsEnabled,
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                notificationHour = hour
                                notificationMinute = minute
                                prefs.edit()
                                    .putInt("notification_hour", hour)
                                    .putInt("notification_minute", minute)
                                    .apply()
                                if (notificationsEnabled) {
                                    scheduleDailyReminder(context, hour, minute)
                                }
                            },
                            notificationHour,
                            notificationMinute,
                            true
                        ).show()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Мотивационные цитаты
                SettingsToggleRow(
                    icon = Icons.Default.Notifications,
                    title = "Мотивационные цитаты",
                    subtitle = "Цитаты об осознанных снах в 10:00, 16:00 и 22:00",
                    checked = motivationalQuotes,
                    onCheckedChange = { enabled ->
                        motivationalQuotes = enabled
                        prefs.edit().putBoolean("motivational_quotes", enabled).apply()
                        if (enabled) {
                            scheduleQuoteAlarms(context)
                        } else {
                            cancelQuoteAlarms(context)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Секция безопасности
                SettingsSectionTitle("🔒 Безопасность")

                // Включить/выключить PIN
                SettingsToggleRow(
                    icon = Icons.Default.Lock,
                    title = "Защита PIN-кодом",
                    subtitle = if (pinEnabled) "Приложение защищено PIN-кодом"
                               else "Включить защиту приложения",
                    checked = pinEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            showSetPin = true
                        } else {
                            removePin(context)
                            setPinEnabled(context, false)
                            pinEnabled = false
                        }
                    }
                )

                // Сменить PIN — только если включён
                if (pinEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsClickRow(
                        icon = Icons.Default.Lock,
                        title = "Сменить PIN-код",
                        subtitle = "Установить новый PIN-код",
                        onClick = { showChangePin = true }
                    )
                }

                // Раздел цитат — только в режиме админа
                if (isAdminMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsSectionTitle("💬 Цитаты для уведомлений")
                    SettingsClickRow(
                        icon = Icons.Default.Add,
                        title = "Управление цитатами",
                        subtitle = "Добавить или удалить цитаты для уведомлений",
                        onClick = { navController.navigate("quotes_admin") }
                    )
                }
                } // закрываем item
            } // закрываем LazyColumn
        }
    }

    // PinScreen поверх всего экрана
    if (showSetPin) {
        PinScreen(
            mode = PinMode.SET,
            onSuccess = {
                pinEnabled = true
                showSetPin = false
            },
            onCancel = { showSetPin = false }
        )
    } else if (showChangePin) {
        PinScreen(
            mode = PinMode.SET,
            onSuccess = { showChangePin = false },
            onCancel = { showChangePin = false }
        )
    }

    // Диалоги цитат вынесены в QuotesAdminScreen
} // закрываем Box
} // закрываем SettingsScreen

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 16.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun SettingsClickRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(
                alpha = if (enabled) 0.9f else 0.5f
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = { if (enabled) onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary.copy(
                        alpha = if (enabled) 1f else 0.4f
                    )
                )
            }
        }
    }
}
