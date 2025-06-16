package com.wneth.mpadtransport.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.wneth.mpadtransport.utilities.generateISODateTime
import java.time.LocalDateTime


@Entity(tableName = "devices", indices = [Index(value = ["deviceName","serialNumber"], unique = true)])
data class DeviceModel (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("Id")
    val id: Int,

    @ColumnInfo(name = "companyId")
    @SerializedName("CompanyId")
    val companyId: Int,

    @ColumnInfo(name = "deviceName")
    @SerializedName("DeviceName")
    val deviceName: String,

    @ColumnInfo(name = "serialNumber")
    @SerializedName("SerialNumber")
    val serialNumber: String?,

    @ColumnInfo(name = "assigned")
    @SerializedName("Assigned")
    val assigned: Boolean,

    @ColumnInfo(name = "isActive")
    @SerializedName("IsActive")
    val isActive: Boolean = true,

    @ColumnInfo(name = "dateCreated")
    @SerializedName("DateCreated")
    val dateCreated: String,
 )