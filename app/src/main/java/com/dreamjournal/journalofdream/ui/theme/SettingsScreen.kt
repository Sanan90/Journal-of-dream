package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.ui.res.stringResource
import com.dreamjournal.journalofdream.R
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
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
import com.dreamjournal.journalofdream.util.cancelDailyReminder
import com.dreamjournal.journalofdream.util.cancelQuoteAlarms
import com.dreamjournal.journalofdream.util.cancelRealityCheck
import com.dreamjournal.journalofdream.util.scheduleDailyReminder
import com.dreamjournal.journalofdream.util.scheduleQuoteAlarms
import com.dreamjournal.journalofdream.util.scheduleRealityCheck
import com.dreamjournal.journalofdream.util.areNotificationsAllowed
import com.dreamjournal.journalofdream.util.canUseExactAlarms
import com.dreamjournal.journalofdream.util.openAppNotificationSettings
import com.dreamjournal.journalofdream.util.openExactAlarmSettings
import com.dreamjournal.journalofdream.util.deleteCurrentUserAccountAndData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch

private val GoldLight      = Color(0xFFF0D68C)
private val GoldDark       = Color(0xFFD4A76A)
private val DangerRed      = Color(0xFFEF5350)
private val PlayfairFamily = FontFamily(Font(R.font.playfair_display_bold, FontWeight.Bold))

private val RowGradient = Brush.verticalGradient(
    listOf(Color(0xFF3B1A58).copy(alpha = 0.75f), Color(0xFF1A0C30).copy(alpha = 0.88f))
)
private val DangerGradient = Brush.verticalGradient(
    listOf(Color(0xFF4A1020).copy(alpha = 0.80f), Color(0xFF1A0808).copy(alpha = 0.90f))
)

