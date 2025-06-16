package com.wneth.mpadtransport.viewmodels

import android.app.Application
import com.wneth.mpadtransport.configurations.MPADTransportApp
import com.wneth.mpadtransport.models.HttpResponseModel

import com.wneth.mpadtransport.utilities.payApiUrl


class MPADPayActivityViewModel (application: Application): BaseViewModel(application){


    init {
        (application as MPADTransportApp).getAppComponent().inject(this)
    }


    suspend fun registerMPADPayUser(userInfo: Map<String, String>): HttpResponseModel{
        return api.post("${payApiUrl}/Auth/Register",userInfo)
    }

    suspend fun addCredit(amount: Double, phoneNumber: String, personnel: String): HttpResponseModel{
        val creditInfo = mapOf(
            "UserId" to "0",
            "Amount" to amount.toString(),
            "PhoneNumber" to phoneNumber,
            "CompanyName" to sharedPrefs.getString("sharedCompanyName", ""),
            "DeviceName" to sharedPrefs.getString("sharedValidatedDeviceName", ""),
            "ProcessedByName" to personnel,
            "SecretCode" to "dsfHj!48@io30pLskmHtj_23mSkmALD2DvVns!"
        )
        return api.post("${payApiUrl}/ClientTransaction/AddCredit",creditInfo)
    }

    suspend fun proceedMPADPayTransaction(transactionCode: String, amount: String, receiptNumber: String): HttpResponseModel{
        val transaction = mapOf(
            "Id" to "0",
            "TransactionType" to "1",
            "AmountToPay" to amount,
            "ReceiptNumber" to receiptNumber,
            "TransactionCode" to transactionCode,
            "CompanyName" to sharedPrefs.getString("sharedCompanyName",""),
            "DeviceName" to sharedPrefs.getString("sharedValidatedDeviceName",""),
            "ProcessedByName" to sharedPrefs.getString("sharedConductorName",""),
            "SecretCode" to "dsfHj!48@io30pLskmHtj_23mSkmALD2DvVns!"
        )
        return api.post("${payApiUrl}/ClientTransaction/PerformTransaction",transaction)
    }


}