package com.tds.puzzivo.feature.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tds.puzzivo.core.database.ShopDao
import com.tds.puzzivo.core.manager.DefaultShopItems
import com.tds.puzzivo.core.manager.ProgressionManager
import com.tds.puzzivo.core.model.PlayerProgress
import com.tds.puzzivo.core.model.ShopItem
import com.tds.puzzivo.core.model.ShopItemType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ShopCategory {
    ALL,
    THEMES,
    BOOSTERS
}

data class ShopUiState(
    val playerProgress: PlayerProgress = PlayerProgress(),
    val selectedCategory: ShopCategory = ShopCategory.ALL,
    val shopItems: List<ShopItem> = emptyList()
)

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopDao: ShopDao,
    private val progressionManager: ProgressionManager
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(ShopCategory.ALL)

    init {
        viewModelScope.launch {
            val existing = shopDao.getAllShopItems().first()
            if (existing.isEmpty()) {
                shopDao.insertAll(DefaultShopItems.getAll())
            }
        }
    }

    val uiState: StateFlow<ShopUiState> = combine(
        progressionManager.playerProgress,
        shopDao.getAllShopItems(),
        _selectedCategory
    ) { progress, items, category ->
        val filtered = when (category) {
            ShopCategory.ALL -> items
            ShopCategory.THEMES -> items.filter { it.type == ShopItemType.THEME }
            ShopCategory.BOOSTERS -> items.filter { it.type != ShopItemType.THEME }
        }
        ShopUiState(
            playerProgress = progress,
            selectedCategory = category,
            shopItems = filtered
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ShopUiState()
    )

    fun onCategorySelected(category: ShopCategory) {
        _selectedCategory.value = category
    }

    fun buyItem(item: ShopItem, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val progress = progressionManager.playerProgress.first()

            if (item.type == ShopItemType.THEME && progress.unlockedThemes.contains(item.id)) {
                onResult(false, "You already own this theme!")
                return@launch
            }

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
                if (coinsDeducted && item.priceCoins > 0) progressionManager.addCoins(item.priceCoins)
                if (gemsDeducted && item.priceGems > 0) progressionManager.addGems(item.priceGems)
                onResult(false, "Transaction failed — insufficient balance.")
                return@launch
            }

            if (item.isConsumable || !item.isPermanent) {
                progressionManager.addInventoryItem(item.id, item.quantity)
            } else if (item.type == ShopItemType.THEME) {
                progressionManager.unlockTheme(item.id)
            }

            onResult(true, "Successfully purchased!")
        }
    }
}
