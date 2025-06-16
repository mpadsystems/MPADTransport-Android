package com.wneth.mpadtransport.repositories

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.wneth.mpadtransport.models.DiscountModel


@Dao
interface IDiscountRepository {

    @Query("SELECT * FROM discounts WHERE isActive = 1")
    fun getDiscounts(): List<DiscountModel>

    @Query("SELECT * FROM discounts WHERE id = :id AND isActive = 1")
    fun getDiscountById(id:Int): DiscountModel?

    @Query("SELECT * FROM discounts WHERE isActive = 1 AND name LIKE '%'||:keyword||'%' OR description LIKE '%'||:keyword||'%'")
    fun filterDiscounts(keyword:String): List<DiscountModel>?

    @Insert
    fun insert(discount: DiscountModel)

    @Insert
    fun insertBulk(discounts: List<DiscountModel>): LongArray

    @Delete
    fun delete(discounts: DiscountModel)

    @Query("DELETE FROM discounts")
    fun deleteAll()

    @Query("DELETE FROM sqlite_sequence WHERE name='discounts'")
    fun resetAutoIncrement()
}