@Composable
fun SettingsScreen(
    navController: NavHostController,
    isAdminMode: Boolean = false,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs   = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var notificationHour   by remember { mutableStateOf(prefs.getInt("notification_hour", 8)) }
    var notificationMinute by remember { mutableStateOf(prefs.getInt("notification_minute", 0)) }
    var notificationsEnabled by remember { mutableStateOf(prefs.getBoolean("notifications_enabled", true)) }
    var motivationalQuotes   by remember { mutableStateOf(prefs.getBoolean("motivational_quotes", true)) }
    var notificationsAllowed by remember { mutableStateOf(areNotificationsAllowed(context)) }
    var exactAlarmsAvailable by remember { mutableStateOf(canUseExactAlarms(context)) }
    var realityCheckEnabled    by remember { mutableStateOf(prefs.getBoolean("reality_check_enabled", false)) }
    var realityIntervalMinutes by remember { mutableStateOf(prefs.getInt("reality_check_interval_minutes", 60)) }
    var realityStartHour   by remember { mutableStateOf(prefs.getInt("reality_check_start_hour", 8)) }
    var realityStartMinute by remember { mutableStateOf(prefs.getInt("reality_check_start_minute", 0)) }
    var realityEndHour     by remember { mutableStateOf(prefs.getInt("reality_check_end_hour", 22)) }
    var realityEndMinute   by remember { mutableStateOf(prefs.getInt("reality_check_end_minute", 0)) }
    var showRealityIntervalDialog by remember { mutableStateOf(false) }

    var pinEnabled       by remember { mutableStateOf(isPinEnabled(context)) }
    val biometricAvailable = remember { isBiometricAvailable(context) }
    var biometricEnabled by remember { mutableStateOf(isBiometricEnabledForCurrentUser(context)) }
    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val isGuest = currentUser == null || currentUser.isAnonymous
    var showSetPin    by remember { mutableStateOf(false) }
    var showChangePin by remember { mutableStateOf(false) }

    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showLogoutDialog        by remember { mutableStateOf(false) }
    var isDeletingAccount       by remember { mutableStateOf(false) }
    var deleteAccountError      by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    var selectedLanguage    by remember { mutableStateOf(LocaleHelper.getSavedLanguage(context)) }
    var showLanguagePicker  by remember { mutableStateOf(false) }

    // ── Layout ────────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.new_fon), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {

            // ── Заголовок ──
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, stringResource(R.string.btn_back),
                        tint = GoldLight, modifier = Modifier.size(28.dp))
                }
                Text(stringResource(R.string.settings_title), color = GoldLight,
                    fontFamily = PlayfairFamily, fontWeight = FontWeight.Bold,
                    fontSize = 30.sp, modifier = Modifier.weight(1f))
                Image(painterResource(R.drawable.setting_icon2), null,
                    Modifier.size(34.dp).padding(end = 10.dp), contentScale = ContentScale.Fit)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 40.dp)
            ) {

                // ════ ЯЗЫК ════
                item { SettingsSection(stringResource(R.string.settings_section_language)) }
                item {
                    val currentLang = LocaleHelper.findLanguage(selectedLanguage)
                    SettingsClickRow(
                        iconRes = R.drawable.settings_language,
                        title   = stringResource(R.string.settings_language),
                        subtitle = "${currentLang.flag} ${currentLang.displayName}",
                        onClick = { showLanguagePicker = true }
                    )
                }

                // ════ УВЕДОМЛЕНИЯ ════
                item { SettingsSection(stringResource(R.string.settings_notifications)) }

                if (!notificationsAllowed) {
                    item {
                        SettingsClickRow(
                            iconRes  = R.drawable.settings_notification,
                            title    = stringResource(R.string.notifications_permission_notice),
                            subtitle = stringResource(R.string.notifications_open_settings),
                            isDanger = false,
                            onClick  = { openAppNotificationSettings(context); notificationsAllowed = areNotificationsAllowed(context) }
                        )
                    }
                }
                if (!exactAlarmsAvailable) {
                    item {
                        SettingsClickRow(
                            iconRes  = R.drawable.settings_notification,
                            title    = stringResource(R.string.setup_exact_alarm_notice),
                            subtitle = stringResource(R.string.exact_alarm_open_settings),
                            onClick  = { openExactAlarmSettings(context); exactAlarmsAvailable = canUseExactAlarms(context) }
                        )
                    }
                }

                item {
                    SettingsToggleRow(
                        iconRes  = R.drawable.settings_notification,
                        title    = stringResource(R.string.settings_notifications),
                        subtitle = stringResource(R.string.settings_reminder_desc),
                        checked  = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            notificationsEnabled = enabled
                            prefs.edit().putBoolean("notifications_enabled", enabled).apply()
                            if (enabled) scheduleDailyReminder(context, notificationHour, notificationMinute)
                            else cancelDailyReminder(context)
                        }
                    )
                }
                item {
                    SettingsClickRow(
                        iconRes  = R.drawable.settings_time,
                        title    = stringResource(R.string.settings_notif_time),
                        subtitle = String.format("%02d:%02d", notificationHour, notificationMinute),
                        enabled  = notificationsEnabled,
                        onClick  = {
                            TimePickerDialog(context, { _, h, m ->
                                notificationHour = h; notificationMinute = m
                                prefs.edit().putInt("notification_hour", h).putInt("notification_minute", m).apply()
                                if (notificationsEnabled) scheduleDailyReminder(context, h, m)
                            }, notificationHour, notificationMinute, true).show()
                        }
                    )
                }
                item {
                    SettingsToggleRow(
                        iconRes  = R.drawable.settings_quotes,
                        title    = stringResource(R.string.settings_quotes),
                        subtitle = stringResource(R.string.settings_quotes_desc),
                        checked  = motivationalQuotes,
                        onCheckedChange = { enabled ->
                            motivationalQuotes = enabled
                            prefs.edit().putBoolean("motivational_quotes", enabled).apply()
                            if (enabled) scheduleQuoteAlarms(context) else cancelQuoteAlarms(context)
                        }
                    )
                }

                // ════ ПРОВЕРКА РЕАЛЬНОСТИ ════
                item { SettingsSection(stringResource(R.string.settings_reality_check)) }
                item {
                    SettingsToggleRow(
                        iconRes  = R.drawable.settings_reality,
                        title    = stringResource(R.string.settings_reality_check),
                        subtitle = stringResource(R.string.settings_reality_check_desc),
                        checked  = realityCheckEnabled,
                        onCheckedChange = { enabled ->
                            realityCheckEnabled = enabled
                            prefs.edit().putBoolean("reality_check_enabled", enabled).apply()
                            if (enabled) scheduleRealityCheck(context) else cancelRealityCheck(context)
                        }
                    )
                }
                item {
                    SettingsClickRow(
                        iconRes  = R.drawable.settings_interval,
                        title    = stringResource(R.string.settings_reality_interval),
                        subtitle = when (realityIntervalMinutes) {
                            30   -> stringResource(R.string.reality_interval_30m)
                            120  -> stringResource(R.string.reality_interval_2h)
                            else -> stringResource(R.string.reality_interval_1h)
                        },
                        enabled = realityCheckEnabled,
                        onClick = { showRealityIntervalDialog = true }
                    )
                }
                item {
                    SettingsClickRow(
                        iconRes  = R.drawable.settings_time,
                        title    = stringResource(R.string.settings_reality_from),
                        subtitle = String.format("%02d:%02d", realityStartHour, realityStartMinute),
                        enabled  = realityCheckEnabled,
                        onClick  = {
                            TimePickerDialog(context, { _, h, m ->
                                realityStartHour = h; realityStartMinute = m
                                prefs.edit().putInt("reality_check_start_hour", h).putInt("reality_check_start_minute", m).apply()
                                if (realityCheckEnabled) scheduleRealityCheck(context)
                            }, realityStartHour, realityStartMinute, true).show()
                        }
                    )
                }
                item {
                    SettingsClickRow(
                        iconRes  = R.drawable.settings_time,
                        title    = stringResource(R.string.settings_reality_to),
                        subtitle = String.format("%02d:%02d", realityEndHour, realityEndMinute),
                        enabled  = realityCheckEnabled,
                        onClick  = {
                            TimePickerDialog(context, { _, h, m ->
                                realityEndHour = h; realityEndMinute = m
                                prefs.edit().putInt("reality_check_end_hour", h).putInt("reality_check_end_minute", m).apply()
                                if (realityCheckEnabled) scheduleRealityCheck(context)
                            }, realityEndHour, realityEndMinute, true).show()
                        }
                    )
                }

                // ════ БЕЗОПАСНОСТЬ ════
                item { SettingsSection(stringResource(R.string.settings_security)) }
                if (!isGuest) {
                    item {
                        SettingsToggleRow(
                            iconRes  = R.drawable.settings_pin,
                            title    = stringResource(R.string.settings_pin),
                            subtitle = if (pinEnabled) stringResource(R.string.settings_pin_active)
                                       else stringResource(R.string.settings_pin_enable),
                            checked  = pinEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) showSetPin = true
                                else {
                                    removePin(context); setPinEnabled(context, false)
                                    setBiometricEnabled(context, false)
                                    pinEnabled = false; biometricEnabled = false
                                }
                            }
                        )
                    }
                    if (pinEnabled) {
                        item {
                            SettingsClickRow(
                                iconRes  = R.drawable.settings_pin,
                                title    = stringResource(R.string.settings_pin_change),
                                subtitle = stringResource(R.string.settings_pin_set),
                                onClick  = { showChangePin = true }
                            )
                        }
                        item {
                            SettingsToggleRow(
                                iconRes  = R.drawable.settings_pin,
                                title    = stringResource(R.string.settings_biometric),
                                subtitle = if (biometricAvailable) stringResource(R.string.settings_biometric_desc)
                                           else stringResource(R.string.settings_biometric_unavailable),
                                checked  = biometricEnabled,
                                enabled  = biometricAvailable,
                                onCheckedChange = { enabled ->
                                    if (biometricAvailable) { biometricEnabled = enabled; setBiometricEnabled(context, enabled) }
                                }
                            )
                        }
                    }
                }

                // ════ АККАУНТ ════
                item { SettingsSection(stringResource(R.string.settings_section_account) ) }
                if (!isGuest) {
                    item {
                        SettingsClickRow(
                            iconRes  = R.drawable.settings_logout,
                            title    = stringResource(R.string.btn_logout),
                            subtitle = stringResource(R.string.dialog_logout_message),
                            isDanger = false,
                            onClick  = { showLogoutDialog = true }
                        )
                    }
                    item {
                        SettingsClickRow(
                            iconRes  = R.drawable.settings_delete,
                            title    = stringResource(R.string.settings_delete_account),
                            subtitle = stringResource(R.string.settings_delete_account_desc),
                            isDanger = true,
                            onClick  = { showDeleteAccountDialog = true }
                        )
                    }
                }

                // ════ ПОДДЕРЖКА ════
                item { SettingsSection(stringResource(R.string.support_section)) }
                item {
                    SettingsClickRow(
                        iconRes  = R.drawable.settings_coffee,
                        title    = stringResource(R.string.support_menu_title),
                        subtitle = stringResource(R.string.support_menu_subtitle),
                        onClick  = { navController.navigate("support") }
                    )
                }

                // ════ АДМИН: цитаты ════
                if (isAdminMode) {
                    item { SettingsSection("💬 Цитаты для уведомлений") }
                    item {
                        SettingsClickRow(
                            iconRes  = R.drawable.settings_admin_quotes,
                            title    = stringResource(R.string.settings_manage_quotes),
                            subtitle = stringResource(R.string.settings_manage_quotes_desc),
                            onClick  = { navController.navigate("quotes_admin") }
                        )
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }

        // ── PinScreen поверх всего ──
        if (showSetPin) {
            PinScreen(mode = PinMode.SET,
                onSuccess = { pinEnabled = true; biometricEnabled = isBiometricEnabledForCurrentUser(context); showSetPin = false },
                onCancel = { showSetPin = false })
        } else if (showChangePin) {
            PinScreen(mode = PinMode.SET,
                onSuccess = { biometricEnabled = isBiometricEnabledForCurrentUser(context); showChangePin = false },
                onCancel = { showChangePin = false })
        }
    }

    // ── Диалог выхода ──
    if (showLogoutDialog) {
        StyledAlertDialog(
            title    = stringResource(R.string.dialog_logout_title),
            message  = stringResource(R.string.dialog_logout_message),
            confirmText = stringResource(R.string.btn_logout),
            isDanger = false,
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    // ── Диалог выбора языка ──
    if (showLanguagePicker) {
        AlertDialog(
            onDismissRequest = { showLanguagePicker = false },
            title   = { Text(stringResource(R.string.settings_language), color = GoldLight, fontFamily = PlayfairFamily, fontWeight = FontWeight.Bold) },
            text    = {
                androidx.compose.foundation.lazy.LazyColumn {
                    items(LocaleHelper.supportedLanguages.size) { index ->
                        val lang       = LocaleHelper.supportedLanguages[index]
                        val isSelected = lang.code == selectedLanguage
                        Row(
                            modifier = Modifier
                                .clickable {
                                    selectedLanguage = lang.code
                                    LocaleHelper.saveLanguage(context, lang.code)
                                    showLanguagePicker = false
                                    val intent = Intent(context, MainActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                }
                                .fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(lang.flag, fontSize = 22.sp)
                                Spacer(Modifier.width(12.dp))
                                Text(lang.displayName, color = Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                            if (isSelected) Text("✓", color = GoldLight, fontWeight = FontWeight.Bold)
                        }
                        if (index < LocaleHelper.supportedLanguages.size - 1)
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showLanguagePicker = false }) { Text(stringResource(R.string.btn_cancel), color = GoldLight) } },
            containerColor = Color(0xFF2A1548),
            titleContentColor = GoldLight
        )
    }

    // ── Диалог удаления аккаунта ──
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeletingAccount) showDeleteAccountDialog = false },
            icon    = { Icon(Icons.Default.Warning, null, tint = DangerRed) },
            title   = { Text(stringResource(R.string.delete_account_title), color = DangerRed, fontFamily = PlayfairFamily, fontWeight = FontWeight.Bold) },
            text    = { Text(stringResource(R.string.delete_account_message), color = Color.White.copy(0.85f)) },
            confirmButton = {
                TextButton(enabled = !isDeletingAccount, onClick = {
                    scope.launch {
                        isDeletingAccount = true; deleteAccountError = null
                        val result = deleteCurrentUserAccountAndData(context)
                        isDeletingAccount = false; showDeleteAccountDialog = false
                        result.onSuccess {
                            removePin(context); setPinEnabled(context, false)
                            prefs.edit().putBoolean("skipAuth", false).apply()
                            val googleClient = GoogleSignIn.getClient(context,
                                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(context.getString(R.string.default_web_client_id))
                                    .requestEmail().build())
                            googleClient.signOut()
                            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                            val intent = Intent(context, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            context.startActivity(intent)
                            (context as? Activity)?.finish()
                        }.onFailure { e ->
                            val msg = e.message.orEmpty()
                            deleteAccountError = if (msg.contains("recent", true) || msg.contains("credential", true) || msg.contains("login", true))
                                context.getString(R.string.delete_account_error_recent_login)
                            else "${context.getString(R.string.delete_account_error_generic)}: $msg"
                        }
                    }
                }) {
                    Text(if (isDeletingAccount) stringResource(R.string.delete_account_progress)
                         else stringResource(R.string.delete_account_confirm), color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(enabled = !isDeletingAccount, onClick = { showDeleteAccountDialog = false }) {
                    Text(stringResource(R.string.btn_cancel), color = GoldLight)
                }
            },
            containerColor = Color(0xFF2A1548),
            properties = DialogProperties(dismissOnBackPress = !isDeletingAccount, dismissOnClickOutside = !isDeletingAccount)
        )
    }

    // ── Диалог интервала проверки реальности ──
    if (showRealityIntervalDialog) {
        AlertDialog(
            onDismissRequest = { showRealityIntervalDialog = false },
            title   = { Text(stringResource(R.string.settings_reality_interval), color = GoldLight, fontFamily = PlayfairFamily, fontWeight = FontWeight.Bold) },
            text    = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(30, 60, 120).forEach { value ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    realityIntervalMinutes = value
                                    prefs.edit().putInt("reality_check_interval_minutes", value).apply()
                                    if (realityCheckEnabled) scheduleRealityCheck(context)
                                    showRealityIntervalDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = realityIntervalMinutes == value,
                                onClick = {
                                    realityIntervalMinutes = value
                                    prefs.edit().putInt("reality_check_interval_minutes", value).apply()
                                    if (realityCheckEnabled) scheduleRealityCheck(context)
                                    showRealityIntervalDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = GoldLight, unselectedColor = Color.White.copy(0.50f)))
                            Text(when (value) {
                                30   -> stringResource(R.string.reality_interval_30m)
                                120  -> stringResource(R.string.reality_interval_2h)
                                else -> stringResource(R.string.reality_interval_1h)
                            }, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showRealityIntervalDialog = false }) { Text(stringResource(R.string.btn_ok), color = GoldLight) } },
            containerColor = Color(0xFF2A1548)
        )
    }

    // ── Ошибка удаления аккаунта ──
    deleteAccountError?.let { errorText ->
        AlertDialog(
            onDismissRequest = { deleteAccountError = null },
            title   = { Text(stringResource(R.string.delete_account_title), color = DangerRed) },
            text    = { Text(errorText, color = Color.White.copy(0.85f)) },
            confirmButton = { TextButton(onClick = { deleteAccountError = null }) { Text("OK", color = GoldLight) } },
            containerColor = Color(0xFF2A1548)
        )
    }
}

