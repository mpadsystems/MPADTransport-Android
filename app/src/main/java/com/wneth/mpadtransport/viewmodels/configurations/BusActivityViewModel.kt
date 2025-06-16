package com.wneth.mpadtransport.viewmodels.configurations

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.wneth.mpadtransport.configurations.MPADTransportApp
import com.wneth.mpadtransport.models.BusModel
import com.wneth.mpadtransport.viewmodels.BaseViewModel


class BusActivityViewModel(application: Application): BaseViewModel(application){

    private lateinit var _buses: MutableLiveData<List<BusModel>>
    var buses: MutableLiveData<List<BusModel>>
        get() = _buses
        set(value) {
            _buses = value
        }


    init {
        (application as MPADTransportApp).getAppComponent().inject(this)
        buses = MutableLiveData()
        getBuses()
    }

    private fun getBuses() {
        val tmpBuses = busRepository.getBuses()
        buses.postValue(tmpBuses!!)
    }

    fun filterBuses(keyword: String) {
        val tmpBuses = busRepository.filterBuses(keyword)
        buses.postValue(tmpBuses!!)
    }

    fun getBus(id:Int): BusModel? {
        val tmpBus = busRepository.getBus(id)
        return tmpBus
    }

    fun deleteBus(bus: BusModel){
        busRepository.delete(bus)
        getBuses()
    }
}