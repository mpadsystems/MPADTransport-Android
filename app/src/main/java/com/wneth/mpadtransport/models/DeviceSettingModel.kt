package com.wneth.mpadtransport.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.wneth.mpadtransport.utilities.generateISODateTime


@Entity(tableName = "deviceSettings")
data class DeviceSettingModel (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("Id")
    val id: Int,

    @ColumnInfo(name = "companyId")
    @SerializedName("CompanyId")
    val companyId: Int,

    @ColumnInfo(name = "backUpFrequency")
    @SerializedName("BackUpFrequency")
    val backUpFrequency: Int,

    @ColumnInfo(name = "showTicketingCashGross")
    @SerializedName("ShowTicketingCashGross")
    val showTicketingCashGross: Boolean,

    @ColumnInfo(name = "showTicketingQR")
    @SerializedName("ShowTicketingQR")
    val showTicketingQR: Boolean,


    @ColumnInfo(name = "showTicketingBaggage")
    @SerializedName("ShowTicketingBaggage")
    val showTicketingBaggage: Boolean,


    @ColumnInfo(name = "showTicketingCashlessPayment")
    @SerializedName("ShowTicketingCashlessPayment")
    val showTicketingCashlessPayment: Boolean,

    @ColumnInfo(name = "showIngressoExpenses")
    @SerializedName("ShowIngressoExpenses")
    val showIngressoExpenses: Boolean,

    @ColumnInfo(name = "showIngressoWithholding")
    @SerializedName("ShowIngressoWithholding")
    val showIngressoWithholding: Boolean,

    @ColumnInfo(name = "showIngressoTotalCommission")
    @SerializedName("ShowIngressoTotalCommission")
    val showIngressoTotalCommission: Boolean,

    @ColumnInfo(name = "showIngressoDriverCommission")
    @SerializedName("ShowIngressoDriverCommission")
    val showIngressoDriverCommission: Boolean,

    @ColumnInfo(name = "showIngressoConductorCommission")
    @SerializedName("ShowIngressoConductorCommission")
    val showIngressoConductorCommission: Boolean,

    @ColumnInfo(name = "showIngressoNetCollection")
    @SerializedName("ShowIngressoNetCollection")
    val showIngressoNetCollection: Boolean,

    @ColumnInfo(name = "showIngressoDriverBonus")
    @SerializedName("ShowIngressoDriverBonus")
    val showIngressoDriverBonus: Boolean,

    @ColumnInfo(name = "showIngressoConductorBonus")
    @SerializedName("ShowIngressoConductorBonus")
    val showIngressoConductorBonus: Boolean,

    @ColumnInfo(name = "showIngressoPartialRemit")
    @SerializedName("ShowIngressoPartialRemit")
    val showIngressoPartialRemit: Boolean,


    @ColumnInfo(name = "showIngressoPrintSummaryPerTrip")
    @SerializedName("ShowIngressoPrintSummaryPerTrip")
    val showIngressoPrintSummaryPerTrip: Boolean,


    @ColumnInfo(name = "receiptTemplate")
    @SerializedName("ReceiptTemplate")
    val receiptTemplate: String,

    @ColumnInfo(name = "showRemittanceViewTickets")
    @SerializedName("ShowRemittanceViewTickets")
    val showRemittanceViewTickets: Boolean,


    @ColumnInfo(name = "showReverseViewTickets")
    @SerializedName("ShowReverseViewTickets")
    val showReverseViewTickets: Boolean,


    @ColumnInfo(name = "dateCreated")
    @SerializedName("DateCreated")
    val dateCreated: String,
)