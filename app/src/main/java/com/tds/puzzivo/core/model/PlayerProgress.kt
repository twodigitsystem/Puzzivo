package com.tds.puzzivo.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_progress")
data class PlayerProgress(
    @PrimaryKey val id: Int = 1, // Single row
    val level: Int = 1,
    val currentXP: Int = 0,
    val coins: Int = 0,
    val gems: Int = 0,
    val totalXPEarned: Long = 0,
    val totalCoinsEarned: Long = 0,
    val selectedThemeId: String = "navy_dark",
    val unlockedThemes: Set<String> = setOf("navy_dark"),
    val premiumUnlocked: Boolean = false
) {
    fun xpToNextLevel(): Int = calculateXPForLevel(level + 1)
    
    fun progressPercent(): Float = currentXP.toFloat() / xpToNextLevel()
    
    companion object {
        fun calculateXPForLevel(level: Int): Int {
            // XP required increases with level
            return 100 + ((level - 1) * 50)
        }
    }
}

@Entity(tableName = "shop_items")
data class ShopItem(
    @PrimaryKey val id: String,
    val type: ShopItemType,
    val nameRes: Int,
    val descriptionRes: Int,
    val priceCoins: Int,
    val priceGems: Int,
    val iconRes: Int,
    val quantity: Int = 1,
    val isConsumable: Boolean,
    val isPermanent: Boolean
)

enum class ShopItemType {
    HINT_PACK,
    UNDO_PACK,
    TIME_BOOST,
    THEME,
    ICON_PACK,
    DOUBLE_COINS,
    AD_REMOVAL,
    PREMIUM_PASS
}

@Entity(tableName = "player_inventory")
data class PlayerInventoryItem(
    @PrimaryKey val itemId: String,
    val quantity: Int = 0
)
