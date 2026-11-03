package com.tds.puzzivo.feature.shop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tds.puzzivo.core.design.*
import com.tds.puzzivo.core.model.ShopItem

@Composable
fun ShopScreen(
    onNavigateBack: () -> Unit,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        color = NavyBackground,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PuzzivoTopBar(
                title = "PUZZIVO SHOP",
                onBackClick = onNavigateBack
            )

            // Currency status bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = NavySurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorderColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🪙", fontSize = 20.sp)
                        Column {
                            Text("Coins", fontSize = 11.sp, color = TextSecondary)
                            Text("${uiState.playerProgress.coins}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AccentYellow)
                        }
                    }
                }

                Surface(
                    color = NavySurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorderColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💎", fontSize = 20.sp)
                        Column {
                            Text("Gems", fontSize = 11.sp, color = TextSecondary)
                            Text("${uiState.playerProgress.gems}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                        }
                    }
                }
            }

            snackbarMessage?.let { msg ->
                Surface(
                    color = NavySurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AccentGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = msg,
                        color = AccentGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.shopItems.isEmpty()) {
                    item {
                        PuzzivoCard(
                            backgroundColor = NavySurface,
                            borderColor = CardBorderColor,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🛍️", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Shop Items Coming Soon", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Check back later for booster packs and exclusive themes!", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                } else {
                    items(uiState.shopItems, key = { it.id }) { item ->
                        ShopItemCard(
                            item = item,
                            onBuy = {
                                viewModel.buyItem(item) { _, msg ->
                                    snackbarMessage = msg
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopItemCard(
    item: ShopItem,
    onBuy: () -> Unit
) {
    PuzzivoCard(
        backgroundColor = NavySurface,
        borderColor = CardBorderColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = NavySurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("🎁", fontSize = 26.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.id.replace("_", " ").uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Quantity: ${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Button(
                onClick = onBuy,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (item.priceGems > 0) AccentCyan else AccentYellow,
                    contentColor = NavyBackground
                )
            ) {
                Text(
                    text = if (item.priceGems > 0) "💎 ${item.priceGems}" else "🪙 ${item.priceCoins}",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
