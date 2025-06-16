package com.wneth.mpadtransport.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.wneth.mpadtransport.configurations.MPADTransportApp
import com.wneth.mpadtransport.models.BusModel
import com.wneth.mpadtransport.models.DispatchModel
import com.wneth.mpadtransport.models.DispatchTripModel
import com.wneth.mpadtransport.models.DispatchWithNameModel
import com.wneth.mpadtransport.models.RouteModel
import com.wneth.mpadtransport.models.TerminalModel
import com.wneth.mpadtransport.models.UserWithRole

class DispatchActivityViewModel (application: Application): BaseViewModel(application){




    private lateinit var _dispatches: MutableLiveData<List<DispatchWithNameModel>>
    var dispatches: MutableLiveData<List<DispatchWithNameModel>>
        get() = _dispatches
        set(value) {
            _dispatches = value
        }

    init {
        (application as MPADTransportApp).getAppComponent().inject(this)
        dispatches = MutableLiveData()
        getDispatches()
    }

    fun insertDispatch(dispatch: DispatchModel): Long {
        return dispatchRepository.insertDispatch(dispatch)
    }

    fun insertDispatchTrip(dispatchTrip: DispatchTripModel): Long {
        return dispatchRepository.insertDispatchTrip(dispatchTrip)
    }


    private fun getDispatches() {
        val tmpDispatches = dispatchRepository.getDispatches()
        _dispatches.postValue(tmpDispatches ?: emptyList())
    }


    fun getDispatchByReferenceId(dispatchReferenceId:Int): DispatchWithNameModel? {
        val tmpDispatch = dispatchRepository.getDispatchByReferenceId(dispatchReferenceId)
        return tmpDispatch
    }


    // DispatchModel Data
    fun getRoutes(searchKeyword:String = ""): List<RouteModel> {
        val tmpRoutes = routeRepository.getRoutes() ?: return emptyList()

        // Duplicate each southbound route (directionId = 1) as northbound (directionId = 2)
        val modifiedRoutes = tmpRoutes.flatMap { route ->
            if (route.directionId == 1) {
                listOf(
                    route,
                    route.copy(directionId = 2)
                )
            } else {
                listOf(route)
            }
        }

        // Apply filtering based on the search keyword if provided
        return if (searchKeyword.isEmpty()) {
            modifiedRoutes
        } else {
            modifiedRoutes.filter { route ->
                route.name.contains(searchKeyword, ignoreCase = true)
            }
        }
    }

    fun getTerminals(searchKeyword:String = ""): List<TerminalModel> {
        val tmpTerminals = terminalRepository.getTerminals() ?: return emptyList()
        if (searchKeyword.isEmpty()) {
            return tmpTerminals
        }
        return tmpTerminals.filter { terminal ->
            terminal.name.contains(searchKeyword, ignoreCase = true)
        }
    }

    fun getUsersByRole(roleId:Int,searchKeyword:String = ""): List<UserWithRole> {
        val tmpUsers = userRepository.getUsersByRole(roleId) ?: return emptyList()
        if (searchKeyword.isEmpty()) {
            return tmpUsers
        }
        return tmpUsers.filter { users ->
            users.fullName.contains(searchKeyword, ignoreCase = true)
        }
    }

    fun getBuses(searchKeyword:String = ""): List<BusModel> {
        val tmpBuses = busRepository.getBuses() ?: return emptyList()
        if (searchKeyword.isEmpty()) {
            return tmpBuses
        }
        return tmpBuses.filter { buses ->
            buses.plateNumber.contains(searchKeyword, ignoreCase = true) || buses.plateNumber.contains(searchKeyword, ignoreCase = true)
        }
    }


    fun getLatestDispatchTrip(): DispatchWithNameModel? {
        return dispatchRepository.getLatestDispatchTrip()
    }


    fun getReversedRoute(routeId: Int, directionId: Int): RouteModel? {
        val routeDirection = if (directionId == 1) 2 else 1
        val route = dispatchRepository.getReversedRoute(routeId)
        return route?.copy(directionId = routeDirection)
    }


}