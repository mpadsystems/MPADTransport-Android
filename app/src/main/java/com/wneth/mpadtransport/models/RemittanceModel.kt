package com.wneth.mpadtransport.models

import android.util.Base64
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
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
@Entity(tableName = "remittances")
data class RemittanceModel(
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

    @ColumnInfo(name = "terminalId")
    @SerializedName("TerminalId")
    val terminalId: Int,

    @ColumnInfo(name = "remittedById")
    @SerializedName("RemittedById")
    val remittedById: Int,

    @ColumnInfo(name = "receivedById")
    @SerializedName("ReceivedById")
    val receivedById: Int,


    @ColumnInfo(name = "signature")
    @SerializedName("Signature")
    val signature: String,

    @ColumnInfo(name = "amount")
    @SerializedName("Amount")
    val amount: Double,


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


data class RemittanceWithNameModel(
    val id: Int?,
    val dispatchReferenceId: Int?,
    val terminalId: Int?,
    val terminalName: String?,
    val remittedById: Int?,
    val remittedByName: String?,
    val receivedById: Int?,
    val receivedByName: String?,
    val signature: String?,
    val remittedAmount: Double?,
    val dateCreated: String
)





