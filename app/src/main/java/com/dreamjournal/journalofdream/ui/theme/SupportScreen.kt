package com.dreamjournal.journalofdream.ui.theme

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dreamjournal.journalofdream.R

private const val SBP_PHONE = "+79687008070"
private const val ADDR_BTC  = "bc1qfvhn8ec7clyp5dgu8vqngtmru0qekx45kzcrr4"
private const val ADDR_ETH  = "0x8BDFAB641e845d59a76f8A5cd8e9Cd5636605E0D"
private const val ADDR_USDT = "TS63ugs4JARhVPzAwudgE77vfnkR6FxSna"
private const val ADDR_XRP  = "rxcm8ekpiepWpAooXpj1w4XUccMDZkgji"
private const val ADDR_DOGE = "DEVxGmChRc4SFDHx37EGR2NwCzghKt3Bmi"
private const val ADDR_SOL  = "FHKpr7GyP1eVktyjpfHMcZzk6rymK1y1LpUeP9cPBtg7"

private val GoldLightS = Color(0xFFF0D68C)
private val GoldDarkS  = Color(0xFFD4A76A)
private val PlayfairFamilyS = FontFamily(Font(R.font.playfair_display_bold, FontWeight.Bold))
private val SupportCardBg = Brush.verticalGradient(
    listOf(Color(0xFF3B1A58).copy(alpha = 0.72f), Color(0xFF1A0C30).copy(alpha = 0.88f))
)

@Composable
fun SupportScreen(navController: NavHostController) {
    val context = LocalContext.current
    val copiedText = stringResource(R.string.support_copied)

    fun copyToClipboard(label: String, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, copiedText, Toast.LENGTH_SHORT).show()
    }

    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.new_fon), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            // Заголовок
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, tint = GoldLightS, modifier = Modifier.size(28.dp))
                }
                Text(stringResource(R.string.support_menu_title), color = GoldLightS,
                    fontFamily = PlayfairFamilyS, fontWeight = FontWeight.Bold,
                    fontSize = 26.sp, modifier = Modifier.weight(1f))
                Image(painterResource(R.drawable.settings_coffee), null,
                    Modifier.size(32.dp).padding(end = 10.dp), contentScale = ContentScale.Fit)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                Text(stringResource(R.string.support_subtitle), fontSize = 15.sp,
                    color = Color.White.copy(0.82f), textAlign = TextAlign.Center,
                    lineHeight = 22.sp)

                // ── СБП ──────────────────────────────────────────────────────
                SupportSectionHeader(stringResource(R.string.support_sbp_title))

                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                        .background(SupportCardBg)
                        .border(1.dp, GoldLightS.copy(0.22f), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(R.drawable.sbp_qr),
                            contentDescription = "QR СБП",
                            modifier = Modifier.size(200.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.support_sbp_hint), fontSize = 13.sp,
                            color = Color.White.copy(0.60f), textAlign = TextAlign.Center)
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFF7B3FA0), Color(0xFF4A2870))))
                                .border(1.dp, GoldLightS.copy(0.40f), RoundedCornerShape(12.dp))
                                .clickable { copyToClipboard("СБП", SBP_PHONE) }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text(stringResource(R.string.support_sbp_copy), color = GoldLightS,
                                fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }

                // ── КРИПТА ───────────────────────────────────────────────────
                SupportSectionHeader(stringResource(R.string.support_crypto_title))

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
                    SupportCryptoCard(name, network, address, copyBtn) { copyToClipboard(name, address) }
                }

                Text(stringResource(R.string.support_thanks), fontSize = 13.sp,
                    color = Color.White.copy(0.50f), textAlign = TextAlign.Center,
                    lineHeight = 20.sp)

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SupportSectionHeader(title: String) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(3.dp).height(20.dp)
            .background(Brush.verticalGradient(listOf(GoldLightS, GoldDarkS)), RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(8.dp))
        Text(title, color = GoldLightS, fontFamily = PlayfairFamilyS,
            fontWeight = FontWeight.Bold, fontSize = 17.sp)
    }
}

@Composable
private fun SupportCryptoCard(name: String, network: String, address: String, copyBtnText: String, onCopy: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(SupportCardBg)
            .border(1.dp, GoldLightS.copy(0.18f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(name, color = GoldLightS, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(network, color = Color.White.copy(0.50f), fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF7B3FA0).copy(0.35f))
                        .border(1.dp, GoldLightS.copy(0.35f), RoundedCornerShape(10.dp))
                        .clickable { onCopy() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(copyBtnText, color = GoldLightS, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (address.length > 20) "${address.take(10)}...${address.takeLast(10)}" else address,
                color = Color.White.copy(0.42f), fontSize = 12.sp
            )
        }
    }
}
