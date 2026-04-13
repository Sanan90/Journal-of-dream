package com.dreamjournal.journalofdream.ui.profile

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.R
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

private val GoldLight      = Color(0xFFF0D68C)
private val GoldDark       = Color(0xFFD4A76A)
private val PlayfairFamily = FontFamily(Font(R.font.playfair_display_bold, FontWeight.Bold))
private val CardGradient   = Brush.verticalGradient(
    listOf(Color(0xFF3B1A58).copy(alpha = 0.80f), Color(0xFF1A0C30).copy(alpha = 0.90f))
)

// ─── Аватары — реальные PNG из drawable/ava1.png ... ava63.png ───────────────
data class AvatarItem(val id: Int, val resId: Int)

val dreamAvatars: List<AvatarItem> = listOf(
    AvatarItem(1,  R.drawable.ava1),  AvatarItem(2,  R.drawable.ava2),
    AvatarItem(3,  R.drawable.ava3),  AvatarItem(4,  R.drawable.ava4),
    AvatarItem(5,  R.drawable.ava5),  AvatarItem(6,  R.drawable.ava6),
    AvatarItem(7,  R.drawable.ava7),  AvatarItem(8,  R.drawable.ava8),
    AvatarItem(9,  R.drawable.ava9),  AvatarItem(10, R.drawable.ava10),
    AvatarItem(11, R.drawable.ava11), AvatarItem(12, R.drawable.ava12),
    AvatarItem(13, R.drawable.ava13), AvatarItem(14, R.drawable.ava14),
    AvatarItem(15, R.drawable.ava15), AvatarItem(16, R.drawable.ava16),
    AvatarItem(17, R.drawable.ava17), AvatarItem(18, R.drawable.ava18),
    AvatarItem(19, R.drawable.ava19), AvatarItem(20, R.drawable.ava20),
    AvatarItem(21, R.drawable.ava21), AvatarItem(22, R.drawable.ava22),
    AvatarItem(23, R.drawable.ava23), AvatarItem(24, R.drawable.ava24),
    AvatarItem(25, R.drawable.ava25), AvatarItem(26, R.drawable.ava26),
    AvatarItem(27, R.drawable.ava27), AvatarItem(28, R.drawable.ava28),
    AvatarItem(29, R.drawable.ava29), AvatarItem(30, R.drawable.ava30),
    AvatarItem(31, R.drawable.ava31), AvatarItem(32, R.drawable.ava32),
    AvatarItem(33, R.drawable.ava33), AvatarItem(34, R.drawable.ava34),
    AvatarItem(35, R.drawable.ava35), AvatarItem(36, R.drawable.ava36),
    AvatarItem(37, R.drawable.ava37), AvatarItem(38, R.drawable.ava38),
    AvatarItem(39, R.drawable.ava39), AvatarItem(40, R.drawable.ava40),
    AvatarItem(41, R.drawable.ava41), AvatarItem(42, R.drawable.ava42),
    AvatarItem(43, R.drawable.ava43), AvatarItem(44, R.drawable.ava44),
    AvatarItem(45, R.drawable.ava45), AvatarItem(46, R.drawable.ava46),
    AvatarItem(47, R.drawable.ava47), AvatarItem(48, R.drawable.ava48),
    AvatarItem(49, R.drawable.ava49), AvatarItem(50, R.drawable.ava50),
    AvatarItem(51, R.drawable.ava51), AvatarItem(52, R.drawable.ava52),
    AvatarItem(53, R.drawable.ava53), AvatarItem(54, R.drawable.ava54),
    AvatarItem(55, R.drawable.ava55), AvatarItem(56, R.drawable.ava56),
    AvatarItem(57, R.drawable.ava57), AvatarItem(58, R.drawable.ava58),
    AvatarItem(59, R.drawable.ava59), AvatarItem(60, R.drawable.ava60),
    AvatarItem(61, R.drawable.ava61), AvatarItem(62, R.drawable.ava62),
    AvatarItem(63, R.drawable.ava63),
    AvatarItem(64, R.drawable.ava64), AvatarItem(65, R.drawable.ava65),
    AvatarItem(66, R.drawable.ava66), AvatarItem(67, R.drawable.ava67),
    AvatarItem(68, R.drawable.ava68), AvatarItem(69, R.drawable.ava69),
    AvatarItem(70, R.drawable.ava70), AvatarItem(71, R.drawable.ava71),
    AvatarItem(72, R.drawable.ava72), AvatarItem(73, R.drawable.ava73),
    AvatarItem(74, R.drawable.ava74), AvatarItem(75, R.drawable.ava75),
    AvatarItem(76, R.drawable.ava76), AvatarItem(77, R.drawable.ava77),
    AvatarItem(78, R.drawable.ava78), AvatarItem(79, R.drawable.ava79),
    AvatarItem(80, R.drawable.ava80), AvatarItem(81, R.drawable.ava81),
    AvatarItem(82, R.drawable.ava82), AvatarItem(83, R.drawable.ava83),
    AvatarItem(84, R.drawable.ava84)
)

