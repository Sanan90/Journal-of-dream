package com.dreamjournal.journalofdream.ui.auth

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.res.stringResource
import com.dreamjournal.journalofdream.R
import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

// SHA-256 хэш PIN — надёжнее чем hashCode()
private fun hashPin(pin: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

private const val SECURITY_PREFS = "security_prefs"
private const val LEGACY_PIN_HASH_CACHE = "pin_hash_cache"
private const val LEGACY_PIN_HASH = "pin_hash"
private const val LEGACY_PIN_ENABLED = "pin_enabled"

private fun currentSecurityOwner(): String {
    val user = FirebaseAuth.getInstance().currentUser
    return when {
        user == null -> "guest"
        user.isAnonymous -> "guest"
        else -> user.uid
    }
}

private fun pinHashCacheKey(owner: String) = "pin_hash_cache_$owner"
private fun pinHashLegacyOwnerKey(owner: String) = "pin_hash_$owner"
private fun pinEnabledKey(owner: String) = "pin_enabled_$owner"
private fun biometricEnabledKey(owner: String) = "biometric_enabled_$owner"

private fun prefs(context: Context) =
    context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE)

private fun migrateLegacyPinIfNeeded(context: Context, owner: String) {
    val prefs = prefs(context)
    if (prefs.contains(pinHashCacheKey(owner)) || prefs.contains(pinHashLegacyOwnerKey(owner))) return

    val legacyHash = prefs.getString(LEGACY_PIN_HASH_CACHE, null) ?: prefs.getString(LEGACY_PIN_HASH, null)
    val legacyEnabled = prefs.getBoolean(LEGACY_PIN_ENABLED, false)

    if (legacyHash != null) {
        prefs.edit()
            .putString(pinHashCacheKey(owner), legacyHash)
            .putBoolean(pinEnabledKey(owner), legacyEnabled)
            .remove(LEGACY_PIN_HASH_CACHE)
            .remove(LEGACY_PIN_HASH)
            .remove(LEGACY_PIN_ENABLED)
            .apply()
    }
}

// Ключ документа в Firestore: users/{uid}/settings/pin
// Для гостей — fallback в SharedPreferences
private fun getPinDocRef() =
    FirebaseAuth.getInstance().currentUser?.takeIf { !it.isAnonymous }?.uid?.let { uid ->
        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .collection("settings").document("pin")
    }

// Сохранить PIN в Firestore (или SharedPreferences для гостя)
fun savePinHash(context: Context, pin: String) {
    val owner = currentSecurityOwner()
    val prefs = prefs(context)
    val hash = hashPin(pin)
    val docRef = getPinDocRef()
    if (docRef != null) {
        docRef.set(mapOf("hash" to hash, "enabled" to true))
    } else {
        prefs.edit().putString(pinHashLegacyOwnerKey(owner), hash).apply()
    }

    prefs.edit()
        .putString(pinHashCacheKey(owner), hash)
        .putBoolean(pinEnabledKey(owner), true)
        .apply()
}

// Проверить PIN — сначала по кэшу, всегда быстро
fun checkPin(context: Context, pin: String): Boolean {
    val owner = currentSecurityOwner()
    migrateLegacyPinIfNeeded(context, owner)
    val prefs = prefs(context)
    val hash = hashPin(pin)
    val cached = prefs.getString(pinHashCacheKey(owner), null)
        ?: prefs.getString(pinHashLegacyOwnerKey(owner), null)
    return hash == cached
}

fun hasPin(context: Context): Boolean {
    val owner = currentSecurityOwner()
    migrateLegacyPinIfNeeded(context, owner)
    val prefs = prefs(context)
    return prefs.contains(pinHashCacheKey(owner)) || prefs.contains(pinHashLegacyOwnerKey(owner))
}

fun removePin(context: Context) {
    val owner = currentSecurityOwner()
    prefs(context)
        .edit()
        .remove(pinHashCacheKey(owner))
        .remove(pinHashLegacyOwnerKey(owner))
        .remove(pinEnabledKey(owner))
        .remove(biometricEnabledKey(owner))
        .apply()
    getPinDocRef()?.delete()
}

