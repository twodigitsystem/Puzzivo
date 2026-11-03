package com.tds.puzzivo.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tds.puzzivo.core.model.ShopItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopDao {
    @Query("SELECT * FROM shop_items ORDER BY priceCoins ASC")
    fun getAllShopItems(): Flow<List<ShopItem>>

    @Query("SELECT * FROM shop_items WHERE id = :id")
    suspend fun getShopItemById(id: String): ShopItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShopItem(item: ShopItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShopItem>)

    @Query("DELETE FROM shop_items")
    suspend fun deleteAll()
}
