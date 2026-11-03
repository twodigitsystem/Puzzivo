package com.tds.puzzivo.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tds.puzzivo.core.model.Achievement
import com.tds.puzzivo.core.model.DailyChallenge
import com.tds.puzzivo.core.model.GameSession
import com.tds.puzzivo.core.model.GameStatistics
import com.tds.puzzivo.core.model.PlayerInventoryItem
import com.tds.puzzivo.core.model.PlayerProgress
import com.tds.puzzivo.core.model.ShopItem

@Database(
    entities = [
        Achievement::class,
        GameStatistics::class,
        GameSession::class,
        DailyChallenge::class,
        PlayerProgress::class,
        ShopItem::class,
        PlayerInventoryItem::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PuzzivoDatabase : RoomDatabase() {
    abstract fun achievementDao(): AchievementDao
    abstract fun statisticsDao(): StatisticsDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun progressDao(): ProgressDao
    abstract fun shopDao(): ShopDao
}
