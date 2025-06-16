package com.wneth.mpadtransport.repositories

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.wneth.mpadtransport.models.RemittanceModel
import com.wneth.mpadtransport.models.RemittanceWithNameModel
import com.wneth.mpadtransport.models.TicketRevenueModel


@Dao
interface IRemittanceRepository {

    @Query("""
        select
            remittance.id AS id,
            remittance.dispatchReferenceId AS dispatchReferenceId,
            remittance.terminalId AS terminalId,
            terminal.name AS terminalName,
            remittance.remittedById AS remittedById,
            userRemitted.fullName AS remittedByName,
            remittance.receivedById AS receivedById,
            userReceived.fullName AS receivedByName,
            signature AS signature,
            amount AS remittedAmount,
            remittance.dateCreated
        from remittances as remittance
        INNER JOIN terminals AS terminal ON remittance.terminalId = terminal.id
        INNER JOIN users AS userRemitted ON userRemitted.id = remittance.remittedById
        INNER JOIN users AS userReceived ON userReceived.id = remittance.receivedById
        WHERE dispatchReferenceId = :dispatchId
    """)
    fun getRemittancesByDispatchId(dispatchId: Int): List<RemittanceWithNameModel>?


    @Query("SELECT * FROM remittances WHERE id = :id")
    fun getRemittance(id:Int): RemittanceModel?

    @Insert
    fun insert(remittance: RemittanceModel)

    @Delete
    fun delete(remittance: RemittanceModel)

    @Query("DELETE FROM terminals")
    fun deleteAll()

    @Query("DELETE FROM sqlite_sequence WHERE name='terminals'")
    fun resetAutoIncrement()
}