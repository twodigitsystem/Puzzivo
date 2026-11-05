package com.tds.puzzivo.feature.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.core.database.ShopDao
import com.tds.puzzivo.core.manager.ProgressionManager
import com.tds.puzzivo.core.model.PlayerProgress
import com.tds.puzzivo.core.model.ShopItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShopUiState(
    val playerProgress: PlayerProgress = PlayerProgress(),
    val shopItems: List<ShopItem> = emptyList()
)

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopDao: ShopDao,
    private val progressionManager: ProgressionManager
) : ViewModel() {

    val uiState: StateFlow<ShopUiState> = combine(
        progressionManager.playerProgress,
        shopDao.getAllShopItems()
    ) { progress, items ->
        ShopUiState(
            playerProgress = progress,
            shopItems = items
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ShopUiState()
    )

    fun buyItem(item: ShopItem, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val progress = progressionManager.playerProgress.first()
            if (item.priceCoins > 0 && progress.coins < item.priceCoins) {
                onResult(false, "Not enough coins!")
                return@launch
            }
            if (item.priceGems > 0 && progress.gems < item.priceGems) {
                onResult(false, "Not enough gems!")
                return@launch
            }

            val coinsDeducted = if (item.priceCoins > 0) progressionManager.spendCoins(item.priceCoins) else true
            val gemsDeducted = if (item.priceGems > 0) progressionManager.spendGems(item.priceGems) else true

            if (!coinsDeducted || !gemsDeducted) {
                onResult(false, "Transaction failed — insufficient balance.")
                return@launch
            }

            if (item.isConsumable || !item.isPermanent) {
                progressionManager.addInventoryItem(item.id, item.quantity)
            } else if (item.type == com.tds.puzzivo.core.model.ShopItemType.THEME) {
                progressionManager.unlockTheme(item.id)
            }

            onResult(true, "Successfully purchased ${item.id}!")
        }
    }
}