// Ключ аватара привязан к UID — каждый аккаунт хранит свой выбор
private fun avatarKey(uid: String?) = "avatar_id_${uid ?: "guest"}"

// ─── Экран профиля ────────────────────────────────────────────────────────────
@Composable
fun ProfileScreen(
    navController: NavHostController,
    currentUser: FirebaseUser?,
    onNameUpdated: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val prefs   = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }

    var displayName by remember {
        mutableStateOf(
            currentUser?.displayName?.takeIf { it.isNotBlank() }
                ?: currentUser?.email?.substringBefore("@") ?: ""
        )
    }
    var editingName      by remember { mutableStateOf(false) }
    var nameInput        by remember { mutableStateOf(displayName) }
    var isSaving         by remember { mutableStateOf(false) }
    var saveError        by remember { mutableStateOf<String?>(null) }
    var selectedAvatarId by remember { mutableStateOf(prefs.getInt(avatarKey(currentUser?.uid), 1)) }
    var showAvatarPicker by remember { mutableStateOf(false) }

    val currentAvatar = dreamAvatars.find { it.id == selectedAvatarId } ?: dreamAvatars.first()

    // Загружаем профиль из Firestore при открытии — для синхронизации между устройствами
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .get().await()
                if (doc.exists()) {
                    // Синхронизируем аватар
                    doc.getLong("avatarId")?.toInt()?.let { remoteAvatarId ->
                        if (remoteAvatarId != selectedAvatarId) {
                            selectedAvatarId = remoteAvatarId
                            prefs.edit().putInt(avatarKey(uid), remoteAvatarId).apply()
                        }
                    }
                    // Синхронизируем имя (приоритет Firestore над Firebase Auth)
                    doc.getString("displayName")?.takeIf { it.isNotBlank() }?.let { remoteName ->
                        if (remoteName != displayName) {
                            displayName = remoteName
                            onNameUpdated(remoteName)
                        }
                    }
                }
            } catch (e: Exception) {
                // Нет сети — используем локальные данные, не критично
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.new_fon), null,
            Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {

            // ── Верхняя панель ──
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, stringResource(R.string.btn_back),
                        tint = GoldLight, modifier = Modifier.size(28.dp))
                }
                Text(stringResource(R.string.profile_title), color = GoldLight,
                    fontFamily = PlayfairFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                // ── Аватар с пульсацией ──
                Box(contentAlignment = Alignment.Center) {
                    val pulse = rememberInfiniteTransition(label = "pulse")
                    val glowAlpha by pulse.animateFloat(0.2f, 0.5f,
                        infiniteRepeatable(tween(1400), RepeatMode.Reverse), "glow")
                    Box(Modifier.size(110.dp).alpha(glowAlpha)
                        .background(Brush.radialGradient(
                            listOf(Color(0xFF9C27B0).copy(0.5f), Color.Transparent)), CircleShape))

                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .shadow(16.dp, CircleShape)
                            .clip(CircleShape)
                            .border(2.dp, Brush.linearGradient(listOf(GoldLight, GoldDark)), CircleShape)
                            .clickable { showAvatarPicker = true }
                    ) {
                        Image(painterResource(currentAvatar.resId), null,
                            Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }

                    // Кнопка редактирования
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp)
                            .size(28.dp).clip(CircleShape).background(Color(0xFF9C27B0))
                            .clickable { showAvatarPicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Имя ──
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(CardGradient)
                        .border(1.dp, GoldLight.copy(0.15f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(stringResource(R.string.profile_name_label), color = GoldLight,
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                        Spacer(Modifier.height(8.dp))

                        if (editingName) {
                            OutlinedTextField(
                                value = nameInput, onValueChange = { nameInput = it },
                                modifier = Modifier.fillMaxWidth(), singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                    focusedBorderColor = GoldLight, unfocusedBorderColor = GoldLight.copy(0.3f),
                                    cursorColor = GoldLight
                                ),
                                placeholder = { Text(stringResource(R.string.profile_name_placeholder), color = Color.White.copy(0.4f)) }
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { editingName = false; nameInput = displayName; saveError = null }) {
                                    Text(stringResource(R.string.btn_cancel), color = Color.White.copy(0.6f))
                                }
                                Button(
                                    onClick = {
                                        if (nameInput.isBlank()) return@Button
                                        isSaving = true; saveError = null
                                        val request = UserProfileChangeRequest.Builder()
                                            .setDisplayName(nameInput.trim()).build()
                                        currentUser?.updateProfile(request)
                                            ?.addOnSuccessListener {
                                                val savedName = nameInput.trim()
                                                displayName = savedName
                                                editingName = false; isSaving = false
                                                onNameUpdated(savedName)
                                                // Синхронизируем имя в Firestore для других устройств
                                                currentUser.uid.let { uid ->
                                                    FirebaseFirestore.getInstance()
                                                        .collection("users").document(uid)
                                                        .set(mapOf("displayName" to savedName), SetOptions.merge())
                                                }
                                            }
                                            ?.addOnFailureListener { e -> saveError = e.message; isSaving = false }
                                    },
                                    enabled = !isSaving && nameInput.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B2FBE))
                                ) {
                                    if (isSaving) CircularProgressIndicator(Modifier.size(16.dp), Color.White, 2.dp)
                                    else {
                                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(stringResource(R.string.profile_save))
                                    }
                                }
                            }
                            saveError?.let { Text(it, color = Color(0xFFEF5350), fontSize = 12.sp) }
                        } else {
                            Row(Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    displayName.ifBlank { stringResource(R.string.profile_name_empty) },
                                    color = if (displayName.isBlank()) Color.White.copy(0.4f) else Color.White,
                                    fontSize = 17.sp, fontWeight = FontWeight.Medium
                                )
                                IconButton(onClick = { nameInput = displayName; editingName = true }) {
                                    Icon(Icons.Default.Edit, null, tint = GoldLight, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Email ──
                if (currentUser?.email != null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(CardGradient)
                            .border(1.dp, GoldLight.copy(0.15f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("Email", color = GoldLight, fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(currentUser.email ?: "", color = Color.White.copy(0.75f), fontSize = 15.sp)
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }

    if (showAvatarPicker) {
        AvatarPickerSheet(
            currentId = selectedAvatarId,
            onSelected = { id ->
                selectedAvatarId = id
                prefs.edit().putInt(avatarKey(currentUser?.uid), id).apply()
                // Синхронизируем аватар в Firestore для других устройств
                currentUser?.uid?.let { uid ->
                    FirebaseFirestore.getInstance()
                        .collection("users").document(uid)
                        .set(mapOf("avatarId" to id), SetOptions.merge())
                }
                showAvatarPicker = false
            },
            onDismiss = { showAvatarPicker = false }
        )
    }
}

// ─── Bottom Sheet выбора аватара ──────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvatarPickerSheet(
    currentId: Int,
    onSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A0C30),
        dragHandle = {
            Box(Modifier.padding(top = 12.dp, bottom = 8.dp)
                .size(width = 40.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(GoldLight.copy(0.3f)))
        }
    ) {
        Text("Выберите аватар", color = GoldLight, fontFamily = PlayfairFamily,
            fontWeight = FontWeight.Bold, fontSize = 20.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(dreamAvatars) { avatar ->
                val isSelected = avatar.id == currentId
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .shadow(if (isSelected) 8.dp else 2.dp, CircleShape)
                        .clip(CircleShape)
                        .then(if (isSelected) Modifier.border(2.dp, GoldLight, CircleShape) else Modifier)
                        .clickable { onSelected(avatar.id) }
                ) {
                    Image(painterResource(avatar.resId), null,
                        Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }
        }
    }
}
