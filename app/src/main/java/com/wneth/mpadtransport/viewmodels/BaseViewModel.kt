package com.wneth.mpadtransport.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.wneth.mpadtransport.configurations.MPADTransportApp
import com.wneth.mpadtransport.models.DeviceSettingModel
import com.wneth.mpadtransport.repositories.IAccountRepository
import com.wneth.mpadtransport.repositories.IBusRepository
import com.wneth.mpadtransport.repositories.IIncentiveRepository
import com.wneth.mpadtransport.repositories.ICompanyRepository
import com.wneth.mpadtransport.repositories.IDeductionRepository
import com.wneth.mpadtransport.repositories.IDeviceSettingsRepository
import com.wneth.mpadtransport.repositories.IDiscountRepository
import com.wneth.mpadtransport.repositories.IDispatchRepository
import com.wneth.mpadtransport.repositories.IFareRepository
import com.wneth.mpadtransport.repositories.IHotspotRepository
import com.wneth.mpadtransport.repositories.IIngressoRepository
import com.wneth.mpadtransport.repositories.IInspectionRepository
import com.wneth.mpadtransport.repositories.IRemittanceRepository
import com.wneth.mpadtransport.repositories.IRouteRepository
import com.wneth.mpadtransport.repositories.ITerminalRepository
import com.wneth.mpadtransport.repositories.ITicketReceiptRepository
import com.wneth.mpadtransport.repositories.IUserRepository
import com.wneth.mpadtransport.utilities.HttpHelper
import com.wneth.mpadtransport.utilities.Preferences
import com.wneth.mpadtransport.utilities.validatedDeviceName
import javax.inject.Inject



