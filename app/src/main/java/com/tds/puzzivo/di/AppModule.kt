package com.tds.puzzivo.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.tds.puzzivo.BuildConfig
import com.tds.puzzivo.core.ads.AdManager
import com.tds.puzzivo.core.ads.AdsConsentManager
import com.tds.puzzivo.core.analytics.AnalyticsManager
import com.tds.puzzivo.core.audio.SoundAndHapticsManager
import com.tds.puzzivo.core.auth.AuthManager
import com.tds.puzzivo.core.database.PuzzivoDatabase
import com.tds.puzzivo.core.network.NetworkManager
import com.tds.puzzivo.core.storage.PreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(
        @ApplicationContext context: Context
    ): PreferencesRepository {
        return PreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideAdsConsentManager(
        @ApplicationContext context: Context
    ): AdsConsentManager {
        return AdsConsentManager(context)
    }

    @Provides
    @Singleton
    fun provideAdManager(
        @ApplicationContext context: Context,
        adsConsentManager: AdsConsentManager,
        remoteConfigManager: com.tds.puzzivo.core.config.RemoteConfigManager
    ): AdManager {
        return AdManager(context, remoteConfigManager) { adsConsentManager.canRequestAds() }
    }

    @Provides
    @Singleton
    fun provideSoundAndHapticsManager(
        @ApplicationContext context: Context
    ): SoundAndHapticsManager {
        return SoundAndHapticsManager(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(
        @ApplicationContext context: Context
    ): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideAnalyticsManager(
        firebaseAnalytics: FirebaseAnalytics
    ): AnalyticsManager {
        return AnalyticsManager(firebaseAnalytics)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // 1 hour
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        return remoteConfig
    }

    @Provides
    @Singleton
    fun provideAuthManager(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth
    ): AuthManager {
        return AuthManager(context, firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideNetworkManager(
        @ApplicationContext context: Context
    ): NetworkManager {
        return NetworkManager(context)
    }

    @Provides
    @Singleton
    fun providePuzzivoDatabase(
        @ApplicationContext context: Context
    ): PuzzivoDatabase {
        val builder = Room.databaseBuilder(
            context,
            PuzzivoDatabase::class.java,
            "puzzivo_database"
        )
            .addMigrations(PuzzivoDatabase.MIGRATION_1_2)
        if (BuildConfig.DEBUG) {
            builder.fallbackToDestructiveMigration(dropAllTables = true)
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideAchievementDao(database: PuzzivoDatabase) = database.achievementDao()

    @Provides
    @Singleton
    fun provideStatisticsDao(database: PuzzivoDatabase) = database.statisticsDao()

    @Provides
    @Singleton
    fun provideChallengeDao(database: PuzzivoDatabase) = database.challengeDao()

    @Provides
    @Singleton
    fun provideProgressDao(database: PuzzivoDatabase) = database.progressDao()

    @Provides
    @Singleton
    fun provideShopDao(database: PuzzivoDatabase) = database.shopDao()

    // Feature Managers

    @Provides
    @Singleton
    fun provideAchievementManager(
        achievementDao: com.tds.puzzivo.core.database.AchievementDao,
        progressDao: com.tds.puzzivo.core.database.ProgressDao,
        progressionManager: com.tds.puzzivo.core.manager.ProgressionManager
    ): com.tds.puzzivo.core.manager.AchievementManager {
        return com.tds.puzzivo.core.manager.AchievementManager(achievementDao, progressDao, progressionManager)
    }

    @Provides
    @Singleton
    fun provideStatisticsManager(
        database: PuzzivoDatabase,
        statisticsDao: com.tds.puzzivo.core.database.StatisticsDao
    ): com.tds.puzzivo.core.manager.StatisticsManager {
        return com.tds.puzzivo.core.manager.StatisticsManager(database, statisticsDao)
    }

    @Provides
    @Singleton
    fun provideProgressionManager(
        progressDao: com.tds.puzzivo.core.database.ProgressDao
    ): com.tds.puzzivo.core.manager.ProgressionManager {
        return com.tds.puzzivo.core.manager.ProgressionManager(progressDao)
    }

    @Provides
    @Singleton
    fun provideChallengeManager(
        challengeDao: com.tds.puzzivo.core.database.ChallengeDao,
        progressionManager: com.tds.puzzivo.core.manager.ProgressionManager
    ): com.tds.puzzivo.core.manager.ChallengeManager {
        return com.tds.puzzivo.core.manager.ChallengeManager(challengeDao, progressionManager)
    }

    @Provides
    @Singleton
    fun provideThemeManager(
        progressionManager: com.tds.puzzivo.core.manager.ProgressionManager,
        achievementManager: com.tds.puzzivo.core.manager.AchievementManager,
        statisticsManager: com.tds.puzzivo.core.manager.StatisticsManager
    ): com.tds.puzzivo.core.manager.ThemeManager {
        return com.tds.puzzivo.core.manager.ThemeManager(progressionManager, achievementManager, statisticsManager)
    }

    @Provides
    @Singleton
    fun provideLeaderboardManager(
        firestore: FirebaseFirestore,
        authManager: AuthManager
    ): com.tds.puzzivo.core.manager.LeaderboardManager {
        return com.tds.puzzivo.core.manager.LeaderboardManager(firestore, authManager)
    }

    @Provides
    @Singleton
    fun provideAppInitializer(
        authManager: AuthManager,
        achievementManager: com.tds.puzzivo.core.manager.AchievementManager,
        progressionManager: com.tds.puzzivo.core.manager.ProgressionManager,
        remoteConfigManager: com.tds.puzzivo.core.config.RemoteConfigManager,
        @ApplicationScope scope: CoroutineScope
    ): com.tds.puzzivo.core.init.AppInitializer {
        return com.tds.puzzivo.core.init.AppInitializer(authManager, achievementManager, progressionManager, remoteConfigManager, scope)
    }
}
