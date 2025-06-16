package com.wneth.mpadtransport.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wneth.mpadtransport.models.DispatchModel
import com.wneth.mpadtransport.models.DispatchTripModel
import com.wneth.mpadtransport.models.IngressoModel
import com.wneth.mpadtransport.models.IngressoDeductionModel
import com.wneth.mpadtransport.models.IngressoDeductionWithNameModel
import com.wneth.mpadtransport.models.InspectionModel
import com.wneth.mpadtransport.models.RemittanceModel
import com.wneth.mpadtransport.models.TicketReceiptModel


@Dao
interface IIngressoRepository {


    @Query("""
        SELECT MAX(referenceId) + 1 FROM ingresso
    """)
    fun validateIngressoReferenceId(): Int

    @Insert
    fun insertIngresso(ingresso: IngressoModel): Long

    @Insert
    fun insertIngressoDeductions(deductions: List<IngressoDeductionModel>): LongArray


    @Query("SELECT count(id) FROM ingresso WHERE status == 0")
    fun getUsyncedIngressoCount(): Int


    @Query("""
        SELECT
            ingressoDeduction.id,
            ingressoDeduction.ingressoReferenceId,
            deduction.id AS deductionId,
            deduction.name AS deductionName,
            deduction.deductionType,
            ingressoDeduction.amount,
            deduction.autoCompute,
            deduction.computeAfter
        FROM  ingresso_deductions AS ingressoDeduction
        INNER JOIN deductions AS deduction ON ingressoDeduction.deductionId = deduction.id
        WHERE ingressoDeduction.ingressoReferenceId = :referenceId
    """)
    fun getIngressoDeductionByReferenceId(referenceId:Int): List<IngressoDeductionWithNameModel>


    // Sync IngressoModel
    @Query("SELECT * FROM ingresso WHERE status == 0 ORDER BY id ASC LIMIT 3")
    fun getUnsyncedIngresso(): List<IngressoModel>


    @Query("SELECT * FROM ingresso_deductions WHERE ingressoReferenceId IN (:ingressoIds) ORDER BY id ASC LIMIT 3")
    fun getUnsyncedIngressoDeductions(ingressoIds:List<Int>): List<IngressoDeductionModel>


    @Query("SELECT * FROM ticket_receipts WHERE status == 0 AND  dispatchReferenceId IN (:dispatchIds) ORDER BY id ASC LIMIT 3")
    fun getUnsyncedDispatchTickets(dispatchIds:List<Int>): List<TicketReceiptModel>


    @Query("SELECT * FROM dispatches WHERE status == 0 AND  referenceId IN (:dispatchIds) ORDER BY id ASC LIMIT 3")
    fun getUnsyncedIngressoDispatches(dispatchIds:List<Int>): List<DispatchModel>


    @Query("SELECT * FROM dispatch_trips WHERE dispatchReferenceId IN (:dispatchIds) ORDER BY id ASC LIMIT 3")
    fun getUnsyncedIngressoDispatchTrips(dispatchIds:List<Int>): List<DispatchTripModel>


    @Query("SELECT * FROM remittances WHERE dispatchReferenceId IN (:dispatchIds) ORDER BY id ASC LIMIT 3")
    fun getUnsyncedIngressoRemittances(dispatchIds:List<Int>): List<RemittanceModel>


    @Query("SELECT * FROM inspections WHERE dispatchReferenceId IN (:dispatchIds) ORDER BY id ASC LIMIT 3")
    fun getUnsyncedInspections(dispatchIds:List<Int>): List<InspectionModel>


    // Update Status
    @Query("UPDATE ingresso SET status = 1 WHERE referenceId IN (:ingressoIds)")
    fun updateIngressoStatus(ingressoIds: List<Int>)


    @Query("UPDATE dispatches SET status = 1 WHERE referenceId IN (:dispatchIds)")
    fun updateDispatchesStatus(dispatchIds: List<Int>)


    @Query("UPDATE ticket_receipts SET status = 1 WHERE dispatchReferenceId IN (:dispatchIds)")
    fun updateReceiptsStatus(dispatchIds: List<Int>)
}