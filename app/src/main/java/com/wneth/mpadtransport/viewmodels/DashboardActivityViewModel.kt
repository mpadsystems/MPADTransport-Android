package com.wneth.mpadtransport.viewmodels

import android.app.Application
import com.wneth.mpadtransport.configurations.MPADTransportApp

class DashboardActivityViewModel (application: Application): BaseViewModel(application){

    init {
        (application as MPADTransportApp).getAppComponent().inject(this)
    }

    fun hasDispatchData(): Boolean {
        val hasDispatch = sharedPrefs.getBoolean("sharedHasDispatch",false)
        return hasDispatch
    }

    fun getUnsyncedIngressoCount(): Int{
        return ingressoRepository.getUsyncedIngressoCount()
    }

}