package com.dreamjournal.journalofdream.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dreamjournal.journalofdream.R

@Composable
fun ChooseActionDialog(
    onDismiss: () -> Unit,
    onDreamSelected: () -> Unit,
    onLocationSelected: () -> Unit,
    onCharacterSelected: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .shadow(18.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF2E2060).copy(alpha = 0.96f),
                                Color(0xFF1C1045).copy(alpha = 0.98f)
                            )
                        ),
                        RoundedCornerShape(28.dp)
                    )
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.22f),
                                Color.White.copy(alpha = 0.06f),
                                Color(0xFFF0D68C).copy(alpha = 0.15f)
                            )
                        ),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 22.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.fab_choose_action),
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 18.dp)
                    )

                    DialogActionButton(
                        text = stringResource(R.string.fab_record_dream),
                        iconRes = R.drawable.dream_journal_icon,
                        backgroundRes = R.drawable.dreamy_purple_blue,
                        onClick = onDreamSelected
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DialogActionButton(
                        text = stringResource(R.string.fab_add_location),
                        iconRes = R.drawable.location_icon,
                        backgroundRes = R.drawable.mystical_pink,
                        onClick = onLocationSelected
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    DialogActionButton(
                        text = stringResource(R.string.fab_add_character),
                        iconRes = R.drawable.dreamers_icon,
                        backgroundRes = R.drawable.soft_lavender,
                        onClick = onCharacterSelected
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = stringResource(R.string.btn_cancel),
                                color = Color(0xFFC8CCFF),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogActionButton(
    text: String,
    iconRes: Int,
    backgroundRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .shadow(8.dp, RoundedCornerShape(26.dp)),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(backgroundRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(26.dp)),
                contentScale = ContentScale.Crop,
                alpha = 0.78f
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.14f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.14f)
                            )
                        ),
                        RoundedCornerShape(26.dp)
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.08f),
                                Color.White.copy(alpha = 0.20f)
                            )
                        ),
                        RoundedCornerShape(26.dp)
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
