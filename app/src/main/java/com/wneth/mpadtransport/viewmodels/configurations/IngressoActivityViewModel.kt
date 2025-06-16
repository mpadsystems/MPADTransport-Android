package com.wneth.mpadtransport.viewmodels.configurations

import android.app.Application
import com.wneth.mpadtransport.configurations.MPADTransportApp
import com.wneth.mpadtransport.models.DeductionModel
import com.wneth.mpadtransport.models.IncentiveWithRoleModel
import com.wneth.mpadtransport.models.IngressoModel
import com.wneth.mpadtransport.models.IngressoDeductionModel
import com.wneth.mpadtransport.models.IngressoDeductionWithNameModel
import com.wneth.mpadtransport.models.TicketRevenueModel
import com.wneth.mpadtransport.models.UserWithRole
import com.wneth.mpadtransport.viewmodels.BaseViewModel
import java.util.UUID

class IngressoActivityViewModel(application: Application): BaseViewModel(application) {


    init {
        (application as MPADTransportApp).getAppComponent().inject(this)
    }

    fun getTotalRevenueByDispatchId(dispatchReferenceId: Int): TicketRevenueModel {
        val revenue = ticketReceiptRepository.computeRevenueByDispatchId(dispatchReferenceId)
        return revenue
    }



    fun getAutoComputeExpenses(withThreshold: Boolean, amount: Double = 0.00): List<IngressoDeductionWithNameModel> {

        val expenses:List<DeductionModel>

        if (!withThreshold){
            expenses = deductionRepository.getAutoComputeExpensesNoThreshold() ?: return emptyList()
            return expenses.map { expense ->
                IngressoDeductionWithNameModel(
                    id = UUID.randomUUID().leastSignificantBits,
                    ingressoReferenceId = 0,
                    deductionId = expense.id,
                    deductionName = expense.name,
                    deductionType = expense.deductionType,
                    amount =  expense.fraction,
                    autoCompute = true,
                    computeAfter = expense.computeAfter
                )
            }
        }else{
            expenses = deductionRepository.getAutoComputeExpensesWithThreshold() ?: return emptyList()
            return expenses.map { expense ->
                val totalAmount = if(amount in expense.thresholdRangeA..expense.thresholdRangeB){
                    (expense.fraction / 100) * amount as Double
                }else{
                    0.00
                }
                IngressoDeductionWithNameModel(
                    id = UUID.randomUUID().leastSignificantBits,
                    ingressoReferenceId = 0,
                    deductionId = expense.id,
                    deductionName = expense.name,
                    deductionType = expense.deductionType,
                    amount = totalAmount,
                    autoCompute = true,
                    computeAfter = expense.computeAfter
                )
            }.filter { deduction ->
                deduction.amount != 0.00
            }
        }
    }

    fun getUserInfoById(userId: Int): UserWithRole? {
        val user = userRepository.getUserInfoById(userId)
        return user
    }

    fun getIncentives(): List<IncentiveWithRoleModel>?{
        return incentiveRepository.getIncentives()
    }

    private fun getIncentivesByRoleId(roleId: Int): List<IncentiveWithRoleModel>?{
        return incentiveRepository.getIncentivesByRoleId(roleId)
    }

    fun getDeductionExpenses(): List<DeductionModel>?{
        return deductionRepository.getDeductionExpenses()
    }

    fun getDeductionWithHoldings(): List<DeductionModel>?{
        return deductionRepository.getDeductionWithHoldings()
    }

    fun getIngressoDeductionByReferenceId(referenceId: Int): List<IngressoDeductionWithNameModel>{
        val deductions = ingressoRepository.getIngressoDeductionByReferenceId(referenceId)
        return deductions
    }

    fun insertFinalIngresso(ingresso: IngressoModel, deductions: List<IngressoDeductionModel>){
        val ingressoId = ingressoRepository.insertIngresso(ingresso)

        val updatedDeductions = deductions.map { deduction ->
            deduction.copy(ingressoReferenceId = ingresso.referenceId)
        }
        ingressoRepository.insertIngressoDeductions(updatedDeductions)

        if (ingressoId > 0){
            sharedPrefs.setInt("sharedTerminalId",0)
            sharedPrefs.setBoolean("sharedHasDispatch",false)
            sharedPrefs.setInt("sharedDispatcherId", 0)
            sharedPrefs.setInt("sharedConductorId", 0)
            sharedPrefs.setInt("sharedDriverId", 0)
        }
    }

}