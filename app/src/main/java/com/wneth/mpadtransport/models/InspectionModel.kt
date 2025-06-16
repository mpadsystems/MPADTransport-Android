package com.wneth.mpadtransport.models

import android.util.Base64
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.wneth.mpadtransport.utilities.generateISODateTime
import java.time.LocalDateTime


@Entity(tableName = "inspections")
data class InspectionModel (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("Id")
    val id: Int,

    @ColumnInfo(name = "dispatchReferenceId")
    @SerializedName("DispatchReferenceId")
    val dispatchReferenceId: Int,


    @ColumnInfo(name = "dispatchTripReferenceId")
    @SerializedName("DispatchTripReferenceId")
    val dispatchTripReferenceId: Int,


    @ColumnInfo(name = "companyId")
    @SerializedName("CompanyId")
    val companyId: Int,

    @ColumnInfo(name = "deviceName")
    @SerializedName("DeviceName")
    val deviceName: String,

    @ColumnInfo(name = "inspectorId")
    @SerializedName("InspectorId")
    val inspectorId: Int,

    @ColumnInfo(name = "directionId")
    @SerializedName("DirectionId")
    val directionId: Int,

    @ColumnInfo(name = "routeId")
    @SerializedName("RouteId")
    val routeId: Int,

    @ColumnInfo(name = "segmentId")
    @SerializedName("SegmentId")
    val segmentId: Int,

    @ColumnInfo(name = "passengerCount")
    @SerializedName("PassengerCount")
    val passengerCount: Int,

    @ColumnInfo(name = "actualPassengerCount")
    @SerializedName("ActualPassengerCount")
    val actualPassengerCount: Int,


    @ColumnInfo(name = "signature")
    @SerializedName("Signature")
    val signature: String,

    @ColumnInfo(name = "dateCreated")
    @SerializedName("DateCreated")
    val dateCreated: String = generateISODateTime(),
 ){
    companion object {
        // Convert ByteArray to Base64 String
        fun fromByteArray(byteArray: ByteArray): String {
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        }

        // Convert Base64 String to ByteArray
        fun toByteArray(base64String: String): ByteArray {
            return Base64.decode(base64String, Base64.DEFAULT)
        }
    }
}

data class DispatchInspectionTicketModel(
    val dispatchId: Int,
    val tripId: Int,
    val ticketId: Int,
    val directionId: Int,
)

data class DispatchInspectionHeaderModel(
    val startTicket: Int,
    val lastTicket: Int,
    val tripCount: Int,
    val directionName: String
)
