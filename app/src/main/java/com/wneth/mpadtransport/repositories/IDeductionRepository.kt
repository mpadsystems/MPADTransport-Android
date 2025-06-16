package com.wneth.mpadtransport.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wneth.mpadtransport.models.DeductionModel

@Dao
interface IDeductionRepository {
    @Query("SELECT * FROM deductions WHERE isActive = 1")
    fun getDeductions(): DeductionModel?

    @Query("SELECT * FROM deductions WHERE deductionType = 1 AND isActive = 1")
    fun getDeductionExpenses(): List<DeductionModel>?


    @Query("SELECT * FROM deductions WHERE deductionType = 2 AND isActive = 1")
    fun getDeductionWithHoldings(): List<DeductionModel>?


    @Query("SELECT * FROM deductions WHERE id = :id AND isActive = 1")
    fun getDeductionById(id: Int): List<DeductionModel>?


    @Query("SELECT * FROM deductions WHERE deductionType = 1 AND autoCompute = 1 AND (thresholdRangeA < 1 AND thresholdRangeB < 1)")
    fun getAutoComputeExpensesNoThreshold(): List<DeductionModel>?


    @Query("SELECT * FROM deductions WHERE deductionType = 1 AND autoCompute = 1 AND (thresholdRangeA > 0 AND thresholdRangeB > 0)")
    fun getAutoComputeExpensesWithThreshold(): List<DeductionModel>?

    @Insert
    fun insert(deduction: DeductionModel)

    @Insert
    fun insertBulk(deductions: List<DeductionModel>): LongArray

    @Query("DELETE FROM deductions")
    fun deleteAll()

    @Query("DELETE FROM sqlite_sequence WHERE name='deductions'")
    fun resetAutoIncrement()
}