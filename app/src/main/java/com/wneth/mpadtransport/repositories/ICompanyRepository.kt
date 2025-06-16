package com.wneth.mpadtransport.repositories


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.wneth.mpadtransport.models.CompanyModel
import com.wneth.mpadtransport.models.UserModel


@Dao
interface ICompanyRepository {
    @Query("SELECT * FROM company")
    fun getCompanyDetails(): CompanyModel?

    @Query("SELECT deviceName FROM devices WHERE id = :deviceId")
    fun getDeviceNameById(deviceId: Int): String?

    @Insert
    fun insert(company: CompanyModel)

    @Query("DELETE FROM company")
    fun deleteAll()

    @Query("DELETE FROM sqlite_sequence WHERE name='company'")
    fun resetAutoIncrement()
}