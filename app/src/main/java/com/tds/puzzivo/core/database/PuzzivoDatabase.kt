package com.tds.puzzivo.core.database

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PuzzivoDatabase : RoomDatabase() {
    abstract fun achievementDao(): AchievementDao
    abstract fun statisticsDao(): StatisticsDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun progressDao(): ProgressDao
    abstract fun shopDao(): ShopDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE game_sessions ADD COLUMN completionId TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL(
                    "UPDATE game_sessions SET completionId = 'legacy_' || id WHERE completionId = ''"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_game_sessions_completionId " +
                            "ON game_sessions(completionId)"
                )
            }
        }
    }
}
