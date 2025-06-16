package com.wneth.mpadtransport.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.wneth.mpadtransport.utilities.generateISODateTime
import java.time.LocalDateTime


@Entity(tableName = "fare")
data class FareModel (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("Id")
    val id: Int,

    @ColumnInfo(name = "companyId")
    @SerializedName("CompanyId")
    val companyId: Int,

    @ColumnInfo(name = "distanceKM")
    @SerializedName("DistanceKM")
    val distanceKM: Int,

    @ColumnInfo(name = "baseAmount")
    @SerializedName("BaseAmount")
    val baseAmount: Double,

    @ColumnInfo(name = "isActive")
    @SerializedName("IsActive")
    val isActive: Boolean = true,

    @ColumnInfo(name = "dateCreated")
    @SerializedName("DateCreated")
    val dateCreated: String,
)


@Entity(tableName = "fare_setups")
data class FareSetupModel (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("Id")
    val id: Int,

    @ColumnInfo(name = "companyId")
    @SerializedName("CompanyId")
    val companyId: Int,

    @ColumnInfo(name = "distanceKM")
    @SerializedName("DistanceKM")
    val distanceKM: Int,

    @ColumnInfo(name = "amount")
    @SerializedName("Amount")
    val amount: Double,

    @ColumnInfo(name = "isDiscount")
    @SerializedName("IsDiscount")
    val isDiscount: Boolean,

    @ColumnInfo(name = "isActive")
    @SerializedName("IsActive")
    val isActive: Boolean,
)