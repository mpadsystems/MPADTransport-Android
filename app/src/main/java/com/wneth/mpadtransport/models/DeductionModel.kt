package com.wneth.mpadtransport.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/*
* deductionType
* 1. Expense
* 2. WithHolding
* */

@Entity(tableName = "deductions")
data class DeductionModel(

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

    @ColumnInfo(name = "deductionType")
    @SerializedName("DeductionType")
    val deductionType: Int,

    @ColumnInfo(name = "deductionTypeName")
    @SerializedName("DeductionTypeName")
    val deductionTypeName: String,

    @ColumnInfo(name = "isPercent")
    @SerializedName("IsPercent")
    val isPercent: Boolean,

    @ColumnInfo(name = "fraction")
    @SerializedName("Fraction")
    val fraction: Double,

    @ColumnInfo(name = "thresholdRangeA")
    @SerializedName("ThresholdRangeA")
    val thresholdRangeA: Double,

    @ColumnInfo(name = "thresholdRangeB")
    @SerializedName("ThresholdRangeB")
    val thresholdRangeB: Double,

    @ColumnInfo(name = "autoCompute")
    @SerializedName("AutoCompute")
    val autoCompute: Boolean,

    @ColumnInfo(name = "computeAfter")
    @SerializedName("ComputeAfter")
    val computeAfter: Int,

    @ColumnInfo(name = "isActive")
    @SerializedName("IsActive")
    val isActive: Boolean = true,

    @ColumnInfo(name = "dateCreated")
    @SerializedName("DateCreated")
    val dateCreated: String,

    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(companyId)
        parcel.writeString(name)
        parcel.writeInt(deductionType)
        parcel.writeString(deductionTypeName)
        parcel.writeByte(if (isPercent) 1 else 0)
        parcel.writeDouble(fraction)
        parcel.writeDouble(thresholdRangeA)
        parcel.writeDouble(thresholdRangeB)
        parcel.writeByte(if (autoCompute) 1 else 0)
        parcel.writeInt(computeAfter)
        parcel.writeByte(if (isActive) 1 else 0)
        parcel.writeString(dateCreated)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DeductionModel> {
        override fun createFromParcel(parcel: Parcel): DeductionModel {
            return DeductionModel(parcel)
        }

        override fun newArray(size: Int): Array<DeductionModel?> {
            return arrayOfNulls(size)
        }
    }
}
