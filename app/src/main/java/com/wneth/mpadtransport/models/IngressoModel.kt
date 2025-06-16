package com.wneth.mpadtransport.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.wneth.mpadtransport.utilities.generateISODateTime
import java.time.LocalDateTime

/*
* DeductionType
* 1. Expenses
* 2. WithHoldings
* */
@Entity(tableName = "ingresso", indices = [Index(value = ["referenceId"], unique = true)])
data class IngressoModel(

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

    @ColumnInfo(name = "dispatchReferenceId")
    @SerializedName("DispatchReferenceId")
    val dispatchReferenceId: Int,

    @ColumnInfo(name = "terminalId")
    @SerializedName("TerminalId")
    val terminalId: Int,

    @ColumnInfo(name = "revenue")
    @SerializedName("Revenue")
    val revenue: Double,


    @ColumnInfo(name = "addedTicketAmount")
    @SerializedName("AddedTicketAmount")
    val addedTicketAmount: Double,

    @ColumnInfo(name = "cancelledTicketAmount")
    @SerializedName("CancelledTicketAmount")
    val cancelledTicketAmount: Double,

    @ColumnInfo(name = "finalRevenue")
    @SerializedName("FinalRevenue")
    val finalRevenue: Double,


    @ColumnInfo(name = "driverCommission")
    @SerializedName("DriverCommission")
    val driverCommission: Double,


    @ColumnInfo(name = "conductorCommission")
    @SerializedName("ConductorCommission")
    val conductorCommission: Double,


    @ColumnInfo(name = "totalCommission")
    @SerializedName("TotalCommission")
    val totalCommission: Double,

    @ColumnInfo(name = "driverBonus")
    @SerializedName("DriverBonus")
    val driverBonus: Double,

    @ColumnInfo(name = "conductorBonus")
    @SerializedName("ConductorBonus")
    val conductorBonus: Double,


    @ColumnInfo(name = "netCollection")
    @SerializedName("NetCollection")
    val netCollection: Double,


    @ColumnInfo(name = "partialRemittedAmount")
    @SerializedName("PartialRemittedAmount")
    val partialRemittedAmount: Double,


    @ColumnInfo(name = "finalRemittedAmount")
    @SerializedName("FinalRemittedAmount")
    val finalRemittedAmount: Double,


    @ColumnInfo(name = "shortOverAmount")
    @SerializedName("ShortOverAmount")
    val shortOverAmount: Double,


    @ColumnInfo(name = "shortOverTarget")
    @SerializedName("ShortOverTarget")
    val shortOverTarget: String,

    @ColumnInfo(name = "status")
    @SerializedName("Status")
    val status: Int = 0,

    @ColumnInfo(name = "dateCreated")
    @SerializedName("DateCreated")
    val dateCreated: String = generateISODateTime(),

    )



@Entity(tableName = "ingresso_deductions")
data class IngressoDeductionModel(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("Id")
    val id: Int,

    @ColumnInfo(name = "deviceName")
    @SerializedName("DeviceName")
    val deviceName: String,

    @ColumnInfo(name = "companyId")
    @SerializedName("CompanyId")
    val companyId: Int,

    @ColumnInfo(name = "dispatchReferenceId")
    @SerializedName("DispatchReferenceId")
    val dispatchReferenceId: Int,

    @ColumnInfo(name = "ingressoReferenceId")
    @SerializedName("IngressoReferenceId")
    val ingressoReferenceId: Int,

    @ColumnInfo(name = "deductionId")
    @SerializedName("DeductionId")
    val deductionId: Int,

    @ColumnInfo(name = "amount")
    @SerializedName("Amount")
    val amount: Double,
    )

data class IngressoDeductionWithNameModel(
    val id: Long,
    val ingressoReferenceId: Int,
    val deductionId: Int,
    val deductionName: String,
    val deductionType: Int,
    val amount: Double,
    val autoCompute: Boolean,
    val computeAfter: Int
)




