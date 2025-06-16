package com.wneth.mpadtransport.viewmodels

import android.app.Application
import com.google.gson.Gson
import com.wneth.mpadtransport.configurations.MPADTransportApp
import com.wneth.mpadtransport.utilities.apiUrl

class ConfigurationActivityViewModel (application: Application): BaseViewModel(application) {

    init {
        (application as MPADTransportApp).getAppComponent().inject(this)
    }

    suspend fun syncIngresso(): Int{

        val gson = Gson()

        val ingresso = ingressoRepository.getUnsyncedIngresso()
        if(ingresso.isEmpty()){
            return 0
        }
        val dispatchIds = ingresso.map { it.dispatchReferenceId }
        val ingressoIds = ingresso.map { it.referenceId }

        val ingressoDeductions  = ingressoRepository.getUnsyncedIngressoDeductions(ingressoIds) // returns a List of Model
        val ingressoDispatches  = ingressoRepository.getUnsyncedIngressoDispatches(dispatchIds) // returns a List of Model
        val ingressoDispatchTrips  = ingressoRepository.getUnsyncedIngressoDispatchTrips(dispatchIds) // returns a List of Model
        val ingressoDispatchTickets  = ingressoRepository.getUnsyncedDispatchTickets(dispatchIds) // returns a List of Model
        val ingressoRemittances  = ingressoRepository.getUnsyncedIngressoRemittances(dispatchIds) // returns a List of Model
        val ingressoInspections  = ingressoRepository.getUnsyncedInspections(dispatchIds)

        // Serialize lists to JSON strings
        val jsonIngresso = gson.toJson(ingresso)
        val jsonDeductions = gson.toJson(ingressoDeductions)
        val jsonDispatches = gson.toJson(ingressoDispatches)
        val jsonDispatchTrips = gson.toJson(ingressoDispatchTrips)
        val jsonDispatchTickets = gson.toJson(ingressoDispatchTickets)
        val jsonRemittances = gson.toJson(ingressoRemittances)
        val jsonInspections = gson.toJson(ingressoInspections)


        // Prepare form data for the POST request
        val formData = mutableMapOf<String, String>()
        formData["ingresso"] = jsonIngresso
        formData["deductions"] = jsonDeductions
        formData["dispatches"] = jsonDispatches
        formData["dispatch_trips"] = jsonDispatchTrips
        formData["dispatch_tickets"] = jsonDispatchTickets
        formData["remittances"] = jsonRemittances
        formData["inspections"] = jsonInspections

        val jsonData = gson.toJson(formData)

        val ingressoData = mapOf(
            "ingressoData" to jsonData
        )

        val response = api.post("${apiUrl}/Ingresso",ingressoData, sharedApiToken)

        if(response.status == 200){
            ingressoRepository.updateIngressoStatus(ingressoIds)
            ingressoRepository.updateDispatchesStatus(dispatchIds)
            ingressoRepository.updateReceiptsStatus(dispatchIds)

            /*val deviceLastReceiptId = response.data["DeviceLastReceiptId"] as Int
            sharedPrefs.setInt("sharedDeviceLastReceiptId", deviceLastReceiptId)

            val deviceLastDispatchId = response.data["DeviceLastDispatchId"] as Int
            sharedPrefs.setInt("sharedDeviceLastDispatchId", deviceLastDispatchId)

            val deviceLastDispatchTripId = response.data["DeviceLastDispatchTripId"] as Int
            sharedPrefs.setInt("sharedDeviceLastDispatchTripId", deviceLastDispatchTripId)

            val deviceLastIngressoId = response.data["DeviceLastIngressoId"] as Int
            sharedPrefs.setInt("sharedDeviceLastIngressoId", deviceLastIngressoId)*/
            
            return 1
        }else{
            return 2
        }
    }

}