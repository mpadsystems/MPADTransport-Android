package com.wneth.mpadtransport.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.wneth.mpadtransport.utilities.generateISODateTime
import java.time.LocalDateTime

/*
* NOTES
* Direction:
* 1. South Bound
* 2. North Bound
*
*
*/
@Entity(tableName = "routes")
data class RouteModel (
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

    @ColumnInfo(name = "directionId")
    @SerializedName("DirectionId")
    val directionId: Int,

    @ColumnInfo(name = "isActive")
    @SerializedName("IsActive")
    val isActive: Boolean = true,

    @ColumnInfo(name = "dateCreated")
    @SerializedName("DateCreated")
    val dateCreated: String,
)




@Entity(tableName = "route_segments")
data class RouteSegmentModel (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("Id")
    val id: Int,

    @ColumnInfo(name = "routeId")
    @SerializedName("RouteId")
    val routeId: Int,

    @ColumnInfo(name = "name")
    @SerializedName("Name")
    val name: String,

    @ColumnInfo(name = "description")
    @SerializedName("Description")
    val description: String?,

    @ColumnInfo(name = "distanceKM")
    @SerializedName("DistanceKM")
    val distanceKM: Int,

    @ColumnInfo(name = "isActive")
    @SerializedName("IsActive")
    val isActive: Boolean = true,

    @ColumnInfo(name = "dateCreated")
    @SerializedName("DateCreated")
    val dateCreated: String,
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readString().toString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(routeId)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeInt(distanceKM)
        parcel.writeByte(if (isActive) 1 else 0)
        parcel.writeString(dateCreated)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RouteSegmentModel> {
        override fun createFromParcel(parcel: Parcel): RouteSegmentModel {
            return RouteSegmentModel(parcel)
        }

        override fun newArray(size: Int): Array<RouteSegmentModel?> {
            return arrayOfNulls(size)
        }
    }
}
