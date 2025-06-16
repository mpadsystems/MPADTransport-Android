package com.wneth.mpadtransport.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.wneth.mpadtransport.utilities.generateISODateTime
import java.time.LocalDateTime

@Entity(tableName = "discounts")
data class DiscountModel (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("Id")
    val id: Int,

    @ColumnInfo(name = "companyId")
    @SerializedName("CompanyId")
    val companyId: Int,

    @ColumnInfo(name = "name")
    @SerializedName("Name")
    val name: String,

    @ColumnInfo(name = "description")
    @SerializedName("Description")
    val description: String,

    @ColumnInfo(name = "fraction")
    @SerializedName("Fraction")
    val fraction: Int,

    @ColumnInfo(name = "isActive")
    @SerializedName("IsActive")
    val isActive: Boolean = true,

    @ColumnInfo(name = "dateCreated")
    @SerializedName("DateCreated")
    val dateCreated: String,
 )