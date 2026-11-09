package com.tds.puzzivo.feature.shop

import app.cash.turbine.test
import com.tds.puzzivo.R
import com.tds.puzzivo.core.database.ShopDao
import com.tds.puzzivo.core.manager.ProgressionManager
import com.tds.puzzivo.core.model.PlayerProgress
import com.tds.puzzivo.core.model.ShopItem
import com.tds.puzzivo.core.model.ShopItemType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShopViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var shopDao: ShopDao
    private lateinit var progressionManager: ProgressionManager

    private val sampleItems = listOf(
        ShopItem("hint_1", ShopItemType.HINT_PACK, R.string.shop_hints_pack_name, R.string.shop_hints_pack_desc, 100, 0, 0, 5, true, false),
        ShopItem("theme_1", ShopItemType.THEME, R.string.shop_theme_forest_name, R.string.shop_theme_forest_desc, 250, 0, 0, 1, false, true)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        shopDao = mockk(relaxed = true)
        progressionManager = mockk(relaxed = true)

        every { shopDao.getAllShopItems() } returns flowOf(sampleItems)
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
            val initial = awaitItem()
            testScheduler.advanceUntilIdle()
            val state = awaitItem()
            assertEquals(500, state.playerProgress.coins)
            assertEquals(50, state.playerProgress.gems)
            assertEquals(2, state.shopItems.size)
        }
    }

    @Test
    fun categoryFilteringWorksCorrectly() = runTest {
        val viewModel = ShopViewModel(shopDao, progressionManager)
        viewModel.onCategorySelected(ShopCategory.THEMES)

        viewModel.uiState.test {
            val initial = awaitItem()
            testScheduler.advanceUntilIdle()
            val state = awaitItem()
            assertEquals(1, state.shopItems.size)
            assertEquals(ShopItemType.THEME, state.shopItems[0].type)
        }
    }

    @Test
    fun buyItemFailsWithInsufficientBalance() = runTest {
        every { progressionManager.playerProgress } returns flowOf(PlayerProgress(coins = 10, gems = 0))
        val viewModel = ShopViewModel(shopDao, progressionManager)

        var success = false
        var msg = ""
        viewModel.buyItem(sampleItems[0]) { s, m ->
            success = s
            msg = m
        }
        testScheduler.advanceUntilIdle()

        assertFalse(success)
        assertEquals("Not enough coins!", msg)
    }

    @Test
    fun buyItemSucceedsWithSufficientCoins() = runTest {
        coEvery { progressionManager.spendCoins(100) } returns true
        val viewModel = ShopViewModel(shopDao, progressionManager)

        var success = false
        viewModel.buyItem(sampleItems[0]) { s, _ -> success = s }
        testScheduler.advanceUntilIdle()

        assertTrue(success)
    }
}
