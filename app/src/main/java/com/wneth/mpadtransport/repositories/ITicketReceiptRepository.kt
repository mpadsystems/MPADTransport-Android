package com.wneth.mpadtransport.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wneth.mpadtransport.models.RemainingPassengerModel
import com.wneth.mpadtransport.models.TicketRevenueModel
import com.wneth.mpadtransport.models.TicketReceiptModel
import com.wneth.mpadtransport.models.TicketReceiptWithNameModel
import com.wneth.mpadtransport.models.TripTicketReceiptModel

@Dao
interface ITicketReceiptRepository {

    @Query("""
        SELECT MAX(referenceId) + 1 FROM ticket_receipts
    """)
    fun validateTicketReceiptReferenceId(): Int

    @Query("SELECT distanceKM FROM route_segments WHERE routeId = :routeId ORDER BY distanceKM ASC")
    fun getRouteSegmentKmsByRouteId(routeId: Int): List<Int>


    @Query("""
        SELECT receipt.id, 
               receipt.deviceName as deviceName,
               receipt.referenceId AS referenceId,
               receipt.companyId, 
               company.companyName, 
               receipt.dispatchReferenceId, 
               receipt.dispatchTripReferenceId,
               dispatchTrip.directionId as routeDirectionId,
               userdispatcher.id AS dispatcherId, 
               userdispatcher.fullName AS dispatcherName, 
               userdriver.id AS driverId, 
               userdriver.fullName AS driverName, 
               userConductor.id AS conductorId, 
               userConductor.fullName AS conductorName, 
               bus.id as busId,
               bus.busNumber as busNumber,
               bus.plateNumber as busPlateNumber,
               route.id AS routeId, 
               route.name AS routeName, 
               receipt.fromSegmentId,
               fromSegment.distanceKM AS fromSegmentKM,
               fromSegment.name AS fromSegmentName,  
               receipt.toSegmentId, 
               tosegment.distanceKM AS toSegmentKM, 
               toSegment.name AS toSegmentName, 
               receipt.isBaggage,
               receipt.totalDistanceKM,
               receipt.discountId, 
               discount.name As discountName,
               discount.fraction as discountAmount, 
               receipt.ticketTotalAmount,
               receipt.paymentType,
               receipt.paymentReferenceNumber,
               receipt.dateCreated
        FROM ticket_receipts as receipt
        INNER JOIN company ON receipt.companyId = company.id
        INNER JOIN route_segments AS fromSegment ON receipt.fromSegmentId = fromSegment.id
        INNER JOIN route_segments AS toSegment ON receipt.toSegmentId = toSegment.id
        INNER JOIN discounts AS discount ON receipt.discountId = discount.id
        INNER JOIN dispatches AS dispatch ON dispatch.referenceId = receipt.dispatchReferenceId
        INNER JOIN dispatch_trips as dispatchTrip ON dispatchTrip.referenceId = receipt.dispatchTripReferenceId
        INNER JOIN routes AS route ON dispatchTrip.routeId = route.id
        INNER JOIN buses AS bus On bus.id = dispatch.busId
        INNER JOIN users AS userDispatcher ON dispatch.dispatcherId = userDispatcher.id
        INNER JOIN users AS userDriver ON dispatch.driverId = userdriver.id
        INNER JOIN users AS userConductor ON receipt.conductorId = userconductor.id
        WHERE receipt.dispatchReferenceId = :dispatchReferenceId
        ORDER BY receipt.id DESC
    """)
    fun getAllReceiptWithNamesByDispatchId(dispatchReferenceId: Int): List<TicketReceiptWithNameModel>


    @Query("""
        SELECT 
            receipt.deviceName as deviceName,
            receipt.dispatchTripReferenceId AS id, 
            receipt.referenceId AS referenceId,
            receipt.companyId, 
            dispatchTrip.routeId,
            route.name AS routeName,
            route.directionId as routeDirectionId,
            receipt.dispatchReferenceId, 
            receipt.dispatchTripReferenceId, 
            receipt.paymentType,
            receipt.paymentReferenceNumber,
            SUM(receipt.ticketTotalAmount) AS ticketTotalAmount, 
            COUNT(receipt.id) AS ticketCount,
            dispatchTrip.dateCreated AS dateCreated
        FROM ticket_receipts AS receipt 
        INNER JOIN dispatch_trips AS dispatchTrip ON receipt.dispatchTripReferenceId = dispatchTrip.referenceId
        INNER JOIN routes AS route ON route.id = dispatchTrip.routeId
        WHERE receipt.dispatchReferenceId = :dispatchReferenceId
        GROUP BY receipt.dispatchTripReferenceId
    """)
    fun getDispatchTripReceiptByRoute(dispatchReferenceId:Int): List<TripTicketReceiptModel>



