package com.wneth.mpadtransport.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wneth.mpadtransport.models.DispatchInspectionTicketModel
import com.wneth.mpadtransport.models.InspectionModel


@Dao
interface IInspectionRepository {

    /*@Query("SELECT * FROM buses")
    fun getBuses(): List<BusModel>?

    @Query("SELECT * FROM buses WHERE id = :id")
    fun getBus(id:Int): BusModel?

    @Query("SELECT * FROM buses WHERE busNumber LIKE '%'||:keyword||'%' OR plateNumber LIKE '%'||:keyword||'%'")
    fun filterBuses(keyword:String): List<BusModel>?*/

    @Insert
    fun insert(inspection: InspectionModel): Long

    @Query("""
        SELECT 
            dt.referenceId AS dispatchId, 
            tc.dispatchTripReferenceId AS tripId,
            tc.referenceId AS ticketId,
            dt.directionId AS directionId
        FROM dispatch_trips AS dt 
        INNER JOIN ticket_receipts AS tc 
            ON tc.dispatchReferenceId = dt.referenceId 
        INNER JOIN routes AS r 
            ON r.id = dt.routeId 
        WHERE dt.referenceId = :dispatchId
        ORDER BY tc.referenceId ASC
    """)
    fun getDispatchInspectionHeader(dispatchId: Int): List<DispatchInspectionTicketModel>

    /*@Insert
    fun insertBulk(buses: List<BusModel>): LongArray

    @Delete
    fun delete(bus: BusModel)

    @Query("DELETE FROM buses")
    fun deleteAll()

    @Query("DELETE FROM sqlite_sequence WHERE name='buses'")
    fun resetAutoIncrement()*/
}