package com.wneth.mpadtransport.viewmodels

import android.app.Application
import com.wneth.mpadtransport.configurations.MPADTransportApp
import com.wneth.mpadtransport.models.RemittanceModel
import com.wneth.mpadtransport.models.RemittanceWithNameModel
import com.wneth.mpadtransport.models.TicketRevenueModel

class RemittanceActivityViewModel (application: Application): BaseViewModel(application){


    init {
        (application as MPADTransportApp).getAppComponent().inject(this)
    }


    fun insertRemittance(remittance: RemittanceModel){
        return remittanceRepository.insert(remittance)
    }


    fun computeRemittancesByDispatchId(dispatchReferenceId: Int): TicketRevenueModel {
        return ticketReceiptRepository.computeRevenueByDispatchId(dispatchReferenceId)
    }


    fun getRemittancesByDispatchId(dispatchReferenceId: Int): List<RemittanceWithNameModel> {
        val tmpRemittances = remittanceRepository.getRemittancesByDispatchId(dispatchReferenceId)
        return tmpRemittances ?: emptyList()
    }

}