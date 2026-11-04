package com.tds.puzzivo.feature.shop

import app.cash.turbine.test
import com.tds.puzzivo.core.database.ShopDao
import com.tds.puzzivo.core.manager.ProgressionManager
import com.tds.puzzivo.core.model.PlayerProgress
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShopViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var shopDao: ShopDao
    private lateinit var progressionManager: ProgressionManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        shopDao = mockk(relaxed = true)
        progressionManager = mockk(relaxed = true)

        every { shopDao.getAllShopItems() } returns flowOf(emptyList())
        every { progressionManager.playerProgress } returns flowOf(PlayerProgress(coins = 500, gems = 50))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiStateEmitsPlayerProgressAndShopItems() = runTest {
        val viewModel = ShopViewModel(shopDao, progressionManager)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(500, state.playerProgress.coins)
            assertEquals(50, state.playerProgress.gems)
            assertEquals(0, state.shopItems.size)
        }
    }
}
