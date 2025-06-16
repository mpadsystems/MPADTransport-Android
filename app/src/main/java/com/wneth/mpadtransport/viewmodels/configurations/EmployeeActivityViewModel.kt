package com.wneth.mpadtransport.viewmodels.configurations

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.wneth.mpadtransport.configurations.MPADTransportApp
import com.wneth.mpadtransport.models.RoleModel
import com.wneth.mpadtransport.models.UserModel
import com.wneth.mpadtransport.models.UserWithRole
import com.wneth.mpadtransport.viewmodels.BaseViewModel


class EmployeeActivityViewModel (application: Application): BaseViewModel(application){


    private lateinit var _employees: MutableLiveData<List<UserWithRole>>
    var employees: MutableLiveData<List<UserWithRole>>
        get() = _employees
        set(value) {
            _employees = value
        }


    init {
        (application as MPADTransportApp).getAppComponent().inject(this)
        employees = MutableLiveData()
        getEmployees()
    }

    private fun getEmployees() {
        val tmpEmployees = userRepository.getUsers()
        employees.postValue(tmpEmployees!!)
    }

    fun filterEmployees(keyword: String) {
        val tmpEmployees = userRepository.filterUsers(keyword)
        employees.postValue(tmpEmployees!!)
    }

    fun getEmployee(id:Int): UserWithRole? {
        val tmpEmployee = userRepository.getUser(id)
        return tmpEmployee
    }


    // ROLES
    fun getRoles(): List<RoleModel> {
        val tmpRoles = userRepository.getRoles()
        return tmpRoles
    }
}