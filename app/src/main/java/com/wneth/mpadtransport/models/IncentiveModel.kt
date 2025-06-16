package com.wneth.mpadtransport.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.wneth.mpadtransport.utilities.generateISODateTime
import java.time.LocalDateTime

@Entity(tableName = "incentives")
data class IncentiveModel (

    @PrimaryKey(autoGenerate = true)
    @SerializedName("Id")
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "companyId")
    @SerializedName("CompanyId")
    val companyId: Int,

    @ColumnInfo(name = "roleId")
    @SerializedName("RoleId")
    val roleId: Int,

    @ColumnInfo(name = "type")
    @SerializedName("Type")
    val type: String,

    @ColumnInfo(name = "fraction")
    @SerializedName("Fraction")
    val fraction: Double,

    @ColumnInfo(name = "isPercent")
    @SerializedName("IsPercent")
    val isPercent: Boolean,

    @ColumnInfo(name = "thresholdRangeA")
    @SerializedName("ThresholdRangeA")
    val thresholdRangeA: Double,

    @ColumnInfo(name = "thresholdRangeB")
    @SerializedName("ThresholdRangeB")
    val thresholdRangeB: Double,

    @ColumnInfo(name = "computeAfter")
    @SerializedName("ComputeAfter")
    val computeAfter: Int,

    @ColumnInfo(name = "isActive")
    @SerializedName("IsActive")
    val isActive: Boolean = true,

    @ColumnInfo(name = "dateCreated")
    @SerializedName("DateCreated")
    val dateCreated: String,

    )


data class IncentiveWithRoleModel (
    val id: Int,
    val companyId: Int,
    val roleId: Int,
    val roleName: String,
    val type: String,
    val fraction: Double,
    val isPercent: Boolean,
    val thresholdRangeA: Double,
    val thresholdRangeB: Double,
    val computeAfter: Int,
    val isActive: Boolean = true,
    val dateCreated: String
)