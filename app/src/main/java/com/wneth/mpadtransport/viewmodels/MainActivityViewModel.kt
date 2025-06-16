package com.wneth.mpadtransport.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import com.wneth.mpadtransport.models.UserModel
import com.wneth.mpadtransport.utilities.apiUrl
import com.wneth.mpadtransport.utilities.convertToModel
import com.wneth.mpadtransport.utilities.mpadUrl

import org.json.JSONObject
import java.time.Duration
import java.time.Instant
class MainActivityViewModel (application: Application): BaseViewModel(application){



    fun loginWithPIN(pin: String, moduleId: Int): Any {
        try {
            val roleIds = getAppModuleAuthorizedRoles(moduleId).split(",").map { it.trim() }
            if(setupCompleted()){

                val response = if (moduleId == 3){
                    accountRepository.loginWithPINAndUserId(pin, roleIds, sharedPrefs.getInt("sharedConductorId",0))
                }else{
                    accountRepository.loginWithPIN(pin, roleIds)
                }
                return if (response != null) {
                    if(moduleId == 0){
                        setAuthUser(response)
                        return true
                    }else{
                        response
                    }
                }else{
                    false
                }
            }
        }catch (e: Exception){
            Log.e("Error", e.message.toString())
        }
        return false;
    }


    suspend fun getMPADMessage():String{
        try {
            val response = api.getInsecure(mpadUrl);
            val responseTxt = response.getString("AndroidCommand");
            sharedPrefs.setString("sharedMPADMessage", responseTxt)
            return responseTxt
        }
        catch (ex: Exception){
            return ""
        }
    }

    suspend fun login(username: String, password: String): Boolean {
        var loginResult = false
        try {
            val storedLastAccess = sharedPrefs.getString("sharedLastDeviceAccess", "")
            if (storedLastAccess.isNotEmpty()) {
                if (tokenHasExpired()) {
                    loginResult = apiLogin(username, password)
                }else{
                    if(setupCompleted()){
                        val response = accountRepository.login(username, password)
                        if (response != null) {
                            setAuthUser(response)
                            loginResult = true
                        }else{
                            loginResult = false
                        }
                    }else{
                        loginResult = apiLogin(username, password)
                    }
                }
            } else {
                if(setupCompleted()){
                    val response = accountRepository.login(username, password)
                    if (response != null) {
                        setAuthUser(response)
                        loginResult = true
                    }else{
                        loginResult = false
                    }
                }else{
                    loginResult = apiLogin(username, password)
                }
            }
        }catch (e: Exception){
            Log.e("Error", e.message.toString())
        }

        sharedPrefs.setString("sharedUsername", username)
        sharedPrefs.setString("sharedPassword", password)

        return loginResult
    }

    private suspend fun apiLogin(username: String, password: String): Boolean {
        val credentials = mapOf(
            "username" to username,
            "password" to password
        )
        sharedApiToken = sharedPrefs.getString("sharedApiToken", "")
        val response = api.post("${apiUrl}/Auth/Login",credentials, sharedApiToken)

        if(response.status == 401){
            return false
        }else{
            sharedPrefs.setString("sharedApiToken", response.data["Token"].toString())
            sharedPrefs.setString("sharedLastDeviceAccess", Instant.now().toString())
            val tmpUser = response.data["User"] as JSONObject
            val user: UserModel = convertToModel(tmpUser)

            setAuthUser(user)
            return true
        }
    }


    fun tokenHasExpired(): Boolean {
        val storedLastAccess = sharedPrefs.getString("sharedLastDeviceAccess", "") ?: return true
        return try {
            val storedDate = Instant.parse(storedLastAccess)
            val now = Instant.now()
            val difference = Duration.between(storedDate, now).toDays()
            difference >= 3
        } catch (e: Exception) {
            true
        }
    }

    private fun setAuthUser(user: UserModel){
        sharedPrefs.setInt("sharedInitialUserId", user.id)
        sharedPrefs.setInt("sharedCompanyId", user.companyId)
        sharedPrefs.setInt("sharedInitialUserRoleId", user.roleId)
        sharedPrefs.setString("sharedInitialUserFullName", user.fullName)
    }

    /*
    * MODULES
    * 0. Main Login Module
    * 1. DispatchModel
    * 2. RemittanceModel
    * 3. Ticketing
    * 4. Reverse Trip
    * 5. Inspection
    * 6. Trip Report
    * 7. Configuration
    * 8. MPADPay Registration
    * 100. Exit Dashboard
    *
    * ROLES
    * 1. SuperUser
    * 2. Administrator
    * 3. Cashier
    * 4. Inspector
    * 5. Dispatcher
    * 6. Conductor
    * 7. Driver
    */

    private fun getAppModuleAuthorizedRoles(module: Int): String {
        return when (module) {
            0 -> "1,2,3,4,5,6,7" // RoleModel: All
            1 -> "5,3,2,1" // RoleModel: Dispatcher / Administrator / Cashier / Superuser
            2 -> "3,2,1"
            3 -> "7,6,2,1"
            4 -> "4,5,2,1"
            5 -> "4,2,1"
            6 -> "6,5,4,3,2,1"
            7 -> "3,2,1"
            8 -> "1,2,3,5,6"
            100 -> "1,2,3,5"
            else -> "Unknown" // Handle unexpected values (optional)
        }
    }


}