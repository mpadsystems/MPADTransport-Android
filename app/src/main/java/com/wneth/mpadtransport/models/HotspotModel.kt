package com.wneth.mpadtransport.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.wneth.mpadtransport.utilities.generateISODateTime
import java.time.LocalDateTime


/*
* NOTES
*
*
*
*/
@Entity(tableName = "hotspots")
data class HotspotModel (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("Id")
    val id: Int,

    @ColumnInfo(name = "companyId")
    @SerializedName("CompanyId")
    val companyId: Int,

    @ColumnInfo(name = "routeId")
    @SerializedName("RouteId")
    val routeId: Int,

    @ColumnInfo(name = "fromSegmentId")
    @SerializedName("FromSegmentId")
    val fromSegmentId: Int,

    @ColumnInfo(name = "toSegmentId")
    @SerializedName("ToSegmentId")
    val toSegmentId: Int,

    @ColumnInfo(name = "amount")
    @SerializedName("Amount")
    val amount: Double,

    @ColumnInfo(name = "customDiscountedAmount")
    @SerializedName("CustomDiscountedAmount")
    val customDiscountedAmount: Double,


    @ColumnInfo(name = "isActive")
    @SerializedName("IsActive")
    val isActive: Boolean = true,

    @ColumnInfo(name = "dateCreated")
    @SerializedName("DateCreated")
    val dateCreated: String
)


data class HotspotWithNameModel(
    val id: Int,
    val companyId: Int,
    val routeId: Int,
    val routeName: String,
    val fromSegmentId: Int,
    val fromSegmentName: String,
    val toSegmentId: Int,
    val toSegmentName: String,
    val amount: Double,
    val customDiscountedAmount: Double,
    val isActive: Boolean,
    val dateCreated: String
)