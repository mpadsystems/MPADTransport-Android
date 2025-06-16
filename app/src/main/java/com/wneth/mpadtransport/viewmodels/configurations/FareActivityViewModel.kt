package com.wneth.mpadtransport.viewmodels.configurations

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.wneth.mpadtransport.configurations.MPADTransportApp
import com.wneth.mpadtransport.models.FareModel
import com.wneth.mpadtransport.models.FareSetupModel
import com.wneth.mpadtransport.viewmodels.BaseViewModel


class FareActivityViewModel(application: Application): BaseViewModel(application){

    private lateinit var _fare: MutableLiveData<FareModel>
    var fare: MutableLiveData<FareModel>
        get() = _fare
        set(value) {
            _fare = value
        }

    private lateinit var _fareSetups: MutableLiveData<List<FareSetupModel>>
    var fareSetups: MutableLiveData<List<FareSetupModel>>
        get() = _fareSetups
        set(value) {
            _fareSetups = value
        }


    init {
        (application as MPADTransportApp).getAppComponent().inject(this)
        _fare = MutableLiveData()
        _fareSetups = MutableLiveData()
        getFare()
        getFareSetups()
    }

    private fun getFare() {
        val tmpFare = fareRepository.getFare()
        fare.postValue(tmpFare!!)
    }

    private fun getFareSetups() {
        val tmpFareSetups = fareRepository.getFareSetups()
        _fareSetups.postValue(tmpFareSetups!!)
    }

}