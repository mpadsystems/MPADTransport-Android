package com.wneth.mpadtransport.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wneth.mpadtransport.utilities.apiUrl
import com.wneth.mpadtransport.utilities.convertToListModel
import com.wneth.mpadtransport.utilities.convertToModel
import com.wneth.mpadtransport.utilities.validatedDeviceName
import org.json.JSONArray
import org.json.JSONObject

class InitialSetupActivityViewModel(application: Application) : BaseViewModel(application) {

    private val syncStates = mapOf(
        "users" to MutableLiveData<Boolean>(),
        "accounts" to MutableLiveData<Boolean>(),
        "company" to MutableLiveData<Boolean>(),
        "buses" to MutableLiveData<Boolean>(),
        "terminals" to MutableLiveData<Boolean>(),
        "discounts" to MutableLiveData<Boolean>(),
        "routes" to MutableLiveData<Boolean>(),
        "routeSegments" to MutableLiveData<Boolean>(),
        "fare" to MutableLiveData<Boolean>(),
        "fareSetups" to MutableLiveData<Boolean>(),
        "hotspots" to MutableLiveData<Boolean>(),
        "deductions" to MutableLiveData<Boolean>(),
        "incentives" to MutableLiveData<Boolean>(),
        "roles" to MutableLiveData<Boolean>(),
        "deviceSettings" to MutableLiveData<Boolean>()
    )

    val usersSynced: LiveData<Boolean> get() = syncStates["users"]!!
    val accountsSynced: LiveData<Boolean> get() = syncStates["accounts"]!!
    val companySynced: LiveData<Boolean> get() = syncStates["company"]!!
    val busesSynced: LiveData<Boolean> get() = syncStates["buses"]!!
    val terminalsSynced: LiveData<Boolean> get() = syncStates["terminals"]!!
    val discountsSynced: LiveData<Boolean> get() = syncStates["discounts"]!!
    val routesSynced: LiveData<Boolean> get() = syncStates["routes"]!!
    val routeSegmentsSynced: LiveData<Boolean> get() = syncStates["routeSegments"]!!
    val fareSynced: LiveData<Boolean> get() = syncStates["fare"]!!
    val fareSetupsSynced: LiveData<Boolean> get() = syncStates["fareSetups"]!!
    val hotspotsSynced: LiveData<Boolean> get() = syncStates["hotspots"]!!
    val deductionsSynced: LiveData<Boolean> get() = syncStates["deductions"]!!
    val incentivesSynced: LiveData<Boolean> get() = syncStates["incentives"]!!
    val rolesSynced: LiveData<Boolean> get() = syncStates["roles"]!!
    val deviceSettingsSynced: LiveData<Boolean> get() = syncStates["deviceSettings"]!!

    private val _setupCompleted = MutableLiveData<Boolean>()
    val setupCompleted: LiveData<Boolean> get() = _setupCompleted

    init {
        syncStates.values.forEach { it.observeForever { checkSetupCompletion() } }
    }

    suspend fun startSetup(context: Context) {
        truncateTables()

        val response = api.get("${apiUrl}/InitialSetup/$sharedCompanyId/${validatedDeviceName(context)}", sharedApiToken)

        //================ SEQUENCE
        val deviceLastReceiptId = response.data["DeviceLastReceiptId"] as Int
        if (sharedPrefs.getInt("sharedDeviceLastReceiptId", 0) == 0 || sharedPrefs.getInt("sharedDeviceLastReceiptId", 0) < deviceLastReceiptId) {
            sharedPrefs.setInt("sharedDeviceLastReceiptId", deviceLastReceiptId)
        }

        val deviceLastDispatchId = response.data["DeviceLastDispatchId"] as Int
        if (sharedPrefs.getInt("sharedDeviceLastDispatchId", 0) == 0 || sharedPrefs.getInt("sharedDeviceLastDispatchId", 0) < deviceLastDispatchId) {
            sharedPrefs.setInt("sharedDeviceLastDispatchId", deviceLastDispatchId)
        }

        val deviceLastDispatchTripId = response.data["DeviceLastDispatchTripId"] as Int
        if (sharedPrefs.getInt("sharedDeviceLastDispatchTripId", 0) == 0 || sharedPrefs.getInt("sharedDeviceLastDispatchTripId", 0) < deviceLastDispatchTripId) {
            sharedPrefs.setInt("sharedDeviceLastDispatchTripId", deviceLastDispatchTripId)
        }

        val deviceLastIngressoId = response.data["DeviceLastIngressoId"] as Int
        if (sharedPrefs.getInt("sharedDeviceLastIngressoId", 0) == 0 || sharedPrefs.getInt("sharedDeviceLastIngressoId", 0) < deviceLastIngressoId) {
            sharedPrefs.setInt("sharedDeviceLastIngressoId", deviceLastIngressoId)
        }


        //================

        val company = response.data["Company"] as? JSONObject
        if (company != null) {
            sharedPrefs.setString("sharedCompanyName", company.getString("CompanyName"))
            sharedPrefs.setString("sharedCompanyTIN", company.getString("TIN"))
            saveDataIfNotEmpty("company", company)
        }

        val users = response.data["Users"] as? JSONArray
        if (users != null) saveDataIfNotEmpty("users", users)

        val terminals = response.data["Terminals"] as? JSONArray
        if (terminals != null) saveDataIfNotEmpty("terminals", terminals)

        val accounts = response.data["Accounts"] as? JSONArray
        if (accounts != null) saveDataIfNotEmpty("accounts", accounts)

        val buses = response.data["Buses"] as? JSONArray
        if (buses != null) saveDataIfNotEmpty("buses", buses)

        val discounts = response.data["Discounts"] as? JSONArray
        if (discounts != null) saveDataIfNotEmpty("discounts", discounts)

        val routes = response.data["Routes"] as? JSONArray
        if (routes != null) saveDataIfNotEmpty("routes", routes)

        val routeSegments = response.data["RouteSegments"] as? JSONArray
        if (routeSegments != null) saveDataIfNotEmpty("routeSegments", routeSegments)

        val fare = response.data["Fare"] as? JSONObject
        if (fare != null) saveDataIfNotEmpty("fare", fare)

        val fareSetups = response.data["FareSetups"] as? JSONArray
        if (fareSetups != null) saveDataIfNotEmpty("fareSetups", fareSetups)

        val hotspots = response.data["Hotspots"] as? JSONArray
        if (hotspots != null) saveDataIfNotEmpty("hotspots", hotspots)

        val deductions = response.data["Deductions"] as? JSONArray
        if (deductions != null) saveDataIfNotEmpty("deductions", deductions)

        val incentives = response.data["Incentives"] as? JSONArray
        if (incentives != null) saveDataIfNotEmpty("incentives", incentives)

        val roles = response.data["Roles"] as? JSONArray
        if (roles != null) saveDataIfNotEmpty("roles", roles)

        val deviceSettings = response.data["DeviceSettings"] as? JSONObject
        if (deviceSettings != null) saveDataIfNotEmpty("deviceSettings", deviceSettings)
    }

