package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.ui.res.stringResource
import com.dreamjournal.journalofdream.R
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dreamjournal.journalofdream.MainActivity
import com.dreamjournal.journalofdream.ui.auth.PinMode
import com.dreamjournal.journalofdream.ui.auth.PinScreen
import com.dreamjournal.journalofdream.ui.auth.isBiometricAvailable
import com.dreamjournal.journalofdream.ui.auth.isBiometricEnabledForCurrentUser
import com.dreamjournal.journalofdream.ui.auth.isPinEnabled
import com.dreamjournal.journalofdream.ui.auth.removePin
import com.dreamjournal.journalofdream.ui.auth.setBiometricEnabled
import com.dreamjournal.journalofdream.ui.auth.setPinEnabled
import com.dreamjournal.journalofdream.util.LocaleHelper
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
import com.dreamjournal.journalofdream.ui.common.BackgroundScreen
import com.dreamjournal.journalofdream.util.cancelQuoteAlarms
import com.dreamjournal.journalofdream.util.scheduleDailyReminder
import com.dreamjournal.journalofdream.util.scheduleQuoteAlarms
import com.dreamjournal.journalofdream.util.areNotificationsAllowed
import com.dreamjournal.journalofdream.util.canUseExactAlarms
import com.dreamjournal.journalofdream.util.openAppNotificationSettings
import com.dreamjournal.journalofdream.util.openExactAlarmSettings

