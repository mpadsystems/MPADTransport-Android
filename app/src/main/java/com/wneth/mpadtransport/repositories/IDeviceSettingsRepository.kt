package com.wneth.mpadtransport.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wneth.mpadtransport.models.DeviceSettingModel


@Dao
interface IDeviceSettingsRepository {

    @Query("SELECT * FROM deviceSettings")
    fun getDeviceSettings(): DeviceSettingModel?

    @Insert
    fun insert(deviceSetting: DeviceSettingModel)

    @Insert
    fun insertBulk(deviceSettings: List<DeviceSettingModel>): LongArray

    @Query("DELETE FROM deviceSettings")
    fun deleteAll()

    @Query("DELETE FROM sqlite_sequence WHERE name='deviceSettings'")
    fun resetAutoIncrement()
}