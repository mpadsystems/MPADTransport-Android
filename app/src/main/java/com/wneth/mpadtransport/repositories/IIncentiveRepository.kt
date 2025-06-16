package com.wneth.mpadtransport.repositories

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.wneth.mpadtransport.models.IncentiveModel
import com.wneth.mpadtransport.models.IncentiveWithRoleModel


@Dao
interface IIncentiveRepository {

    @Query("""
        SELECT incentive.*, role.name AS roleName  
        FROM incentives as incentive 
        INNER JOIN roles as role ON incentive.roleId = role.id
        WHERE incentive.isActive = 1
        """)
    fun getIncentives(): List<IncentiveWithRoleModel>?


    @Query("""
        SELECT incentive.*, role.name AS roleName 
        FROM incentives as incentive 
        INNER JOIN roles as role ON incentive.roleId = role.id 
        WHERE incentive.id = :id AND incentive.isActive = 1
        """)
    fun getIncentiveById(id:Int): IncentiveWithRoleModel?

    @Query("""
        SELECT incentive.*, role.name AS roleName 
        FROM incentives as incentive 
        INNER JOIN roles as role ON incentive.roleId = role.id 
        WHERE incentive.roleId = :roleId AND incentive.isActive = 1
    """)
    fun getIncentivesByRoleId(roleId:Int): List<IncentiveWithRoleModel>?

    /*@Query("SELECT * FROM terminals WHERE name LIKE '%'||:keyword||'%' OR address LIKE '%'||:keyword||'%'")
    fun filterTerminals(keyword:String): List<TerminalModel>?*/

    @Insert
    fun insert(incentive: IncentiveModel)

    @Insert
    fun insertBulk(incentive: List<IncentiveModel>): LongArray

    @Delete
    fun delete(incentive: IncentiveModel)

    @Query("DELETE FROM incentives")
    fun deleteAll()

    @Query("DELETE FROM sqlite_sequence WHERE name='incentives'")
    fun resetAutoIncrement()
}