package com.wneth.mpadtransport.viewmodels.configurations

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.wneth.mpadtransport.configurations.MPADTransportApp
import com.wneth.mpadtransport.models.RouteModel
import com.wneth.mpadtransport.models.RouteSegmentModel
import com.wneth.mpadtransport.repositories.RouteWithSegments
import com.wneth.mpadtransport.viewmodels.BaseViewModel

class RouteActivityViewModel(application: Application): BaseViewModel(application) {

    private lateinit var _routes: MutableLiveData<List<RouteWithSegments>>
    var routes: MutableLiveData<List<RouteWithSegments>>
        get() = _routes
        set(value) {
            _routes = value
        }


    private lateinit var _routeSegments: MutableLiveData<List<RouteSegmentModel>>
    var routeSegments: MutableLiveData<List<RouteSegmentModel>>
        get() = _routeSegments
        set(value) {
            _routeSegments = value
        }

    init {
        (application as MPADTransportApp).getAppComponent().inject(this)
        routes = MutableLiveData()
        routeSegments = MutableLiveData()
        getRoutes()
    }

    // Routes
    private fun getRoutes() {
        val tmpRoutes = routeRepository.getRoutesWithSegments()
        routes.postValue(tmpRoutes!!)
    }

    fun filterRoutes(keyword: String) {
        val tmpRoutes = routeRepository.filterRoutes(keyword)
        routes.postValue(tmpRoutes!!)
    }

    fun getRoute(id: Int): RouteWithSegments? {
        val tmpRoute = routeRepository.getRoute(id)
        return tmpRoute
    }

    fun deleteRoute(route: RouteModel) {
        routeRepository.deleteRoute(route)
        getRoutes()
    }


    // RouteSegments
    private fun getRouteSegments() {
        val tmpRouteSegments = routeRepository.getRouteSegments()
        routeSegments.postValue(tmpRouteSegments!!)
    }

    fun getRouteSegmentsByRouteId(routeId: Int, searchKeyword: String = ""){
        var tmpRouteSegments = routeRepository.getRouteSegmentsByRouteId(routeId) ?: emptyList()
        if (searchKeyword.isEmpty()) {
            routeSegments.postValue(tmpRouteSegments)
        }
        tmpRouteSegments = tmpRouteSegments.filter { segment ->
            segment.name.contains(searchKeyword, ignoreCase = true) ||
                    segment.distanceKM.toString().contains(searchKeyword, ignoreCase = true)
        }
        routeSegments.postValue(tmpRouteSegments)
    }

    fun filterRouteSegmentsByRouteId(routeId: Int, keyword: String){
        val tmpRouteSegments = routeRepository.filterRouteSegmentsByRouteId(routeId, keyword)
        routeSegments.postValue(tmpRouteSegments!!)
    }

    fun filterRouteSegments(keyword: String) {
        val tmpRouteSegments = routeRepository.filterRouteSegments(keyword)
        routeSegments.postValue(tmpRouteSegments!!)
    }

    fun deleteRouteSegment(routeSegment: RouteSegmentModel) {
        routeRepository.deleteRouteSegment(routeSegment)
        getRouteSegments()
    }


}