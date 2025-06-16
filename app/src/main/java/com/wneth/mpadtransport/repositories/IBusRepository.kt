package com.wneth.mpadtransport.repositories

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.wneth.mpadtransport.models.BusModel


@Dao
interface IBusRepository {

    @Query("SELECT * FROM buses WHERE isActive = 1")
    fun getBuses(): List<BusModel>?

    @Query("SELECT * FROM buses WHERE id = :id AND isActive = 1")
    fun getBus(id:Int): BusModel?

    @Query("SELECT * FROM buses WHERE isActive = 1 AND busNumber LIKE '%'||:keyword||'%' OR plateNumber LIKE '%'||:keyword||'%'")
    fun filterBuses(keyword:String): List<BusModel>?

    @Insert
    fun insert(bus: BusModel)

    @Insert
    fun insertBulk(buses: List<BusModel>): LongArray

    @Delete
    fun delete(bus: BusModel)

    @Query("DELETE FROM buses")
    fun deleteAll()

    @Query("DELETE FROM sqlite_sequence WHERE name='buses'")
    fun resetAutoIncrement()
}