    @Query("""
        SELECT receipt.id, 
               receipt.deviceName as deviceName,
               receipt.referenceId AS referenceId,
               receipt.companyId, 
               company.companyName, 
               receipt.dispatchReferenceId, 
               receipt.dispatchTripReferenceId,
               dispatchTrip.directionId as routeDirectionId,
               userdispatcher.id AS dispatcherId, 
               userdispatcher.fullName AS dispatcherName, 
               userdriver.id AS driverId, 
               userdriver.fullName AS driverName, 
               userConductor.id AS conductorId, 
               userConductor.fullName AS conductorName, 
               bus.id as busId,
               bus.busNumber as busNumber,
               bus.plateNumber as busPlateNumber,
               route.id AS routeId, 
               route.name AS routeName, 
               receipt.fromSegmentId,
               fromSegment.distanceKM AS fromSegmentKM,
               fromSegment.name AS fromSegmentName,  
               receipt.toSegmentId, 
               tosegment.distanceKM AS toSegmentKM, 
               toSegment.name AS toSegmentName, 
               receipt.isBaggage,
               receipt.totalDistanceKM, 
               receipt.discountId, 
               discount.name As discountName,
               discount.fraction as discountAmount, 
               receipt.ticketTotalAmount,
               receipt.paymentType,
               receipt.paymentReferenceNumber,
               receipt.dateCreated
        FROM ticket_receipts as receipt
        INNER JOIN company ON receipt.companyId = company.id
        INNER JOIN route_segments AS fromSegment ON receipt.fromSegmentId = fromSegment.id
        INNER JOIN route_segments AS toSegment ON receipt.toSegmentId = toSegment.id
        INNER JOIN discounts AS discount ON receipt.discountId = discount.id
        INNER JOIN dispatches AS dispatch ON dispatch.referenceId = receipt.dispatchReferenceId
        INNER JOIN dispatch_trips as dispatchTrip ON dispatchTrip.referenceId = receipt.dispatchTripReferenceId
        INNER JOIN routes AS route ON dispatchTrip.routeId = route.id
        INNER JOIN buses AS bus On bus.id = dispatch.busId
        INNER JOIN users AS userDispatcher ON dispatch.dispatcherId = userDispatcher.id
        INNER JOIN users AS userDriver ON dispatch.driverId = userdriver.id
        INNER JOIN users AS userConductor ON dispatch.conductorId = userconductor.id
        WHERE receipt.dispatchReferenceId = :dispatchReferenceId AND receipt.dispatchTripReferenceId = :dispatchTripReferenceId
    """)
    fun getDispatchTripReceipt(dispatchReferenceId:Int, dispatchTripReferenceId:Int): List<TicketReceiptWithNameModel>


    @Query("""
        SELECT 
            IFNULL(ticket_totals.totalRevenue, 0.00) AS totalRevenue,
            IFNULL(SUM(remittance.amount), 0.00) AS totalRemitted,
            IFNULL(ticket_totals.totalRevenue - IFNULL(SUM(remittance.amount), 0.00), 0.00) AS totalUnremitted
        FROM 
            (SELECT dispatchReferenceId, SUM(ticketTotalAmount) AS totalRevenue FROM ticket_receipts WHERE dispatchReferenceId = :dispatchReferenceId AND status = 0) AS ticket_totals
        LEFT JOIN 
            remittances AS remittance ON remittance.dispatchReferenceId = :dispatchReferenceId
        WHERE ticket_totals.dispatchReferenceId not in (SELECT dispatchReferenceId FROM ingresso)
    """)
    fun computeRevenueByDispatchId(dispatchReferenceId: Int): TicketRevenueModel


    // Create tem table for remaining passengers
    @Query(""" 
        SELECT
            receipt.id as receiptId,
            fromSegment.id as segmentId,
            receipt.fromSegmentId,
            receipt.toSegmentId,
            fromSegment.distanceKM AS fromKM,
            toSegment.distanceKM AS toKM,
            receipt.totalDistanceKM
        FROM ticket_receipts  AS receipt
        INNER JOIN route_segments AS fromSegment ON receipt.fromSegmentId = fromSegment.id
        INNER JOIN route_segments AS toSegment ON receipt.toSegmentId = toSegment.id
        WHERE receipt.dispatchReferenceId = :dispatchId
        AND receipt.dispatchTripReferenceId = :dispatchTripId AND receipt.isBaggage = 0
    """)
    fun getKMRemainingPassengerReference(dispatchId: Int,dispatchTripId:Int): List<RemainingPassengerModel>


    @Insert
    fun insertReceipt(receipt: TicketReceiptModel)

}



