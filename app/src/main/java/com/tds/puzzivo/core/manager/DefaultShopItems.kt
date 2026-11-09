package com.tds.puzzivo.core.manager

import com.tds.puzzivo.R
import com.tds.puzzivo.core.model.ShopItem
import com.tds.puzzivo.core.model.ShopItemType

object DefaultShopItems {

    fun getAll(): List<ShopItem> = listOf(
        ShopItem(
            id = "hint_pack_5",
            type = ShopItemType.HINT_PACK,
            nameRes = R.string.shop_hints_pack_name,
            descriptionRes = R.string.shop_hints_pack_desc,
            priceCoins = 100,
            priceGems = 0,
            iconRes = R.mipmap.ic_launcher,
            quantity = 5,
            isConsumable = true,
            isPermanent = false
        ),
        ShopItem(
            id = "undo_pack_5",
            type = ShopItemType.UNDO_PACK,
            nameRes = R.string.shop_undo_pack_name,
            descriptionRes = R.string.shop_undo_pack_desc,
            priceCoins = 80,
            priceGems = 0,
            iconRes = R.mipmap.ic_launcher,
            quantity = 5,
            isConsumable = true,
            isPermanent = false
        ),
        ShopItem(
            id = "time_booster_5",
            type = ShopItemType.TIME_BOOST,
            nameRes = R.string.shop_time_boost_name,
            descriptionRes = R.string.shop_time_boost_desc,
            priceCoins = 75,
            priceGems = 0,
            iconRes = R.mipmap.ic_launcher,
            quantity = 5,
            isConsumable = true,
            isPermanent = false
        ),
        ShopItem(
            id = "double_coins_pass",
            type = ShopItemType.DOUBLE_COINS,
            nameRes = R.string.shop_double_coins_name,
            descriptionRes = R.string.shop_double_coins_desc,
            priceCoins = 0,
            priceGems = 50,
            iconRes = R.mipmap.ic_launcher,
            quantity = 1,
            isConsumable = false,
            isPermanent = true
        ),
        ShopItem(
            id = "theme_forest_green",
            type = ShopItemType.THEME,
            nameRes = R.string.shop_theme_forest_name,
            descriptionRes = R.string.shop_theme_forest_desc,
            priceCoins = 250,
            priceGems = 0,
            iconRes = R.mipmap.ic_launcher,
            quantity = 1,
            isConsumable = false,
            isPermanent = true
        ),
        ShopItem(
            id = "theme_ocean_blue",
            type = ShopItemType.THEME,
            nameRes = R.string.shop_theme_ocean_name,
            descriptionRes = R.string.shop_theme_ocean_desc,
            priceCoins = 300,
            priceGems = 0,
            iconRes = R.mipmap.ic_launcher,
            quantity = 1,
            isConsumable = false,
            isPermanent = true
        ),
        ShopItem(
            id = "theme_sunset_orange",
            type = ShopItemType.THEME,
            nameRes = R.string.shop_theme_sunset_name,
            descriptionRes = R.string.shop_theme_sunset_desc,
            priceCoins = 400,
            priceGems = 0,
            iconRes = R.mipmap.ic_launcher,
            quantity = 1,
            isConsumable = false,
            isPermanent = true
        ),
        ShopItem(
            id = "theme_rose_gold",
            type = ShopItemType.THEME,
            nameRes = R.string.shop_theme_rose_gold_name,
            descriptionRes = R.string.shop_theme_rose_gold_desc,
            priceCoins = 0,
            priceGems = 30,
            iconRes = R.mipmap.ic_launcher,
            quantity = 1,
            isConsumable = false,
            isPermanent = true
        )
    )
}
