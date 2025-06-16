package com.wneth.mpadtransport.viewmodels

import android.app.Application
import com.wneth.mpadtransport.configurations.MPADTransportApp
import com.wneth.mpadtransport.models.DispatchInspectionHeaderModel
import com.wneth.mpadtransport.models.InspectionModel
import com.wneth.mpadtransport.utilities.reverseStringParts

class InspectionActivityViewModel (application: Application): BaseViewModel(application){


    init {
        (application as MPADTransportApp).getAppComponent().inject(this)
    }

    fun getDispatchTripKMPassengerCount(dispatchId: Int, dispatchTripId: Int, km: Int): Int{
        val tickets = ticketReceiptRepository.getDispatchTripReceipt(dispatchId, dispatchTripId)

        val filteredTickets = tickets.filter { ticket ->
            when (ticket.routeDirectionId) {
                // Handle normal direction (FROM -> TO)
                1 -> ticket.fromSegmentKM <= km && ticket.toSegmentKM > km
                // Handle reversed direction (TO -> FROM)
                2 -> ticket.fromSegmentKM >= km && ticket.toSegmentKM < km
                else -> false // Handle any other cases if necessary
            }
        }

        return filteredTickets.size
    }


    fun getDispatchInspectionHeader(dispatchReferenceId:Int): DispatchInspectionHeaderModel{
        val tmpInspectDispatch = inspectionRepository.getDispatchInspectionHeader(dispatchReferenceId)

        var startTicket = tmpInspectDispatch.first().ticketId;
        var lastTicket = tmpInspectDispatch.last().ticketId;
        var tripCount = tmpInspectDispatch.distinctBy { it.tripId }.count()
        var directionName = if(tmpInspectDispatch.first().directionId == 1){
            "South Bound"
        }else{
            "North Bound"
        }
        return DispatchInspectionHeaderModel(startTicket, lastTicket, tripCount, directionName)
    }


    fun insertInspection(inspectionModel: InspectionModel): Boolean {
        val rowId = inspectionRepository.insert(inspectionModel)
        return rowId > 0
    }

}