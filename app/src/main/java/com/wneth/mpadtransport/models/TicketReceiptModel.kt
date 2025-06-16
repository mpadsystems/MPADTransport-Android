package com.wneth.mpadtransport.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

import com.wneth.mpadtransport.utilities.generateISODateTime

/*
* NOTES
* Ticket Status
* 0. Fresh
* 1. Synced
* 2. Partially Remitted
* 3. Remitted
*/

@Entity(tableName = "ticket_receipts", indices = [Index(value = ["referenceId"], unique = true)])
data class TicketReceiptModel (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("Id")
    val id: Int,

    @ColumnInfo(name = "deviceName")
    @SerializedName("DeviceName")
    val deviceName: String,

    @ColumnInfo(name = "referenceId")
    @SerializedName("ReferenceId")
    val referenceId: Int,

    @ColumnInfo(name = "companyId")
    @SerializedName("CompanyId")
    val companyId: Int,

    @ColumnInfo(name = "conductorId")
    @SerializedName("ConductorId")
    val conductorId: Int,

    @ColumnInfo(name = "dispatchReferenceId")
    @SerializedName("DispatchReferenceId")
    val dispatchReferenceId: Int,

    @ColumnInfo(name = "dispatchTripReferenceId")
    @SerializedName("DispatchTripReferenceId")
    val dispatchTripReferenceId: Int,

    @ColumnInfo(name = "fromSegmentId")
    @SerializedName("FromSegmentId")
    val fromSegmentId: Int,

    @ColumnInfo(name = "toSegmentId")
    @SerializedName("ToSegmentId")
    val toSegmentId: Int,

    @ColumnInfo(name = "isBaggage")
    @SerializedName("IsBaggage")
    val isBaggage: Boolean,

    @ColumnInfo(name = "totalDistanceKM")
    @SerializedName("TotalDistanceKM")
    val totalDistanceKM: Int,

    @ColumnInfo(name = "discountId")
    @SerializedName("DiscountId")
    val discountId: Int,

    @ColumnInfo(name = "discountAmount")
    @SerializedName("DiscountAmount")
    val ticketDiscount: Int,

    @ColumnInfo(name = "ticketTotalAmount")
    @SerializedName("TicketTotalAmount")
    val ticketTotalAmount: Double,


    @ColumnInfo(name = "paymentType")
    @SerializedName("PaymentType")
    val paymentType: Int,

    @ColumnInfo(name = "paymentReferenceNumber")
    @SerializedName("PaymentReferenceNumber")
    val paymentReferenceNumber: String,


    @ColumnInfo(name = "status")
    @SerializedName("Status")
    val status: Int,

    @ColumnInfo(name = "dateCreated")
    @SerializedName("DateCreated")
    val dateCreated: String = generateISODateTime(),
)

data class TripTicketReceiptModel(
    val id: Int,
    val referenceId: Int,
    val companyId: Int,
    val routeId: Int,
    val routeName: String,
    val routeDirectionId: Int,
    val dispatchReferenceId: Int,
    val dispatchTripReferenceId: Int,
    val ticketTotalAmount: Double,
    val paymentType: Int,
    val paymentReferenceNumber: String,
    val ticketCount: Int,
    val dateCreated: String
)

/*
* NOTES: paymentType
* 1. CASH
* 2. GCASH
* 3. MAYA
* 4. DEBIT
* 5. CREDIT
 */
data class TicketReceiptWithNameModel(
    val id: Int,
    val deviceName: String,
    val referenceId: Int,
    val companyId: Int,
    val companyName: String,
    val dispatchReferenceId: Int,
    val dispatchTripReferenceId: Int,
    val routeDirectionId: Int,
    //val dispatcherId: Int,
    //val dispatcherName: String,
    val driverId: Int,
    val driverName: String,
    val conductorId: Int,
    val conductorName: String,
    val busId: Int,
    val busNumber: Int,
    val busPlateNumber: String,
    val routeId: Int,
    val routeName: String,
    val fromSegmentId: Int,
    val fromSegmentKM: Int,
    val fromSegmentName: String,
    val toSegmentId: Int,
    val toSegmentKM: Int,
    val toSegmentName: String,
    val isBaggage: Boolean,
    val totalDistanceKM: Int,
    val discountId: Int,
    val discountName: String,
    val discountAmount: Int,
    val ticketTotalAmount: Double,
    val paymentType: Int,
    val paymentReferenceNumber: String,
    val dateCreated: String = generateISODateTime(),
)


data class TicketRevenueModel(
    val totalRevenue: Double,
    val totalRemitted: Double,
    val totalUnremitted: Double
)

data class RemainingPassengerModel(
    val receiptId: Int,
    val segmentId: Int,
    val fromSegmentId: Int,
    val toSegmentId: Int,
    val fromKM: Int,
    val toKM: Int,
    val totalDistanceKM: Int
)

data class TicketPaymentDataModel(val paymentType: Int, val referenceNumber: String)