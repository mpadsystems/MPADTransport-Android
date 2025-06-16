package com.wneth.mpadtransport.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wneth.mpadtransport.models.DispatchModel
import com.wneth.mpadtransport.models.DispatchTripModel
import com.wneth.mpadtransport.models.DispatchWithNameModel
import com.wneth.mpadtransport.models.RouteModel


@Dao
interface IDispatchRepository {

    /*@Query("SELECT * FROM dispatches")
    fun getDispatches(): List<DispatchModel>?*/

    @Query("""
        SELECT MAX(referenceId) + 1 FROM dispatches
    """)
    fun validateDispatchReferenceId(): Int


    @Query("""
        SELECT MAX(referenceId) + 1 FROM dispatch_trips
    """)
    fun validateDispatchTripReferenceId(): Int


    @Query("""
        SELECT 
            d.id AS dispatchId, 
            d.referenceId AS dispatchReferenceId,
            dt.referenceId AS dispatchTripReferenceId,
            d.companyId, 
            d.deviceName,
            dt.routeId,
            r.name AS routeName, 
            r.directionId,
            dt.terminalId, 
            t.name AS terminalName, 
            d.dispatcherId,
            u3.fullName AS dispatcherName,
            d.driverId, 
            u1.fullName AS driverName, 
            d.conductorId, 
            u2.fullName AS conductorName, 
            d.busId, 
            b.busNumber, 
            b.plateNumber AS busPlateNumber, 
            d.status, 
            d.dateCreated
        FROM dispatches d
        JOIN users u1 ON d.driverId = u1.id
        JOIN users u2 ON d.conductorId = u2.id
        JOIN users u3 ON d.dispatcherId = u3.id
        JOIN buses b ON d.busId = b.id
        JOIN dispatch_trips dt ON d.referenceId = dt.dispatchReferenceId
        JOIN routes r ON dt.routeId = r.id
        JOIN terminals t ON dt.terminalId = t.id
    """)
    fun getDispatches(): List<DispatchWithNameModel>?



    @Query("""
        SELECT 
            d.id AS dispatchId, 
            d.referenceId AS dispatchReferenceId,
            dt.referenceId AS dispatchTripReferenceId, 
            d.companyId, 
            d.deviceName,
            dt.routeId,
            r.name AS routeName, 
            dt.directionId,
            dt.terminalId, 
            t.name AS terminalName, 
            d.dispatcherId,
            u3.fullName AS dispatcherName,
            d.driverId, 
            u1.fullName AS driverName, 
            d.conductorId, 
            u2.fullName AS conductorName, 
            d.busId, 
            b.busNumber, 
            b.plateNumber AS busPlateNumber, 
            d.status, 
            d.dateCreated
        FROM dispatch_trips dt
        JOIN users u1 ON d.driverId = u1.id
        JOIN users u2 ON d.conductorId = u2.id
        JOIN users u3 ON d.dispatcherId = u3.id
        JOIN buses b ON d.busId = b.id
        JOIN dispatches d ON d.referenceId = dt.dispatchReferenceId
        JOIN routes r ON dt.routeId = r.id
        JOIN terminals t ON dt.terminalId = t.id
        WHERE dt.dispatchReferenceId = :referenceId
    """)
    fun getDispatchByReferenceId(referenceId:Int): DispatchWithNameModel?


    @Query("""
        SELECT 
            d.id AS dispatchId, 
            d.referenceId AS dispatchReferenceId,
            dt.referenceId AS dispatchTripReferenceId,
            d.companyId, 
            d.deviceName,
            dt.routeId,
            r.name AS routeName, 
            dt.directionId,
            dt.terminalId, 
            t.name AS terminalName, 
            d.dispatcherId,
            u3.fullName AS dispatcherName,
            d.driverId, 
            u1.fullName AS driverName, 
            d.conductorId, 
            u2.fullName AS conductorName, 
            d.busId, 
            b.busNumber, 
            b.plateNumber AS busPlateNumber, 
            d.status, 
            d.dateCreated
        FROM dispatches d
        JOIN users u1 ON d.driverId = u1.id
        JOIN users u2 ON d.conductorId = u2.id
        JOIN users u3 ON d.dispatcherId = u3.id
        JOIN buses b ON d.busId = b.id
        JOIN dispatch_trips dt ON d.referenceId = dt.dispatchReferenceId
        JOIN routes r ON dt.routeId = r.id
        JOIN terminals t ON dt.terminalId = t.id
        WHERE dt.id = (
            SELECT MAX(dt2.id)
            FROM dispatch_trips dt2
            WHERE dt2.routeId = dt.routeId AND dt2.directionId = dt.directionId
        )
        ORDER BY dt.id DESC
        LIMIT 1
    """)
    fun getLatestDispatchTrip(): DispatchWithNameModel?


    @Query("""
        SELECT * FROM routes 
        WHERE id = :routeId
        LIMIT 1
    """)
    fun getReversedRoute(routeId: Int): RouteModel?


    @Insert
    fun insertDispatch(dispatch: DispatchModel): Long


    @Insert
    fun insertDispatchTrip(dispatchTrip: DispatchTripModel): Long

}