import android.app.Activity
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.window.DialogProperties
import com.dreamjournal.journalofdream.util.deleteCurrentUserAccountAndData
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.dreamjournal.journalofdream.util.cancelDailyReminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController, isAdminMode: Boolean = false) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var notificationHour by remember { mutableStateOf(prefs.getInt("notification_hour", 8)) }
    var notificationMinute by remember { mutableStateOf(prefs.getInt("notification_minute", 0)) }
    var notificationsEnabled by remember { mutableStateOf(prefs.getBoolean("notifications_enabled", true)) }
    var motivationalQuotes by remember { mutableStateOf(prefs.getBoolean("motivational_quotes", true)) }
    var notificationsAllowed by remember { mutableStateOf<Boolean>(areNotificationsAllowed(context)) }
    var exactAlarmsAvailable by remember { mutableStateOf<Boolean>(canUseExactAlarms(context)) }

    // PIN состояние — вынесено наверх чтобы показывать поверх всего экрана
    var pinEnabled by remember { mutableStateOf(isPinEnabled(context)) }
    val biometricAvailable = remember { isBiometricAvailable(context) }
    var biometricEnabled by remember { mutableStateOf(isBiometricEnabledForCurrentUser(context)) }
    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val isGuest = currentUser == null || currentUser.isAnonymous
    var showSetPin by remember { mutableStateOf(false) }
    var showChangePin by remember { mutableStateOf(false) }

    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var deleteAccountError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Язык приложения
    var selectedLanguage by remember {
        mutableStateOf(LocaleHelper.getSavedLanguage(context))
    }
    var showLanguagePicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.btn_back), tint = Color.White)
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
                // Секция языка
                    SettingsSectionTitle(stringResource(R.string.settings_section_language))


                    val currentLang = LocaleHelper.findLanguage(selectedLanguage)
                SettingsClickRow(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.settings_language),
                    subtitle = "${currentLang.flag} ${currentLang.displayName}",
                    onClick = { showLanguagePicker = true }
                )

                Spacer(modifier = Modifier.height(8.dp))
                } // закрываем item языка

                item {
                // Секция уведомлений
                    SettingsSectionTitle(stringResource(R.string.settings_notifications))

                    if (!notificationsAllowed) {
                        SettingsClickRow(
                            icon = Icons.Default.Settings,
                            title = stringResource(R.string.notifications_permission_notice),
                            subtitle = stringResource(R.string.notifications_open_settings),
                            onClick = {
                                openAppNotificationSettings(context)
                                notificationsAllowed = areNotificationsAllowed(context)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (!exactAlarmsAvailable) {
                        SettingsClickRow(
                            icon = Icons.Default.Notifications,
                            title = stringResource(R.string.setup_exact_alarm_notice),
                            subtitle = stringResource(R.string.exact_alarm_open_settings),
                            onClick = {
                                openExactAlarmSettings(context)
                                exactAlarmsAvailable = canUseExactAlarms(context)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Включить/выключить уведомления
                    SettingsToggleRow(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.settings_notifications),
                        subtitle = stringResource(R.string.settings_reminder_desc),
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            notificationsEnabled = enabled
                            prefs.edit().putBoolean("notifications_enabled", enabled).apply()

                            if (enabled) {
                                scheduleDailyReminder(context, notificationHour, notificationMinute)
                            } else {
                                cancelDailyReminder(context)
                            }
                        }
                    )

                Spacer(modifier = Modifier.height(8.dp))

                // Время уведомления
                SettingsClickRow(
                    icon = Icons.Default.DateRange,
                    title = stringResource(R.string.settings_notif_time),
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
                    title = stringResource(R.string.settings_quotes),
                    subtitle = stringResource(R.string.settings_quotes_desc),
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
                    SettingsSectionTitle(stringResource(R.string.settings_security))


                    // Включить/выключить PIN — только для авторизованных (не гостей)
                if (!isGuest) {
                    SettingsToggleRow(
                        icon = Icons.Default.Lock,
                        title = stringResource(R.string.settings_pin),
                        subtitle = if (pinEnabled) stringResource(R.string.settings_pin_active)
                                   else stringResource(R.string.settings_pin_enable),
                        checked = pinEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                showSetPin = true
                            } else {
                                removePin(context)
                                setPinEnabled(context, false)
                                setBiometricEnabled(context, false)
                                pinEnabled = false
                                biometricEnabled = false
                            }
                        }
                    )
                    // Сменить PIN — только если включён
                    if (pinEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SettingsClickRow(
                            icon = Icons.Default.Lock,
                            title = stringResource(R.string.settings_pin_change),
                            subtitle = stringResource(R.string.settings_pin_set),
                            onClick = { showChangePin = true }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SettingsToggleRow(
                            icon = Icons.Default.Lock,
                            title = stringResource(R.string.settings_biometric),
                            subtitle = if (biometricAvailable) {
                                stringResource(R.string.settings_biometric_desc)
                            } else {
                                stringResource(R.string.settings_biometric_unavailable)
                            },
                            checked = biometricEnabled,
                            enabled = biometricAvailable,
                            onCheckedChange = { enabled ->
                                if (biometricAvailable) {
                                    biometricEnabled = enabled
                                    setBiometricEnabled(context, enabled)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsClickRow(
                        icon = Icons.Default.Delete,
                        title = stringResource(R.string.settings_delete_account),
                        subtitle = stringResource(R.string.settings_delete_account_desc),
                        onClick = { showDeleteAccountDialog = true }
                    )
                }

                // Раздел цитат — только в режиме админа
                if (isAdminMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsSectionTitle("💬 Цитаты для уведомлений")
                    SettingsClickRow(
                        icon = Icons.Default.Add,
                        title = stringResource(R.string.settings_manage_quotes),
                        subtitle = stringResource(R.string.settings_manage_quotes_desc),
                        onClick = { navController.navigate("quotes_admin") }
                    )

                }
                } // закрываем item

                // Кнопка поддержки
                item {
                Spacer(modifier = Modifier.height(4.dp))
                SettingsSectionTitle(stringResource(R.string.support_section))
                SettingsClickRow(
                    icon = Icons.Default.Add,
                    title = stringResource(R.string.support_menu_title),
                    subtitle = stringResource(R.string.support_menu_subtitle),
                    onClick = { navController.navigate("support") }
                )
                } // закрываем item поддержки
            } // закрываем LazyColumn
        }
    }

    // Диалог выбора языка
    if (showLanguagePicker) {
        AlertDialog(
            onDismissRequest = { showLanguagePicker = false },
            title = { Text(stringResource(R.string.settings_language)) },
            text = {
                androidx.compose.foundation.lazy.LazyColumn {
                    items(LocaleHelper.supportedLanguages.size) { index ->
                        val lang = LocaleHelper.supportedLanguages[index]
                        val isSelected = lang.code == selectedLanguage
                        Row(
                            modifier = Modifier
                                .clickable {
                                if (!isSelected) {
                                    selectedLanguage = lang.code
                                    LocaleHelper.saveLanguage(context, lang.code)
                                    showLanguagePicker = false
                                    // Перезапускаем Activity чтобы применить новый язык
                                    val intent = Intent(context, MainActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } else {
                                    showLanguagePicker = false
                                }
                            }
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(lang.flag, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = lang.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            if (isSelected) {
                                Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (index < LocaleHelper.supportedLanguages.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguagePicker = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    // PinScreen поверх всего экрана
    if (showSetPin) {
        PinScreen(
            mode = PinMode.SET,
            onSuccess = {
                pinEnabled = true
                biometricEnabled = isBiometricEnabledForCurrentUser(context)
                showSetPin = false
            },
            onCancel = { showSetPin = false }
        )
    } else if (showChangePin) {
        PinScreen(
            mode = PinMode.SET,
            onSuccess = {
                biometricEnabled = isBiometricEnabledForCurrentUser(context)
                showChangePin = false
            },
            onCancel = { showChangePin = false }
        )
    }

    // Диалоги цитат вынесены в QuotesAdminScreen
} // закрываем Box

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeletingAccount) showDeleteAccountDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(stringResource(R.string.delete_account_title)) },
            text = { Text(stringResource(R.string.delete_account_message)) },
            confirmButton = {
                TextButton(
                    enabled = !isDeletingAccount,
                    onClick = {
                        scope.launch {
                            isDeletingAccount = true
                            deleteAccountError = null

                            val result = deleteCurrentUserAccountAndData(context)

                            isDeletingAccount = false
                            showDeleteAccountDialog = false

                            result.onSuccess {
                                removePin(context)
                                setPinEnabled(context, false)

                                prefs.edit()
                                    .putBoolean("skipAuth", false)
                                    .apply()

                                val googleClient = GoogleSignIn.getClient(
                                    context,
                                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken(context.getString(R.string.default_web_client_id))
                                        .requestEmail()
                                        .build()
                                )
                                googleClient.signOut()
                                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

                                val intent = Intent(context, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                context.startActivity(intent)
                                (context as? Activity)?.finish()
                            }.onFailure { e ->
                                val msg = e.message.orEmpty()

                                deleteAccountError =
                                    if (
                                        msg.contains("recent", ignoreCase = true) ||
                                        msg.contains("credential", ignoreCase = true) ||
                                        msg.contains("login", ignoreCase = true)
                                    ) {
                                        context.getString(R.string.delete_account_error_recent_login)
                                    } else {
                                        "${context.getString(R.string.delete_account_error_generic)}: $msg"
                                    }
                            }
                        }
                    }
                ) {
                    Text(
                        text = if (isDeletingAccount) {
                            stringResource(R.string.delete_account_progress)
                        } else {
                            stringResource(R.string.delete_account_confirm)
                        },
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isDeletingAccount,
                    onClick = { showDeleteAccountDialog = false }
                ) {
                    Text(stringResource(R.string.btn_cancel))
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = !isDeletingAccount,
                dismissOnClickOutside = !isDeletingAccount
            )
        )
    }
    deleteAccountError?.let { errorText ->
        AlertDialog(
            onDismissRequest = { deleteAccountError = null },
            title = { Text(stringResource(R.string.delete_account_title)) },
            text = { Text(errorText) },
            confirmButton = {
                TextButton(onClick = { deleteAccountError = null }) {
                    Text("OK")
                }
            }
        )
    }
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
    enabled: Boolean = true,
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
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.6f else 0.4f)
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
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