    private fun <T> saveDataIfNotEmpty(type: String, data: T) {
        if (data == null || (data is JSONObject && data.length() == 0) || (data is JSONArray && data.length() == 0)) {
            syncStates[type]?.value = true
        } else {
            saveData(type, data)
        }
    }

    private fun <T> saveData(type: String, data: T) {
        try {
            when (type) {
                "company" -> companyRepository.insert(convertToModel(data as JSONObject))
                "users" -> userRepository.insertUserBulk(convertToListModel(data as JSONArray))
                "accounts" -> accountRepository.insertBulk(convertToListModel(data as JSONArray))
                "buses" -> busRepository.insertBulk(convertToListModel(data as JSONArray))
                "terminals" -> terminalRepository.insertBulk(convertToListModel(data as JSONArray))
                "discounts" -> discountRepository.insertBulk(convertToListModel(data as JSONArray))
                "routes" -> routeRepository.insertRouteBulk(convertToListModel(data as JSONArray))
                "routeSegments" -> routeRepository.insertSegmentBulk(convertToListModel(data as JSONArray))
                "fare" -> fareRepository.insertFare(convertToModel(data as JSONObject))
                "fareSetups" -> fareRepository.insertFareSetupsBulk(convertToListModel(data as JSONArray))
                "hotspots" -> hotspotRepository.insertBulk(convertToListModel(data as JSONArray))
                "deductions" -> deductionRepository.insertBulk(convertToListModel(data as JSONArray))
                "incentives" -> incentiveRepository.insertBulk(convertToListModel(data as JSONArray))
                "roles" -> userRepository.insertRoleBulk(convertToListModel(data as JSONArray))
                "deviceSettings" -> deviceSettingsRepository.insert(convertToModel(data as JSONObject))
            }
            syncStates[type]?.value = true
        } catch (e: Exception) {
            syncStates[type]?.value = false
            Log.e("saveData", "Error parsing $type data: ${e.message}")
            truncateTables()
        }
    }

    private fun checkSetupCompletion() {
        _setupCompleted.value = syncStates.values.all { it.value == true }
    }

    private fun truncateTables() {
        userRepository.deleteAllUser()
        userRepository.resetAutoIncrementUser()

        accountRepository.deleteAll()
        accountRepository.resetAutoIncrement()

        companyRepository.deleteAll()
        companyRepository.resetAutoIncrement()

        busRepository.deleteAll()
        busRepository.resetAutoIncrement()

        terminalRepository.deleteAll()
        terminalRepository.resetAutoIncrement()

        discountRepository.deleteAll()
        discountRepository.resetAutoIncrement()

        routeRepository.deleteAllRoutes()
        routeRepository.resetAutoIncrementRoutes()

        routeRepository.deleteAllRouteSegments()
        routeRepository.resetAutoIncrementRouteSegments()

        fareRepository.deleteAllFare()
        fareRepository.resetAutoIncrementFare()

        fareRepository.deleteAllFareSetups()
        fareRepository.resetAutoIncrementFareSetups()

        hotspotRepository.deleteAll()
        hotspotRepository.resetAutoIncrement()

        deductionRepository.deleteAll()
        deductionRepository.resetAutoIncrement()

        incentiveRepository.deleteAll()
        incentiveRepository.resetAutoIncrement()

        userRepository.deleteAllRole()
        userRepository.resetAutoIncrementRole()

        deviceSettingsRepository.deleteAll()
        deviceSettingsRepository.resetAutoIncrement()
    }
}