// ─── Заголовок секции ─────────────────────────────────────────────────────────
@Composable
fun SettingsSection(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(3.dp).height(20.dp)
            .background(Brush.verticalGradient(listOf(GoldLight, GoldDark)), RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(10.dp))
        Text(title, color = GoldLight, fontFamily = PlayfairFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

// ─── Строка с переключателем ──────────────────────────────────────────────────
@Composable
fun SettingsToggleRow(
    iconRes: Int,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(RowGradient)
            .border(1.dp, GoldLight.copy(if (enabled) 0.22f else 0.08f), RoundedCornerShape(16.dp))
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(iconRes), null,
                Modifier.size(28.dp).let { if (!enabled) it.then(Modifier) else it },
                contentScale = ContentScale.Fit,
                alpha = if (enabled) 1f else 0.40f)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = if (enabled) Color.White else Color.White.copy(0.45f),
                    fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(subtitle, color = Color.White.copy(if (enabled) 0.58f else 0.30f), fontSize = 12.sp, lineHeight = 16.sp)
            }
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = checked, onCheckedChange = onCheckedChange, enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor      = GoldLight,
                    checkedTrackColor      = Color(0xFF7B3FA0),
                    checkedBorderColor     = GoldLight.copy(0.50f),
                    uncheckedThumbColor    = Color.White.copy(0.60f),
                    uncheckedTrackColor    = Color.White.copy(0.12f),
                    uncheckedBorderColor   = Color.White.copy(0.20f),
                    disabledCheckedThumbColor   = GoldLight.copy(0.35f),
                    disabledUncheckedThumbColor = Color.White.copy(0.25f)
                )
            )
        }
    }
}

