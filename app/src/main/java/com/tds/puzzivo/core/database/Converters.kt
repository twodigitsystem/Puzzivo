package com.tds.puzzivo.core.database

import androidx.room.TypeConverter
import com.tds.puzzivo.core.model.AchievementRarity
import com.tds.puzzivo.core.model.DifficultyLevel
import com.tds.puzzivo.core.model.GameType
import com.tds.puzzivo.core.model.ShopItemType
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromGameType(value: GameType): String = value.name

    @TypeConverter
    fun toGameType(value: String): GameType = GameType.valueOf(value)

    @TypeConverter
    fun fromAchievementRarity(value: AchievementRarity): String = value.name

    @TypeConverter
    fun toAchievementRarity(value: String): AchievementRarity = 
        AchievementRarity.valueOf(value)

    @TypeConverter
    fun fromDifficultyLevel(value: DifficultyLevel): String = value.name

    @TypeConverter
    fun toDifficultyLevel(value: String): DifficultyLevel = 
        DifficultyLevel.valueOf(value)

    @TypeConverter
    fun fromShopItemType(value: ShopItemType): String = value.name

    @TypeConverter
    fun toShopItemType(value: String): ShopItemType = 
        ShopItemType.valueOf(value)

    @TypeConverter
    fun fromStringSet(value: Set<String>): String = Json.encodeToString(value.toList())

    @TypeConverter
    fun toStringSet(value: String): Set<String> = 
        Json.decodeFromString<List<String>>(value).toSet()
}
