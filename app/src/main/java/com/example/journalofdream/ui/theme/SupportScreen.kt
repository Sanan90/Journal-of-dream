package com.example.journalofdream.ui.theme

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.journalofdream.R
import com.example.journalofdream.ui.common.BackgroundScreen

// ────────────────────────────────────────────────
//  ВСТАВЬ СВОИ АДРЕСА ЗДЕСЬ
// ────────────────────────────────────────────────
private const val SBP_PHONE = "+79687008070"
private const val ADDR_BTC  = "bc1qfvhn8ec7clyp5dgu8vqngtmru0qekx45kzcrr4"
private const val ADDR_ETH  = "0x8BDFAB641e845d59a76f8A5cd8e9Cd5636605E0D"
private const val ADDR_USDT = "TS63ugs4JARhVPzAwudgE77vfnkR6FxSna"
private const val ADDR_XRP  = "rxcm8ekpiepWpAooXpj1w4XUccMDZkgji"
private const val ADDR_DOGE = "DEVxGmChRc4SFDHx37EGR2NwCzghKt3Bmi"
private const val ADDR_SOL  = "FHKpr7GyP1eVktyjpfHMcZzk6rymK1y1LpUeP9cPBtg7"
// ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(navController: NavHostController) {
    val context = LocalContext.current
    val copiedText = stringResource(R.string.support_copied)

    fun copyToClipboard(label: String, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, copiedText, Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.support_menu_title),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            BackgroundScreen()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.support_subtitle),
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(20.dp))

                // ── СБП ──────────────────────────────────
                SupportSectionTitle(stringResource(R.string.support_sbp_title))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.sbp_qr),
                            contentDescription = "QR СБП",
                            modifier = Modifier
                                .size(200.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.support_sbp_hint),
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { copyToClipboard("СБП", SBP_PHONE) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, Color.White.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(stringResource(R.string.support_sbp_copy))
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── КРИПТА ───────────────────────────────
                SupportSectionTitle(stringResource(R.string.support_crypto_title))

                val copyBtn = stringResource(R.string.support_copy_btn)
                val cryptoList = listOf(
                    Triple("₿  Bitcoin",  "BTC • Bitcoin network",   ADDR_BTC),
                    Triple("Ξ  Ethereum", "ETH • Ethereum network",  ADDR_ETH),
                    Triple("₮  Tether",   "USDT • TRC20 network",    ADDR_USDT),
                    Triple("✕  XRP",      "XRP • XRP Ledger",        ADDR_XRP),
                    Triple("Ð  Dogecoin", "DOGE • Dogecoin network", ADDR_DOGE),
                    Triple("◎  Solana",   "SOL • Solana network",    ADDR_SOL),
                )

                cryptoList.forEach { (name, network, address) ->
                    CryptoCard(
                        name = name,
                        network = network,
                        address = address,
                        copyBtnText = copyBtn,
                        onCopy = { copyToClipboard(name, address) }
                    )
                    Spacer(Modifier.height(10.dp))
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.support_thanks),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SupportSectionTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun CryptoCard(
    name: String,
    network: String,
    address: String,
    copyBtnText: String,
    onCopy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        name,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        network,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
                OutlinedButton(
                    onClick = onCopy,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF7C4DFF)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, Color(0xFF7C4DFF).copy(alpha = 0.5f)
                    )
                ) {
                    Text(copyBtnText, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            // Показываем обрезанный адрес
            Text(
                text = if (address.length > 20)
                    "${address.take(10)}...${address.takeLast(10)}"
                else address,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp
            )
        }
    }
}
