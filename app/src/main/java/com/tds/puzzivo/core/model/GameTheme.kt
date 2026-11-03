package com.tds.puzzivo.core.model

import androidx.compose.ui.graphics.Color

enum class GameTheme(
    val id: String,
    val displayNameRes: Int,
    val backgroundColor: Long,
    val surfaceColor: Long,
    val primaryColor: Long,
    val secondaryColor: Long,
    val accentColor: Long,
    val requirementType: ThemeRequirementType,
    val requirementValue: Int
) {
    NAVY_DARK(
        id = "navy_dark",
        displayNameRes = com.tds.puzzivo.R.string.theme_navy_dark,
        backgroundColor = 0xFF0F172A,
        surfaceColor = 0xFF1E293B,
        primaryColor = 0xFFFACC15,
        secondaryColor = 0xFF06B6D4,
        accentColor = 0xFF22C55E,
        requirementType = ThemeRequirementType.NONE,
        requirementValue = 0
    ),
    FOREST_GREEN(
        id = "forest_green",
        displayNameRes = com.tds.puzzivo.R.string.theme_forest_green,
        backgroundColor = 0xFF064E3B,
        surfaceColor = 0xFF065F46,
        primaryColor = 0xFF10B981,
        secondaryColor = 0xFF34D399,
        accentColor = 0xFFFACC15,
        requirementType = ThemeRequirementType.GAMES_PLAYED,
        requirementValue = 50
    ),
    OCEAN_BLUE(
        id = "ocean_blue",
        displayNameRes = com.tds.puzzivo.R.string.theme_ocean_blue,
        backgroundColor = 0xFF0C4A6E,
        surfaceColor = 0xFF075985,
        primaryColor = 0xFF0EA5E9,
        secondaryColor = 0xFF38BDF8,
        accentColor = 0xFFFACC15,
        requirementType = ThemeRequirementType.LEVEL,
        requirementValue = 10
    ),
    SUNSET_ORANGE(
        id = "sunset_orange",
        displayNameRes = com.tds.puzzivo.R.string.theme_sunset_orange,
        backgroundColor = 0xFF7C2D12,
        surfaceColor = 0xFF9A3412,
        primaryColor = 0xFFF97316,
        secondaryColor = 0xFFFB923C,
        accentColor = 0xFFFACC15,
        requirementType = ThemeRequirementType.STREAK,
        requirementValue = 7
    ),
    MIDNIGHT_PURPLE(
        id = "midnight_purple",
        displayNameRes = com.tds.puzzivo.R.string.theme_midnight_purple,
        backgroundColor = 0xFF4C1D95,
        surfaceColor = 0xFF5B21B6,
        primaryColor = 0xFF8B5CF6,
        secondaryColor = 0xFFA78BFA,
        accentColor = 0xFFFACC15,
        requirementType = ThemeRequirementType.ACHIEVEMENT_COUNT,
        requirementValue = 10
    ),
    ROSE_GOLD(
        id = "rose_gold",
        displayNameRes = com.tds.puzzivo.R.string.theme_rose_gold,
        backgroundColor = 0xFF831843,
        surfaceColor = 0xFF9F1239,
        primaryColor = 0xFFEC4899,
        secondaryColor = 0xFFF472B6,
        accentColor = 0xFFFACC15,
        requirementType = ThemeRequirementType.PREMIUM,
        requirementValue = 0
    );
    
    fun getBackgroundColor() = Color(backgroundColor)
    fun getSurfaceColor() = Color(surfaceColor)
    fun getPrimaryColor() = Color(primaryColor)
    fun getSecondaryColor() = Color(secondaryColor)
    fun getAccentColor() = Color(accentColor)
}

enum class ThemeRequirementType {
    NONE,
    GAMES_PLAYED,
    LEVEL,
    STREAK,
    ACHIEVEMENT_COUNT,
    PREMIUM
}