fun isPinEnabled(context: Context): Boolean {
    val owner = currentSecurityOwner()
    migrateLegacyPinIfNeeded(context, owner)
    return prefs(context).getBoolean(pinEnabledKey(owner), false)
}

fun setPinEnabled(context: Context, enabled: Boolean) {
    val owner = currentSecurityOwner()
    prefs(context)
        .edit().putBoolean(pinEnabledKey(owner), enabled).apply()
    if (!enabled) {
        setBiometricEnabled(context, false)
        getPinDocRef()?.update("enabled", false)
    }
}

fun isBiometricEnabledForCurrentUser(context: Context): Boolean {
    val owner = currentSecurityOwner()
    return prefs(context).getBoolean(biometricEnabledKey(owner), false)
}

fun setBiometricEnabled(context: Context, enabled: Boolean) {
    val owner = currentSecurityOwner()
    prefs(context).edit().putBoolean(biometricEnabledKey(owner), enabled).apply()
}

// Синхронизировать PIN из Firestore в локальный кэш (вызывать при логине)
suspend fun syncPinFromFirestore(context: Context) {
    val owner = currentSecurityOwner()
    try {
        val doc = getPinDocRef()?.get()?.await() ?: run {
            migrateLegacyPinIfNeeded(context, owner)
            return
        }
        val hash = doc.getString("hash") ?: return
        val enabled = doc.getBoolean("enabled") ?: false
        prefs(context)
            .edit()
            .putString(pinHashCacheKey(owner), hash)
            .putBoolean(pinEnabledKey(owner), enabled)
            .apply()
    } catch (e: Exception) {
        migrateLegacyPinIfNeeded(context, owner)
    }
}

fun isBiometricAvailable(context: Context): Boolean {
    val bm = BiometricManager.from(context)
    return bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
           BiometricManager.BIOMETRIC_SUCCESS
}

fun showBiometricPrompt(context: Context, onSuccess: () -> Unit) {
    val activity = context as? androidx.fragment.app.FragmentActivity ?: return
    val executor = ContextCompat.getMainExecutor(context)
    val prompt = BiometricPrompt(
        activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
        }
    )
    prompt.authenticate(
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.biometric_prompt_title))
            .setSubtitle(context.getString(R.string.biometric_prompt_subtitle))
            .setNegativeButtonText(context.getString(R.string.pin_enter))
            .build()
    )
}

enum class PinMode { SET, ENTER }

