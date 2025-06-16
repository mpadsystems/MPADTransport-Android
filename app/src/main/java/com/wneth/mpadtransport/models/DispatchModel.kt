package com.wneth.mpadtransport.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.wneth.mpadtransport.utilities.generateISODateTime
import java.time.LocalDateTime


/*
* NOTES
* Status
* 0. Pending
* 1. Synced
*
*/
@Entity(tableName = "dispatches", indices = [Index(value = ["referenceId"], unique = true)])
data class DispatchModel (
    @PrimaryKey(autoGenerate = true)
    @SerializedName("Id")
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "referenceId")
    @SerializedName("ReferenceId")
    val referenceId: Int,

    @ColumnInfo(name = "companyId")
    @SerializedName("CompanyId")
    val companyId: Int,


    @ColumnInfo(name = "deviceName")
    @SerializedName("DeviceName")
    val deviceName: String,


    @ColumnInfo(name = "dispatcherId")
    @SerializedName("DispatcherId")
    val dispatcherId: Int,

    @ColumnInfo(name = "driverId")
    @SerializedName("DriverId")
    val driverId: Int,

    @ColumnInfo(name = "conductorId")
    @SerializedName("ConductorId")
    val conductorId: Int,

    @ColumnInfo(name = "busId")
    @SerializedName("BusId")
    val busId: Int,

    @ColumnInfo(name = "status")
    @SerializedName("Status")
    val status: Int,

    @ColumnInfo(name = "dateCreated")
    @SerializedName("DateCreated")
    val dateCreated: String = generateISODateTime(),
)



@Entity(tableName = "dispatch_trips", indices = [Index(value = ["referenceId"], unique = true)])
data class DispatchTripModel (
    @PrimaryKey(autoGenerate = true)
    @SerializedName("Id")
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "companyId")
    @SerializedName("CompanyId")
    val companyId: Int,

    @ColumnInfo(name = "deviceName")
    @SerializedName("DeviceName")
    val deviceName: String,

    @ColumnInfo(name = "referenceId")
    @SerializedName("ReferenceId")
    val referenceId: Int,

    @ColumnInfo(name = "dispatchReferenceId")
    @SerializedName("DispatchReferenceId")
    val dispatchReferenceId: Int,


    @ColumnInfo(name = "routeId")
    @SerializedName("RouteId")
    val routeId: Int,


    @ColumnInfo(name = "directionId")
    @SerializedName("DirectionId")
    val directionId: Int,


    @ColumnInfo(name = "terminalId")
    @SerializedName("TerminalId")
    val terminalId: Int,


    @ColumnInfo(name = "reversedById")
    @SerializedName("ReversedById")
    val reversedById: Int,


    @ColumnInfo(name = "dateCreated")
    @SerializedName("DateCreated")
    val dateCreated: String = generateISODateTime(),
)


data class DispatchWithNameModel(
    val dispatchId: Int,
    val dispatchReferenceId: Int,
    val dispatchTripReferenceId: Int,
    val companyId: Int,
    val deviceName: String,
    val routeId: Int,
    val routeName: String,
    val directionId: Int,
    val terminalId: Int,
    val terminalName: String,
    val dispatcherId: Int,
    val dispatcherName: String,
    val driverId: Int,
    val driverName: String,
    val conductorId: Int,
    val conductorName: String,
    val busId: Int,
    val busNumber: Int,
    val busPlateNumber: String,
    val status: Int,
    val dateCreated: String
)

data class DispatchTripReportModel(
    val dispatch: DispatchWithNameModel,
    val receipts: List<TicketReceiptWithNameModel>
)