// ─── Кликабельная строка ──────────────────────────────────────────────────────
@Composable
fun SettingsClickRow(
    iconRes: Int,
    title: String,
    subtitle: String,
    enabled: Boolean = true,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    val gradient = if (isDanger) DangerGradient else RowGradient
    val borderColor = when {
        isDanger  -> DangerRed.copy(0.45f)
        !enabled  -> Color.White.copy(0.06f)
        else      -> GoldLight.copy(0.22f)
    }

    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(iconRes), null, Modifier.size(28.dp),
                contentScale = ContentScale.Fit, alpha = if (enabled) 1f else 0.35f)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title,
                    color = if (isDanger) DangerRed else if (enabled) Color.White else Color.White.copy(0.40f),
                    fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(subtitle,
                    color = if (isDanger) DangerRed.copy(0.70f) else Color.White.copy(if (enabled) 0.55f else 0.28f),
                    fontSize = 12.sp, lineHeight = 16.sp)
            }
            // Стрелка вправо
            Text("›", color = if (isDanger) DangerRed.copy(0.60f) else GoldLight.copy(if (enabled) 0.55f else 0.20f),
                fontSize = 22.sp, fontWeight = FontWeight.Light)
        }
    }
}

// ─── Стилизованный диалог ─────────────────────────────────────────────────────
@Composable
private fun StyledAlertDialog(
    title: String,
    message: String,
    confirmText: String,
    isDanger: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text(title, color = if (isDanger) DangerRed else GoldLight, fontFamily = PlayfairFamily, fontWeight = FontWeight.Bold) },
        text    = { Text(message, color = Color.White.copy(0.85f)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = if (isDanger) DangerRed else GoldLight, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel), color = Color.White.copy(0.70f)) } },
        containerColor = Color(0xFF2A1548)
    )
}

// ─── Алиасы для совместимости ─────────────────────────────────────────────────
@Composable
fun SettingsSectionTitle(title: String) = SettingsSection(title)