@Composable
fun PinScreen(
    mode: PinMode,
    onSuccess: () -> Unit,
    onCancel: (() -> Unit)? = null,
    onSkip: (() -> Unit)? = null,       // только для SET при первой установке
    onForgotPin: (() -> Unit)? = null   // сброс PIN с выходом из аккаунта
) {
    val context = LocalContext.current
    var enteredPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var shake by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var showBiometricOptInDialog by remember { mutableStateOf(false) }

    val pinLength = 4

    if (showBiometricOptInDialog) {
        AlertDialog(
            onDismissRequest = {
                setBiometricEnabled(context, false)
                showBiometricOptInDialog = false
                onSuccess()
            },
            title = { Text(stringResource(R.string.biometric_enable_title)) },
            text = { Text(stringResource(R.string.biometric_enable_message)) },
            confirmButton = {
                TextButton(onClick = {
                    setBiometricEnabled(context, true)
                    showBiometricOptInDialog = false
                    onSuccess()
                }) {
                    Text(stringResource(R.string.btn_enable))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    setBiometricEnabled(context, false)
                    showBiometricOptInDialog = false
                    onSuccess()
                }) {
                    Text(stringResource(R.string.btn_not_now))
                }
            }
        )
    }

    // Диалог сброса PIN
    if (showForgotDialog) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val isGuest = currentUser == null || currentUser.isAnonymous
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text(stringResource(R.string.pin_forgot_title)) },
            text = {
                Text(
                    if (isGuest) stringResource(R.string.pin_forgot_guest_message)
                    else stringResource(R.string.pin_forgot_message)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showForgotDialog = false
                    removePin(context)
                    FirebaseAuth.getInstance().signOut()
                    // onForgotPin делает recreate() — полный перезапуск на экран авторизации
                    (onForgotPin ?: onSuccess).invoke()
                }) {
                    Text(
                        if (isGuest) stringResource(R.string.pin_forgot_exit)
                        else stringResource(R.string.pin_forgot_reset),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    val shakeOffset by animateFloatAsState(
        targetValue = if (shake) 10f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        finishedListener = { shake = false },
        label = "shake"
    )

    LaunchedEffect(Unit) {
        if (mode == PinMode.ENTER && isBiometricEnabledForCurrentUser(context) && isBiometricAvailable(context)) {
            showBiometricPrompt(context, onSuccess)
        }
    }

    fun handleDigit(digit: String) {
        if (enteredPin.length >= pinLength) return
        enteredPin += digit
        errorMessage = null

        if (enteredPin.length == pinLength) {
            when (mode) {
                PinMode.ENTER -> {
                    if (checkPin(context, enteredPin)) {
                        onSuccess()
                    } else {
                        errorMessage = context.getString(R.string.pin_error_invalid)
                        shake = true
                        enteredPin = ""
                    }
                }
                PinMode.SET -> {
                    if (!isConfirming) {
                        confirmPin = enteredPin
                        enteredPin = ""
                        isConfirming = true
                    } else {
                        if (enteredPin == confirmPin) {
                            savePinHash(context, enteredPin)
                            setPinEnabled(context, true)
                            if (isBiometricAvailable(context)) {
                                showBiometricOptInDialog = true
                            } else {
                                setBiometricEnabled(context, false)
                                onSuccess()
                            }
                        } else {
                            errorMessage = context.getString(R.string.pin_error_mismatch)
                            shake = true
                            enteredPin = ""
                            isConfirming = false
                            confirmPin = ""
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.new_fon), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when {
                    mode == PinMode.SET && !isConfirming -> stringResource(R.string.pin_create)
                    mode == PinMode.SET && isConfirming -> stringResource(R.string.pin_repeat)
                    else -> stringResource(R.string.pin_enter_title)
                },
                color = Color(0xFFF0D68C),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color.Red.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Точки
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.offset(x = shakeOffset.dp)
            ) {
                repeat(pinLength) { index ->
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < enteredPin.length) Color.White
                                else Color.White.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Клавиатура
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("bio", "0", "del")
            )

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                keys.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        row.forEach { key ->
                            when (key) {
                                "bio" -> {
                                    if (mode == PinMode.ENTER && isBiometricEnabledForCurrentUser(context) && isBiometricAvailable(context)) {
                                        PinKey(onClick = { showBiometricPrompt(context, onSuccess) }) {
                                            Text("👆", fontSize = 26.sp)
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.size(72.dp))
                                    }
                                }
                                "del" -> PinKey(onClick = {
                                    if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                }) {
                                    Text("⌫", fontSize = 22.sp, color = Color.White)
                                }
                                else -> PinKey(onClick = { handleDigit(key) }) {
                                    Text(key, fontSize = 26.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопки внизу
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (onCancel != null) {
                    TextButton(onClick = onCancel) {
                        Text(stringResource(R.string.btn_cancel), color = Color.White.copy(alpha = 0.7f))
                    }
                }
                if (onSkip != null) {
                    TextButton(onClick = onSkip) {
                        Text(stringResource(R.string.onboarding_skip), color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }

            // Кнопка "Забыл PIN" — только при вводе
            if (mode == PinMode.ENTER) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { showForgotDialog = true }) {
                    Text(
                        text = stringResource(R.string.pin_forgot_btn),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PinKey(onClick: () -> Unit, content: @Composable () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFF2E1650).copy(alpha = 0.70f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0D68C).copy(alpha = 0.30f)),
        contentPadding = PaddingValues(0.dp)
    ) { content() }
}
