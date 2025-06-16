package com.wneth.mpadtransport.repositories

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.wneth.mpadtransport.models.BusModel
import com.wneth.mpadtransport.models.FareModel
import com.wneth.mpadtransport.models.FareSetupModel


@Dao
interface IFareRepository {

    @Query("SELECT * FROM fare WHERE isActive = 1")
    fun getFare(): FareModel?

    @Query("SELECT * FROM fare_setups WHERE isActive = 1")
    fun getFareSetups(): List<FareSetupModel>?


    @Query("SELECT * FROM fare_setups WHERE isActive = 1 AND distanceKm = :km and isDiscount = :isDiscount")
    fun getFareSetupByKM(km:Int, isDiscount:Boolean): FareSetupModel?

    @Query("SELECT baseAmount FROM fare WHERE isActive = 1")
    fun getBaseFare(): Double?

    @Insert
    fun insertFare(fare: FareModel)

    @Insert
    fun insertFareBulk(fare: List<FareModel>): LongArray


    @Insert
    fun insertFareSetups(fareSetup: FareSetupModel)

    @Insert
    fun insertFareSetupsBulk(fareSetups: List<FareSetupModel>): LongArray

    @Query("DELETE FROM fare")
    fun deleteAllFare()

    @Query("DELETE FROM sqlite_sequence WHERE name='fare'")
    fun resetAutoIncrementFare()

    @Query("DELETE FROM fare_setups")
    fun deleteAllFareSetups()

    @Query("DELETE FROM sqlite_sequence WHERE name='fare_setups'")
    fun resetAutoIncrementFareSetups()
}