package com.wneth.mpadtransport.viewmodels.configurations

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.wneth.mpadtransport.configurations.MPADTransportApp
import com.wneth.mpadtransport.models.HotspotWithNameModel
import com.wneth.mpadtransport.viewmodels.BaseViewModel


class HotspotActivityViewModel(application: Application): BaseViewModel(application){

    private lateinit var _hotspots: MutableLiveData<List<HotspotWithNameModel>>
    var hotspots: MutableLiveData<List<HotspotWithNameModel>>
        get() = _hotspots
        set(value) {
            _hotspots = value
        }




    init {
        (application as MPADTransportApp).getAppComponent().inject(this)
        _hotspots = MutableLiveData()
        getHotspots()
    }


    private fun getHotspots() {
        val tmpHotspots = hotspotRepository.getHotspots()
        _hotspots.postValue(tmpHotspots!!)
    }

    fun filterHotspots(keyword: String) {
        val tmpHotspots = hotspotRepository.getHotspots()
        val filtered = tmpHotspots?.filter {
            it.routeName.contains(keyword, ignoreCase = true) ||
                    it.fromSegmentName.contains(keyword, ignoreCase = true) ||
                    it.toSegmentName.contains(keyword, ignoreCase = true)
        }
        _hotspots.postValue(filtered!!)
    }




}