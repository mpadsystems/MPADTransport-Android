package com.wneth.mpadtransport.repositories

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.wneth.mpadtransport.models.BusModel
import com.wneth.mpadtransport.models.FareSetupModel
import com.wneth.mpadtransport.models.HotspotModel
import com.wneth.mpadtransport.models.HotspotWithNameModel


@Dao
interface IHotspotRepository {

    @Query("""
        -- Subquery to handle distinct concatenation for fromSegmentNames
        WITH from_segments_aggregated AS (
            SELECT
                id,
                name AS fromSegmentName
            FROM (
                SELECT DISTINCT
                    hotspot.id,
                    fromSegment.name
                FROM hotspots AS hotspot
                LEFT JOIN route_segments AS fromSegment
                    ON hotspot.fromSegmentId = fromSegment.id
                WHERE hotspot.isActive = 1
            ) AS subquery
            GROUP BY id
        ),
        
        -- Subquery to handle distinct concatenation for toSegmentNames
        to_segments_aggregated AS (
            SELECT
                id,
                name AS toSegmentName
            FROM (
                SELECT DISTINCT
                    hotspot.id,
                    toSegment.name
                FROM hotspots AS hotspot
                LEFT JOIN route_segments AS toSegment
                    ON hotspot.toSegmentId = toSegment.id
                WHERE hotspot.isActive = 1
            ) AS subquery
            GROUP BY id
        )
        
        -- Main query combining all results
        SELECT
            hotspot.id,
            hotspot.companyId,
            hotspot.routeId,
            route.name AS routeName,
            hotspot.fromSegmentId,
            fsa.fromSegmentName,
            hotspot.toSegmentId,
            tsa.toSegmentName,
            hotspot.amount,
            hotspot.customDiscountedAmount,
            hotspot.isActive,
            hotspot.dateCreated
        FROM hotspots AS hotspot
        INNER JOIN routes AS route
            ON route.id = hotspot.routeId
        LEFT JOIN from_segments_aggregated AS fsa
            ON fsa.id = hotspot.id
        LEFT JOIN to_segments_aggregated AS tsa
            ON tsa.id = hotspot.id
        WHERE hotspot.isActive = 1
        GROUP BY
            hotspot.id,
            hotspot.companyId,
            hotspot.routeId,
            route.name,
            hotspot.fromSegmentId,
            hotspot.toSegmentId,
            hotspot.amount,
            hotspot.customDiscountedAmount,
            hotspot.isActive,
            hotspot.dateCreated
    """)
    fun getHotspots(): List<HotspotWithNameModel>?


    @Query("""
        SELECT 
            hotspot.id,
            hotspot.companyId,
            hotspot.routeId,
            route.name AS routeName,
            hotspot.fromSegmentId AS fromSegmentId,
            fromSegment.name AS fromSegmentName,
            hotspot.toSegmentId AS toSegmentId,
            toSegment.name AS toSegmentName,
            hotspot.amount,
            hotspot.customDiscountedAmount,
            hotspot.customDiscountedAmount,
            hotspot.isActive,
            hotspot.dateCreated
        FROM hotspots AS hotspot 
        INNER JOIN routes AS route ON route.id = hotspot.routeId
        INNER JOIN route_segments AS fromSegment ON fromSegment.id = hotspot.fromSegmentId
        INNER JOIN route_segments AS toSegment  ON toSegment.id = hotspot.toSegmentId
        WHERE hotspot.routeId = :routeId AND hotspot.fromSegmentId = :fromSegmentId AND hotspot.toSegmentId = :toSegmentId AND hotspot.isActive = 1;
    """)
    fun getSegmentHotspotAmount(routeId:Int, fromSegmentId:Int, toSegmentId:Int): HotspotWithNameModel?


    @Query("""
            SELECT 
                hotspot.id,
                hotspot.companyId,
                hotspot.routeId,
                route.name AS routeName,
                hotspot.fromSegmentId AS fromSegmentId,
                fromSegment.name AS fromSegmentName,
                hotspot.toSegmentId AS toSegmentId,
                toSegment.name AS toSegmentName,
                hotspot.amount,
                hotspot.customDiscountedAmount,
                hotspot.isActive,
                hotspot.dateCreated
            FROM hotspots AS hotspot 
            INNER JOIN routes AS route ON route.id = hotspot.routeId
            INNER JOIN route_segments AS fromSegment ON fromSegment.id = hotspot.fromSegmentId
            INNER JOIN route_segments AS toSegment  ON toSegment.id = hotspot.toSegmentId
            WHERE hotspot.isActive = 1 AND route.name LIKE '%'||:keyword||'%' OR fromSegment.name LIKE '%'||:keyword||'%' OR toSegment.name LIKE '%'||:keyword||'%'
    """)
    fun filterHotspots(keyword:String): List<HotspotWithNameModel>?

    @Insert
    fun insert(hotspot: HotspotModel)

    @Insert
    fun insertBulk(hotspots: List<HotspotModel>): LongArray

    @Delete
    fun delete(hotspot: HotspotModel)

    @Query("DELETE FROM hotspots")
    fun deleteAll()

    @Query("DELETE FROM sqlite_sequence WHERE name='hotspots'")
    fun resetAutoIncrement()
}