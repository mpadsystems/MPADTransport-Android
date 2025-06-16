package com.wneth.mpadtransport.viewmodels.configurations

import android.app.Application
import com.wneth.mpadtransport.configurations.MPADTransportApp
import com.wneth.mpadtransport.models.IncentiveWithRoleModel
import com.wneth.mpadtransport.viewmodels.BaseViewModel

class IncentiveActivityViewModel (application: Application): BaseViewModel(application){

    init {
        (application as MPADTransportApp).getAppComponent().inject(this)
    }

    fun getCommissions(): List<IncentiveWithRoleModel>? {
        val tmpIncentives = incentiveRepository.getIncentives()
        return tmpIncentives
    }

    /*fun filterTerminals(keyword: String) {
        val tmpTerminals = terminalRepository.filterTerminals(keyword)
        terminals.postValue(tmpTerminals!!)
    }*/

    fun getIncentiveById(id:Int): IncentiveWithRoleModel? {
        val tmpIncentive = incentiveRepository.getIncentiveById(id)
        return tmpIncentive
    }

}