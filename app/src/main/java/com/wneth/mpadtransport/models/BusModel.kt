package com.wneth.mpadtransport.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.wneth.mpadtransport.utilities.generateISODateTime
import java.time.LocalDateTime


@Entity(tableName = "buses", indices = [Index(value = ["busNumber","plateNumber"], unique = true)])
data class BusModel (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("Id")
    val id: Int,

    @ColumnInfo(name = "companyId")
    @SerializedName("CompanyId")
    val companyId: Int,

    @ColumnInfo(name = "busNumber")
    @SerializedName("BusNumber")
    val busNumber: Int,

    @ColumnInfo(name = "plateNumber")
    @SerializedName("PlateNumber")
    val plateNumber: String,

    @ColumnInfo(name = "airCondition")
    @SerializedName("AirCondition")
    val airCondition: Boolean,

    @ColumnInfo(name = "isActive")
    @SerializedName("IsActive")
    val isActive: Boolean = true,

    @ColumnInfo(name = "dateCreated")
    @SerializedName("DateCreated")
    val dateCreated: String,
 )