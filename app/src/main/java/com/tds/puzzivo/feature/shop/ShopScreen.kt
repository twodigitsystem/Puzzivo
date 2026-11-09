package com.tds.puzzivo.feature.shop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tds.puzzivo.core.design.AccentCyan
import com.tds.puzzivo.core.design.AccentGreen
import com.tds.puzzivo.core.design.AccentYellow
import com.tds.puzzivo.core.design.CardBorderColor
import com.tds.puzzivo.core.design.NavyBackground
import com.tds.puzzivo.core.design.NavySurface
import com.tds.puzzivo.core.design.NavySurfaceVariant
import com.tds.puzzivo.core.design.PuzzivoCard
import com.tds.puzzivo.core.design.PuzzivoTopBar
import com.tds.puzzivo.core.design.TextPrimary
import com.tds.puzzivo.core.design.TextSecondary
import com.tds.puzzivo.core.model.ShopItem
import com.tds.puzzivo.core.model.ShopItemType

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

            // Currency Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShopCategory.entries.forEach { category ->
                    FilterChip(
                        selected = uiState.selectedCategory == category,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(category.name, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentYellow,
                            selectedLabelColor = NavyBackground,
                            containerColor = NavySurface,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            snackbarMessage?.let { msg ->
                Surface(
                    color = NavySurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AccentGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = msg,
                        color = AccentGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
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
                                Text("No Items Found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                    }
                } else {
                    items(uiState.shopItems, key = { it.id }) { item ->
                        val isOwned = item.type == ShopItemType.THEME && uiState.playerProgress.unlockedThemes.contains(item.id)
                        ShopItemCard(
                            item = item,
                            isOwned = isOwned,
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
    isOwned: Boolean,
    onBuy: () -> Unit
) {
    val itemIcon = when (item.type) {
        ShopItemType.THEME -> "🎨"
        ShopItemType.HINT_PACK -> "💡"
        ShopItemType.UNDO_PACK -> "↩️"
        ShopItemType.TIME_BOOST -> "⏳"
        ShopItemType.DOUBLE_COINS -> "⚡"
        else -> "🎁"
    }

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
                    Text(itemIcon, fontSize = 26.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(item.nameRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(item.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Button(
                onClick = onBuy,
                enabled = !isOwned,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOwned) NavySurfaceVariant else if (item.priceGems > 0) AccentCyan else AccentYellow,
                    contentColor = if (isOwned) TextSecondary else NavyBackground
                )
            ) {
                Text(
                    text = when {
                        isOwned -> "OWNED"
                        item.priceGems > 0 -> "💎 ${item.priceGems}"
                        else -> "🪙 ${item.priceCoins}"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
