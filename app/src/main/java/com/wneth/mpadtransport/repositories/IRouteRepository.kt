package com.wneth.mpadtransport.repositories

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.wneth.mpadtransport.models.BusModel
import com.wneth.mpadtransport.models.RouteModel
import com.wneth.mpadtransport.models.RouteSegmentModel


@Dao
interface IRouteRepository {

    @Query("SELECT * FROM routes WHERE isActive = 1")
    fun getRoutes(): List<RouteModel>?

    @Transaction
    @Query("SELECT * FROM routes WHERE isActive = 1")
    fun getRoutesWithSegments(): List<RouteWithSegments>?

    @Query("SELECT * FROM routes WHERE id = :id AND isActive = 1")
    fun getRoute(id:Int): RouteWithSegments?

    @Query("SELECT * FROM routes WHERE isActive = 1 AND name LIKE '%'||:keyword||'%'")
    fun filterRoutes(keyword:String): List<RouteWithSegments>?

    @Insert
    fun insertRoute(route: RouteModel)

    @Insert
    fun insertRouteBulk(routes: List<RouteModel>): LongArray

    @Delete
    fun deleteRoute(route: RouteModel)



    // RouteSegments
    @Query("SELECT * FROM route_segments WHERE routeId = :routeId AND distanceKM = :km")
    fun getRouteSegmentByRouteIdAndKM(routeId: Int, km: Int): RouteSegmentModel?

    @Query("SELECT * FROM route_segments")
    fun getRouteSegments(): List<RouteSegmentModel>?

    @Query("SELECT * FROM route_segments WHERE routeId = :routeId")
    fun getRouteSegmentsByRouteId(routeId: Int): List<RouteSegmentModel>?

    @Query("SELECT * FROM route_segments WHERE id = :id")
    fun getRouteSegment(id:Int): RouteSegmentModel?

    @Query("SELECT * FROM route_segments WHERE routeId = :routeId AND name LIKE '%'||:keyword||'%'")
    fun filterRouteSegmentsByRouteId(routeId: Int, keyword:String): List<RouteSegmentModel>?

    @Query("SELECT * FROM route_segments WHERE name LIKE '%'||:keyword||'%'")
    fun filterRouteSegments(keyword:String): List<RouteSegmentModel>?

    @Insert
    fun insertSegment(routeSegment: RouteSegmentModel)

    @Insert
    fun insertSegmentBulk(routeSegments: List<RouteSegmentModel>): LongArray

    @Delete
    fun deleteRouteSegment(routeSegment: RouteSegmentModel)

    @Query("DELETE FROM routes")
    fun deleteAllRoutes()

    @Query("DELETE FROM sqlite_sequence WHERE name='routes'")
    fun resetAutoIncrementRoutes()

    @Query("DELETE FROM route_segments")
    fun deleteAllRouteSegments()

    @Query("DELETE FROM sqlite_sequence WHERE name='route_segments'")
    fun resetAutoIncrementRouteSegments()
}


data class RouteWithSegments(
    @Embedded val route: RouteModel,
    @Relation(
        parentColumn = "id",
        entityColumn = "routeId"
    )
    val segments: List<RouteSegmentModel>
)