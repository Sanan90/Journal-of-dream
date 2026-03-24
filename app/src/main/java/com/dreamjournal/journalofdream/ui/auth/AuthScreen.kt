package com.dreamjournal.journalofdream.ui.auth

import androidx.compose.ui.res.stringResource
import android.app.Activity
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dreamjournal.journalofdream.R
import com.dreamjournal.journalofdream.viewmodel.DreamViewModel
import com.dreamjournal.journalofdream.viewmodel.LocationViewModel
import com.dreamjournal.journalofdream.viewmodel.CharacterViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest

private val GoldLight = Color(0xFFF0D68C)
private val GoldDark = Color(0xFFD4A76A)
private val AccentPurple = Color(0xFF9C6ADE)
private val AccentPurpleDark = Color(0xFF7C4DFF)

private val PlayfairFamily = FontFamily(
    Font(R.font.playfair_display_bold, FontWeight.Bold)
)

@Composable
fun AuthScreen(
    dreamViewModel: DreamViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(),
    characterViewModel: CharacterViewModel = viewModel(),
    onAuthSuccess: () -> Unit,
    onSkipAuth: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val activity = context as? Activity
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val prefs = context.getSharedPreferences("app_prefs", Activity.MODE_PRIVATE)
    val hasAuthHistory = remember { prefs.getBoolean("has_auth_history", false) }
    var isRegisterMode by remember { mutableStateOf(!hasAuthHistory) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val strNameError = stringResource(R.string.auth_name_error)
    val strEmailError = stringResource(R.string.auth_email_error)
    val strPasswordError = stringResource(R.string.auth_password_error)
    val strWrongCreds = stringResource(R.string.auth_wrong_creds)
    val strResetSent = stringResource(R.string.auth_reset_sent)
    val strVerifySent = stringResource(R.string.auth_verify_sent)
    val strNotVerified = stringResource(R.string.auth_not_verified)

    fun validate(): Boolean {
        var ok = true
        if (isRegisterMode && name.isBlank()) { nameError = strNameError; ok = false } else nameError = null
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) { emailError = strEmailError; ok = false } else emailError = null
        if (password.length < 6) { passwordError = strPasswordError; ok = false } else passwordError = null
        return ok
    }

    fun clearErrors() { nameError = null; emailError = null; passwordError = null; errorMessage = null; successMessage = null }

    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(Exception::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            isLoading = true
            auth.signInWithCredential(credential).addOnCompleteListener { res ->
                isLoading = false
                if (res.isSuccessful) {
                    prefs.edit().putBoolean("has_auth_history", true).apply()
                    auth.currentUser?.let {
                        locationViewModel.onUserLogin(it)
                        characterViewModel.onUserLogin(it)
                        dreamViewModel.onUserLogin()
                    }
                    onAuthSuccess()
                } else { errorMessage = res.exception?.message }
            }
        } catch (e: Exception) { isLoading = false; errorMessage = e.message }
    }

    // Пульсирующее свечение
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by infiniteTransition.animateFloat(
        0.8f, 1.2f,
        infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "s"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        0.08f, 0.25f,
        infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "a"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // ═══ ФОН ═══
        Image(
            painter = painterResource(R.drawable.new_fon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Свечение
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier
                    .padding(top = 50.dp)
                    .size(300.dp)
                    .scale(glowScale)
                    .alpha(glowAlpha)
                    .background(Brush.radialGradient(listOf(AccentPurple, Color.Transparent)), CircleShape)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(50.dp))

            // Полумесяц
            Image(
                painter = painterResource(R.drawable.elegant_golden_moon),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )

            Spacer(Modifier.height(12.dp))

            // "Дневник снов"
            Text(
                stringResource(R.string.nav_diary),
                fontFamily = PlayfairFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = GoldLight,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )

            Spacer(Modifier.height(12.dp))

            // Заголовок
            AnimatedContent(
                targetState = isRegisterMode,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "title"
            ) { isReg ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(if (isReg) R.string.auth_title_register else R.string.auth_title_login),
                        fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = Color.White, textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(if (isReg) R.string.auth_subtitle_register else R.string.auth_subtitle_login),
                        fontSize = 14.sp, color = Color.White.copy(alpha = 0.55f), textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ═══ СТЕКЛЯННАЯ КАРТОЧКА ═══
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2A1F5E).copy(alpha = 0.5f), Color(0xFF1A1040).copy(alpha = 0.6f))
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.12f))
                        ),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 22.dp)
            ) {
                Column {
                    // Имя — только регистрация
                    AnimatedVisibility(
                        visible = isRegisterMode,
                        enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                        exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                    ) {
                        Column {
                            AuthTextField(
                                value = name, onValueChange = { name = it; nameError = null; errorMessage = null },
                                label = stringResource(R.string.auth_name), icon = Icons.Default.Person,
                                isError = nameError != null, errorText = nameError
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }

                    // Email
                    AuthTextField(
                        value = email, onValueChange = { email = it; emailError = null; errorMessage = null },
                        label = stringResource(R.string.auth_email), icon = Icons.Default.Email,
                        isError = emailError != null, errorText = emailError
                    )
                    Spacer(Modifier.height(12.dp))

                    // Пароль
                    AuthTextField(
                        value = password, onValueChange = { password = it; passwordError = null; errorMessage = null },
                        label = stringResource(R.string.auth_password), icon = Icons.Default.Lock,
                        isError = passwordError != null, errorText = passwordError,
                        isPassword = true, passwordVisible = passwordVisible,
                        onTogglePassword = { passwordVisible = !passwordVisible }
                    )

                    // Забыли пароль
                    AnimatedVisibility(visible = !isRegisterMode) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            TextButton(onClick = {
                                if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) emailError = strEmailError
                                else auth.sendPasswordResetEmail(email.trim()).addOnCompleteListener { task ->
                                    if (task.isSuccessful) successMessage = "$strResetSent ${email.trim()}"
                                    else errorMessage = task.exception?.message
                                }
                            }) {
                                Text(stringResource(R.string.auth_forgot), color = GoldDark.copy(alpha = 0.7f), fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Сообщения
                    errorMessage?.let {
                        Text(it, color = Color(0xFFFF6B6B), fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        Spacer(Modifier.height(6.dp))
                    }
                    successMessage?.let {
                        Text(it, color = Color(0xFF81C784), fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        Spacer(Modifier.height(6.dp))
                    }

                    // ═══ КНОПКА ВОЙТИ — с космическим фоном ═══
                    Button(
                        onClick = {
                            if (!validate()) return@Button
                            keyboardController?.hide(); focusManager.clearFocus()
                            isLoading = true
                            if (isRegisterMode) {
                                auth.createUserWithEmailAndPassword(email.trim(), password)
                                    .addOnCompleteListener { res ->
                                        if (res.isSuccessful) {
                                            val profileUpdate = UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()
                                            auth.currentUser?.updateProfile(profileUpdate)?.addOnCompleteListener {
                                                prefs.edit().putBoolean("has_auth_history", true).apply()
                                                auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                                                    isLoading = false
                                                    if (verifyTask.isSuccessful) {
                                                        prefs.edit().putBoolean("has_auth_history", true).apply()
                                                        isRegisterMode = false; password = ""
                                                        successMessage = "$strVerifySent ${email.trim()}"
                                                        auth.signOut()
                                                    } else errorMessage = verifyTask.exception?.message ?: "Failed to send verification email"
                                                } ?: run { isLoading = false; errorMessage = "Failed to send verification email" }
                                            }
                                        } else { isLoading = false; errorMessage = res.exception?.message }
                                    }
                            } else {
                                auth.signInWithEmailAndPassword(email.trim(), password)
                                    .addOnCompleteListener { res ->
                                        if (res.isSuccessful) {
                                            auth.currentUser?.reload()?.addOnCompleteListener {
                                                isLoading = false
                                                val user = auth.currentUser
                                                if (user != null && !user.isEmailVerified) {
                                                    auth.signOut(); errorMessage = null; successMessage = strNotVerified; user.sendEmailVerification()
                                                } else {
                                                    prefs.edit().putBoolean("has_auth_history", true).apply()
                                                    user?.let { locationViewModel.onUserLogin(it); characterViewModel.onUserLogin(it); dreamViewModel.onUserLogin() }
                                                    onAuthSuccess()
                                                }
                                            } ?: run { isLoading = false; prefs.edit().putBoolean("has_auth_history", true).apply(); onAuthSuccess() }
                                        } else { isLoading = false; errorMessage = strWrongCreds }
                                    }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(10.dp, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Космический фон кнопки
                            Image(
                                painter = painterResource(R.drawable.btn_login_bg),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Полупрозрачный оверлей для яркости
                            Box(
                                Modifier.fillMaxSize().background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF7C4DFF).copy(alpha = 0.3f), Color(0xFFB388FF).copy(alpha = 0.2f))
                                    )
                                )
                            )
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            } else {
                                Text(
                                    stringResource(if (isRegisterMode) R.string.auth_register_btn else R.string.auth_login_btn),
                                    fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Переключение — внутри карточки
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TextButton(onClick = {
                            isRegisterMode = !isRegisterMode; clearErrors(); name = ""; password = ""
                        }) {
                            Text(
                                stringResource(if (isRegisterMode) R.string.auth_has_account else R.string.auth_no_account),
                                color = GoldLight.copy(alpha = 0.7f), fontSize = 14.sp, textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Google — внутри карточки, с иконкой
                    OutlinedButton(
                        onClick = {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(context.getString(R.string.default_web_client_id))
                                .requestEmail().build()
                            googleLauncher.launch(GoogleSignIn.getClient(context, gso).signInIntent)
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.92f),
                            contentColor = Color.Black
                        ),
                        border = androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_google),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            stringResource(R.string.auth_google_btn),
                            fontSize = 15.sp, fontWeight = FontWeight.Medium,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // "или"
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.12f))
                Text("  ${stringResource(R.string.auth_or)}  ", color = Color.White.copy(alpha = 0.35f), fontSize = 13.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.12f))
            }

            // Гость
            TextButton(onClick = onSkipAuth, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.auth_guest_btn), color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(24.dp))
        }

        // Оверлей загрузки
        if (isLoading) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1040).copy(alpha = 0.9f))
                ) {
                    Column(Modifier.padding(36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AccentPurple)
                        Spacer(Modifier.height(14.dp))
                        Text(stringResource(R.string.auth_loading), color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ═══ Тёмное полупрозрачное текстовое поле ═══
@Composable
private fun AuthTextField(
    value: String, onValueChange: (String) -> Unit,
    label: String, icon: ImageVector,
    isError: Boolean = false, errorText: String? = null,
    isPassword: Boolean = false, passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.5f)) },
        leadingIcon = { Icon(icon, null, tint = if (isError) Color(0xFFFF6B6B) else AccentPurple) },
        trailingIcon = if (isPassword) {
            { IconButton(onClick = { onTogglePassword?.invoke() }) {
                Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = Color.White.copy(alpha = 0.4f))
            } }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        isError = isError,
        supportingText = errorText?.let { { Text(it, color = Color(0xFFFF6B6B), fontSize = 12.sp) } },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentPurple,
            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
            errorBorderColor = Color(0xFFFF6B6B),
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = AccentPurple,
            focusedLabelColor = AccentPurple,
        )
    )
}
