package com.wneth.mpadtransport.viewmodels

import android.app.Application
import com.wneth.mpadtransport.configurations.MPADTransportApp
import com.wneth.mpadtransport.models.DiscountModel
import com.wneth.mpadtransport.models.DispatchWithNameModel
import com.wneth.mpadtransport.models.HotspotWithNameModel
import com.wneth.mpadtransport.models.TicketReceiptModel
import com.wneth.mpadtransport.models.TicketReceiptWithNameModel
import com.wneth.mpadtransport.models.RouteSegmentModel
import com.wneth.mpadtransport.models.TripTicketReceiptModel

import com.wneth.mpadtransport.utilities.generateISODateTime
import org.json.JSONObject
import kotlin.math.round


class TicketReceiptActivityViewModel (application: Application): BaseViewModel(application){


    init {
        (application as MPADTransportApp).getAppComponent().inject(this)
        getLatestDispatchTrip()
        getDiscounts()
    }

    fun getLatestDispatchTrip(): DispatchWithNameModel? {
        return dispatchRepository.getLatestDispatchTrip()
    }

    fun getDiscounts(): List<DiscountModel> {
        return discountRepository.getDiscounts()
    }


    fun getAllReceiptWithNamesByDispatchReferenceId(dispatchReferenceId: Int): List<TicketReceiptWithNameModel> {
        return ticketReceiptRepository.getAllReceiptWithNamesByDispatchId(dispatchReferenceId)
    }


    fun getDispatchTripReceiptGroup(dispatchReferenceId: Int): List<TripTicketReceiptModel> {
        return ticketReceiptRepository.getDispatchTripReceiptByRoute(dispatchReferenceId)
    }

    fun getDispatchTripReceipts(dispatchReferenceId:Int, dispatchTripReferenceId: Int, searchKeyword: String = ""): List<TicketReceiptWithNameModel> {
        val tmpReceipts = ticketReceiptRepository.getDispatchTripReceipt(dispatchReferenceId,dispatchTripReferenceId) ?: return emptyList()
        if (searchKeyword.isEmpty()) {
            return tmpReceipts
        }
        return tmpReceipts.filter { receipt ->
            receipt.referenceId.toString().contains(searchKeyword, ignoreCase = true) ||
                    receipt.fromSegmentName.contains(searchKeyword, ignoreCase = true) ||
                    receipt.toSegmentName.contains(searchKeyword, ignoreCase = true)
        }
    }


    fun getRouteSegmentByRouteIdAndKM(routeId: Int, km: Int): RouteSegmentModel?{
        val routeSegments = routeRepository.getRouteSegmentByRouteIdAndKM(routeId,km)
        return routeSegments
    }

    fun getKMRemainingPassengerReference(dispatchReferenceId: Int,dispatchTripReferenceId: Int, selectedFromKM: Int, selectedToKM: Int, directionId: Int): Int{
        val reference = ticketReceiptRepository.getKMRemainingPassengerReference(dispatchReferenceId,dispatchTripReferenceId)

        val filteredTickets = reference.filter { ticket ->
            when (directionId) {
                // Handle normal direction (FROM -> TO)
                1 -> ticket.fromKM <= selectedFromKM && ticket.toKM > selectedFromKM
                // Handle reversed direction (TO -> FROM)
                2 -> ticket.fromKM >= selectedFromKM && ticket.toKM < selectedFromKM
                else -> false // Handle any other cases if necessary
            }
        }

        return filteredTickets.size
    }

    fun getRouteSegmentKmsByRouteId(routeId: Int): List<Int> {
        return ticketReceiptRepository.getRouteSegmentKmsByRouteId(routeId)
    }

    fun getSegmentHotspotAmount(routeId: Int, fromSegmentId: Int, toSegmentId: Int): HotspotWithNameModel?{
        return hotspotRepository.getSegmentHotspotAmount(routeId, fromSegmentId,toSegmentId)
    }


    fun computeTotalFare(km: Int, discount: Int, tmpHotspotInfo: HotspotWithNameModel?): Double {

        var discountedFare: Double = 0.00

        val tmpFareSetup = fareRepository.getFareSetupByKM(km, (discount > 0))
        if (tmpFareSetup?.amount != null){
            discountedFare = (tmpFareSetup.amount)
            return round(discountedFare)
        }

        if (tmpHotspotInfo?.amount!! > 0.0){
            discountedFare = if (discount > 0) {
                if (tmpHotspotInfo.customDiscountedAmount > 0.0){
                    tmpHotspotInfo.customDiscountedAmount
                }else{
                    ((tmpHotspotInfo.amount)) * (1 - discount.toDouble() / 100.00)
                }
            } else {
                (tmpHotspotInfo.amount)
            }
        }

        return round(discountedFare)
    }


    fun insertReceipt(receipt: TicketReceiptWithNameModel) {
        val tmpReceipt = TicketReceiptModel(
            id = 0,
            deviceName = receipt.deviceName,
            referenceId = receipt.referenceId,
            companyId = receipt.companyId,
            conductorId = receipt.conductorId,
            dispatchReferenceId = receipt.dispatchReferenceId,
            dispatchTripReferenceId = receipt.dispatchTripReferenceId,
            fromSegmentId = receipt.fromSegmentId,
            toSegmentId = receipt.toSegmentId,
            totalDistanceKM = receipt.totalDistanceKM,
            isBaggage = receipt.isBaggage,
            discountId = receipt.discountId,
            ticketDiscount = receipt.discountAmount,
            ticketTotalAmount = receipt.ticketTotalAmount,
            paymentType = receipt.paymentType,
            paymentReferenceNumber = receipt.paymentReferenceNumber,
            status = 0,
            dateCreated = generateISODateTime()
        )
        ticketReceiptRepository.insertReceipt(tmpReceipt)
    }
}