package com.example.journalofdream.ui.theme

import androidx.compose.ui.platform.LocalContext
import com.example.journalofdream.util.LocaleHelper
import androidx.compose.ui.res.stringResource
import com.example.journalofdream.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.example.journalofdream.model.Technique
import com.example.journalofdream.model.TechniqueComment
import com.example.journalofdream.sync.CommentRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TechniqueDetailSheet(
    technique: Technique,
    userVote: String?,
    isAdminMode: Boolean = false,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lang = LocaleHelper.getSavedLanguage(context).let { if (it == "system") context.resources.configuration.locales[0].language else it }
    val repository = remember { CommentRepository() }
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    var comments by remember { mutableStateOf<List<TechniqueComment>>(emptyList()) }
    var likedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var commentText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    DisposableEffect(technique.id) {
        repository.startListening(technique.id) { comments = it }
        scope.launch { likedIds = repository.getLikedCommentIds(technique.id) }
        onDispose { repository.stopListening() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Шапка с кнопкой назад
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.btn_back),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = stringResource(R.string.tech_label),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Название
                item {
                    Text(
                        text = technique.localizedName(lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 30.sp
                    )
                }

                // Источник
                if (technique.source.isNotBlank()) {
                    item {
                        Text(
                            text = "📚 ${technique.source}",
                            fontSize = 13.sp,
                            color = Color(0xFF7E57C2),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Полное описание
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = technique.localizedDescription(lang),
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Лайки/дизлайки
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VoteButton("👍", technique.likes, userVote == "like", Color(0xFF4CAF50), onLike)
                        VoteButton("👎", technique.dislikes, userVote == "dislike", Color(0xFFEF5350), onDislike)
                        val rating = technique.likes - technique.dislikes
                        Text(
                            text = if (rating > 0) "+$rating" else "$rating",
                            color = when {
                                rating > 0 -> Color(0xFF4CAF50)
                                rating < 0 -> Color(0xFFEF5350)
                                else -> Color.Gray
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                }

                // Заголовок комментариев
                item {
                    Text(
                        text = "💬 " + stringResource(R.string.tech_comments, comments.size),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Поле ввода
                if (currentUid != null) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                placeholder = { Text(stringResource(R.string.tech_comment_hint)) },
                                modifier = Modifier.weight(1f),
                                singleLine = false,
                                maxLines = 3,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Send
                                ),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (commentText.isNotBlank() && !isSending) {
                                        val text = commentText
                                        commentText = ""
                                        isSending = true
                                        scope.launch {
                                            repository.addComment(technique.id, text)
                                            isSending = false
                                            keyboard?.hide()
                                        }
                                    }
                                })
                            )
                            IconButton(
                                onClick = {
                                    if (commentText.isNotBlank() && !isSending) {
                                        val text = commentText
                                        commentText = ""
                                        isSending = true
                                        scope.launch {
                                            repository.addComment(technique.id, text)
                                            isSending = false
                                            keyboard?.hide()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF7E57C2), CircleShape)
                            ) {
                                if (isSending) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Send, stringResource(R.string.tech_send), tint = Color.White)
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            stringResource(R.string.tech_login_to_comment),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontSize = 13.sp
                        )
                    }
                }

                // Комментарии
                if (comments.isEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.tech_no_comments),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(comments, key = { it.id }) { comment ->
                        val isLiked = comment.id in likedIds
                        CommentCard(
                            comment = comment,
                            isLiked = isLiked,
                            isOwn = comment.authorUid == currentUid,
                            isAdmin = isAdminMode,
                            onLike = {
                                val wasLiked = isLiked
                                likedIds = if (wasLiked) likedIds - comment.id else likedIds + comment.id
                                scope.launch {
                                    repository.likeComment(technique.id, comment.id, wasLiked)
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    repository.deleteComment(technique.id, comment.id)
                                }
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun CommentCard(
    comment: TechniqueComment,
    isLiked: Boolean,
    isOwn: Boolean,
    isAdmin: Boolean = false,
    onLike: () -> Unit,
    onDelete: () -> Unit
) {
    val timeStr = remember(comment.createdAt) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date(comment.createdAt))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOwn)
                Color(0xFF7E57C2).copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isOwn) stringResource(R.string.tech_you) else comment.authorName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = if (isOwn) Color(0xFF7E57C2) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = timeStr,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLike, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(R.string.tech_like),
                        tint = if (isLiked) Color(0xFFEF5350) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "${comment.likes}",
                    fontSize = 12.sp,
                    color = if (isLiked) Color(0xFFEF5350) else Color.Gray
                )
                if (isOwn || isAdmin) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = onDelete,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            if (isAdmin && !isOwn) "🗑 " + stringResource(R.string.btn_delete) else stringResource(R.string.btn_delete),
                            color = Color.Red.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