open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    @Inject lateinit var userRepository: IUserRepository
    @Inject lateinit var accountRepository: IAccountRepository
    @Inject lateinit var companyRepository: ICompanyRepository
    @Inject lateinit var busRepository: IBusRepository
    @Inject lateinit var terminalRepository: ITerminalRepository
    @Inject lateinit var discountRepository: IDiscountRepository
    @Inject lateinit var routeRepository: IRouteRepository
    @Inject lateinit var dispatchRepository: IDispatchRepository
    @Inject lateinit var fareRepository: IFareRepository
    @Inject lateinit var ticketReceiptRepository: ITicketReceiptRepository
    @Inject lateinit var hotspotRepository: IHotspotRepository
    @Inject lateinit var remittanceRepository: IRemittanceRepository
    @Inject lateinit var deductionRepository: IDeductionRepository
    @Inject lateinit var incentiveRepository: IIncentiveRepository
    @Inject lateinit var ingressoRepository: IIngressoRepository
    @Inject lateinit var deviceSettingsRepository: IDeviceSettingsRepository
    @Inject lateinit var inspectionRepository: IInspectionRepository


    val api = HttpHelper()
    val sharedPrefs = Preferences(application)

    var sharedCompanyId: Int = 0
    var sharedApiToken: String = ""

    init {
        (application as MPADTransportApp).getAppComponent().inject(this)
        sharedCompanyId = sharedPrefs.getInt("sharedCompanyId",0)
        sharedApiToken = sharedPrefs.getString("sharedApiToken","")
    }


    fun setupCompleted():Boolean {
        val isSetup = userRepository.getUsers()
        //val isSetup = companyRepository.getCompanyDetails()
        return isSetup!!.size > 1
    }


    fun deviceSettings(): DeviceSettingModel? {
        return deviceSettingsRepository.getDeviceSettings()
    }

    fun validateReferenceId(referenceId: Int, table: String): Boolean {

        if (table == "dispatches") {
            val tmpRef = dispatchRepository.validateDispatchReferenceId()
            return if (tmpRef == 0) {
                true
            } else {
                referenceId == tmpRef
            }
        }
        if (table == "dispatch_trips") {
            val tmpRef = dispatchRepository.validateDispatchTripReferenceId()
            return if (tmpRef == 0) {
                true
            } else {
                referenceId == tmpRef
            }
        }
        if (table == "ticket_receipt") {
            val tmpRef = ticketReceiptRepository.validateTicketReceiptReferenceId()
            return if (tmpRef == 0) {
                true
            } else {
                referenceId == tmpRef
            }
        }
        if (table == "ingresso") {
            val tmpRef = ingressoRepository.validateIngressoReferenceId()
            return if (tmpRef == 0) {
                true
            } else {
                referenceId == tmpRef
            }
        }

        return false
    }


    fun showAllSharedPrefs() {
        Log.d("SharedPrefs", "sharedAdministratorId: ${sharedPrefs.getInt("sharedAdministratorId", 0).takeIf { it != 0 } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedApiToken: ${sharedPrefs.getString("sharedApiToken", "").takeIf { !it.isNullOrEmpty() } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedCashierId: ${sharedPrefs.getInt("sharedCashierId", 0).takeIf { it != 0 } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedCompanyId: ${sharedPrefs.getInt("sharedCompanyId", 0).takeIf { it != 0 } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedCompanyName: ${sharedPrefs.getString("sharedCompanyName", "").takeIf { !it.isNullOrEmpty() } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedCompanyTIN: ${sharedPrefs.getString("sharedCompanyTIN", "").takeIf { !it.isNullOrEmpty() } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedConductorId: ${sharedPrefs.getInt("sharedConductorId", 0).takeIf { it != 0 } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedConductorName: ${sharedPrefs.getString("sharedConductorName", "").takeIf { !it.isNullOrEmpty() } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedValidatedDeviceId: ${sharedPrefs.getInt("sharedValidatedDeviceId", 0).takeIf { it != 0 } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedValidatedDeviceName: ${sharedPrefs.getString("sharedValidatedDeviceName", "").takeIf { !it.isNullOrEmpty() } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedDispatcherId: ${sharedPrefs.getInt("sharedDispatcherId", 0).takeIf { it != 0 } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedReversedById: ${sharedPrefs.getInt("sharedReversedById", 0).takeIf { it != 0 } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedDriverId: ${sharedPrefs.getInt("sharedDriverId", 0).takeIf { it != 0 } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedDriverName: ${sharedPrefs.getString("sharedDriverName", "").takeIf { !it.isNullOrEmpty() } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedInitialUserFullName: ${sharedPrefs.getString("sharedInitialUserFullName", "").takeIf { !it.isNullOrEmpty() } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedInitialUserId: ${sharedPrefs.getInt("sharedInitialUserId", 0).takeIf { it != 0 } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedInitialUserRoleId: ${sharedPrefs.getInt("sharedInitialUserRoleId", 0).takeIf { it != 0 } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedInspectorId: ${sharedPrefs.getInt("sharedInspectorId", 0).takeIf { it != 0 } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedTerminalId: ${sharedPrefs.getInt("sharedTerminalId", 0).takeIf { it != 0 } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedBusId: ${sharedPrefs.getInt("sharedBusId", 0).takeIf { it != 0 } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedMPADPayPersonnelId: ${sharedPrefs.getInt("sharedMPADPayPersonnelId", 0).takeIf { it != 0 } ?: "not set yet."}")



        Log.d("SharedPrefs", "sharedHasDispatch: ${sharedPrefs.getBoolean("sharedHasDispatch", false).takeIf { it } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedDeviceConfirmed: ${sharedPrefs.getBoolean("sharedDeviceConfirmed", false).takeIf { it } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedDeviceLastReceiptId: ${sharedPrefs.getInt("sharedDeviceLastReceiptId", 0).takeIf { it != 0 } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedDeviceLastDispatchId: ${sharedPrefs.getInt("sharedDeviceLastDispatchId", 0).takeIf { it != 0 } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedDeviceLastDispatchTripId: ${sharedPrefs.getInt("sharedDeviceLastDispatchTripId", 0).takeIf { it != 0 } ?: "not set yet."}")
        Log.d("SharedPrefs", "sharedDeviceLastIngressoId: ${sharedPrefs.getInt("sharedDeviceLastIngressoId", 0).takeIf { it != 0 } ?: "not set yet."}")
    }
}

