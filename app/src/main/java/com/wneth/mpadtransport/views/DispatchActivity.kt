package com.wneth.mpadtransport.views

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.wneth.mpadtransport.databinding.ActivityDispatchBinding
import com.wneth.mpadtransport.models.BusModel
import com.wneth.mpadtransport.models.DispatchModel
import com.wneth.mpadtransport.models.DispatchTripModel
import com.wneth.mpadtransport.models.RouteModel
import com.wneth.mpadtransport.models.TerminalModel
import com.wneth.mpadtransport.models.UserWithRole
import com.wneth.mpadtransport.utilities.validatedDeviceName
import com.wneth.mpadtransport.utilities.dialogs.DialogRecyclerItems
import com.wneth.mpadtransport.utilities.getRouteDirectionName
import com.wneth.mpadtransport.utilities.interfaces.PrinterEventListener
import com.wneth.mpadtransport.utilities.interfaces.RecyclerDialogEventListener
import com.wneth.mpadtransport.utilities.interfaces.SearchFragmentListener
import com.wneth.mpadtransport.utilities.printer.Printer
import com.wneth.mpadtransport.utilities.printer.templates.generateDispatchTemplate
import com.wneth.mpadtransport.utilities.reverseStringParts
import com.wneth.mpadtransport.utilities.showCustomToast
import com.wneth.mpadtransport.utilities.writeLogToSDCard
import com.wneth.mpadtransport.viewmodels.DispatchActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DispatchActivity : AppCompatActivity(), SearchFragmentListener, RecyclerDialogEventListener,
    PrinterEventListener {

    private lateinit var viewModel: DispatchActivityViewModel
    private lateinit var viewBinding: ActivityDispatchBinding
    private lateinit var printer: Printer
    private lateinit var recyclerDialog: DialogRecyclerItems

    private var _tmpCompanyId: Int = 0
    private var _tmpRouteId: Int = 0
    private var _tmpDirectionId: Int = 0
    private var _tmpTerminalId: Int = 0
    private var _tmpDriverId: Int = 0
    private var _tmpConductorId: Int = 0
    private var _tmpBusId: Int = 0
    private var _tmpStatus: Int = 0
    private var _currentDeviceName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[DispatchActivityViewModel::class.java]
        viewBinding = ActivityDispatchBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        try {
            printer = Printer(this)
            printer.initPrinter()
        }catch (ex: Exception){

        }

        _currentDeviceName = validatedDeviceName(this)

        // Initialize click events
        /*
        * 1. RouteModel
        * 2. TerminalModel
        * 3. Conductor
        * 4. Driver
        * 5. BusModel
        * */
        val buttonDispatchMap = mapOf(
            viewBinding.btnSetRoute to 1,
            viewBinding.btnSetTerminal to 2,
            viewBinding.btnSetConductor to 3,
            viewBinding.btnSetDriver to 4,
            viewBinding.btnSetBus to 5
        )
        buttonDispatchMap.forEach { (button, dispatchInfo) ->
            button.setOnClickListener {
                setDispatchInfo(dispatchInfo)
            }
        }

        viewBinding.confirmDispatch.setOnClickListener { view ->
            viewBinding.confirmDispatch.isEnabled = false
            lifecycleScope.launch(Dispatchers.IO) {
                saveDispatch(view)
            }
        }
    }

    private fun setDispatchInfo(entity: Int) {
        try {
            recyclerDialog = DialogRecyclerItems.newInstance(entity)
            recyclerDialog.show(supportFragmentManager, "DialogForm")
            recyclerDialog.recyclerEventListener = this
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    override fun onRecyclerItemClicked(
        id: Int,
        position: Int,
        title: String,
        subTitle: String,
        navigateTo: String,
        data: Any?
    ) {
        if (data is RouteModel) {

            _tmpCompanyId = data.companyId
            _tmpRouteId = data.id
            _tmpDirectionId = data.directionId
            viewBinding.txtRoute.text = if (data.directionId == 2) {
                reverseStringParts(data.name)
            } else {
                data.name
            }
            viewBinding.txtDirection.text = getRouteDirectionName(data.directionId)
        }

        if (data is TerminalModel) {
            _tmpTerminalId = data.id
            viewBinding.txtTerminal.text = data.name
        }

        if (data is UserWithRole) {
            if (data.roleId == 6) {
                _tmpConductorId = data.id
                viewBinding.txtConductor.text = data.fullName
                viewModel.sharedPrefs.setInt("sharedConductorId", data.id)
                viewModel.sharedPrefs.setString("sharedConductorName", data.fullName)
            }
            if (data.roleId == 7) {
                _tmpDriverId = data.id
                viewBinding.txtDriver.text = data.fullName
                viewModel.sharedPrefs.setInt("sharedDriverId", data.id)
                viewModel.sharedPrefs.setString("sharedDriverName", data.fullName)
            }
        }

        if (data is BusModel) {
            _tmpBusId = data.id
            viewBinding.txtBus.text = data.busNumber.toString()
        }
    }

    override fun onRecyclerItemLongClicked(
        id: Int,
        position: Int,
        title: String,
        subTitle: String,
        navigateTo: String
    ) {
        //
    }

    private fun saveDispatch(view: View) {
        try {
            if (
                _tmpRouteId == 0 ||
                _tmpTerminalId == 0 ||
                _tmpConductorId == 0 ||
                _tmpDriverId == 0 ||
                _tmpBusId == 0
            ) {
                CoroutineScope(Dispatchers.IO).launch {
                    Toast(this@DispatchActivity).showCustomToast(
                        "Incomplete dispatch information",
                        this@DispatchActivity,
                        Toast.LENGTH_SHORT
                    )
                }

                return
            }


            CoroutineScope(Dispatchers.IO).launch {
                val dispatchReferenceId =
                    viewModel.sharedPrefs.getInt("sharedDeviceLastDispatchId", 0) + 1

                if (!viewModel.validateReferenceId(dispatchReferenceId, "dispatches")) {
                    withContext(Dispatchers.Main) {
                        Toast(this@DispatchActivity).showCustomToast(
                            "Dispatch already exists",
                            this@DispatchActivity,
                            Toast.LENGTH_SHORT
                        )
                    }
                    return@launch
                }

                val deviceName = validatedDeviceName(this@DispatchActivity)
                val dispatch = DispatchModel(
                    id = 0,
                    referenceId = dispatchReferenceId,
                    companyId = _tmpCompanyId,
                    deviceName = deviceName,
                    dispatcherId = viewModel.sharedPrefs.getInt("sharedDispatcherId", 0),
                    driverId = _tmpDriverId,
                    conductorId = _tmpConductorId,
                    busId = _tmpBusId,
                    status = _tmpStatus
                )
                viewModel.insertDispatch(dispatch)

                val dispatchTripReferenceId =
                    viewModel.sharedPrefs.getInt("sharedDeviceLastDispatchTripId", 0) + 1
                val dispatchTrip = DispatchTripModel(
                    id = 0,
                    deviceName = deviceName,
                    companyId = _tmpCompanyId,
                    referenceId = dispatchTripReferenceId,
                    dispatchReferenceId = dispatchReferenceId,
                    routeId = _tmpRouteId,
                    directionId = _tmpDirectionId,
                    terminalId = _tmpTerminalId,
                    reversedById = viewModel.sharedPrefs.getInt("sharedDispatcherId", 0)
                )
                viewModel.insertDispatchTrip(dispatchTrip)


                viewModel.sharedPrefs.setBoolean("sharedHasDispatch", true)

                viewModel.sharedPrefs.setInt("sharedDeviceLastDispatchId", dispatchReferenceId)
                viewModel.sharedPrefs.setInt(
                    "sharedDeviceLastDispatchTripId",
                    dispatchTripReferenceId
                )
                viewModel.sharedPrefs.setString("sharedValidatedDeviceName", deviceName)

                viewModel.sharedPrefs.setInt("sharedTerminalId", _tmpTerminalId)
                viewModel.sharedPrefs.setInt("sharedConductorId", _tmpConductorId)
                viewModel.sharedPrefs.setInt("sharedDriverId", _tmpDriverId)
                viewModel.sharedPrefs.setInt("sharedBusId", _tmpBusId)

                val data = viewModel.getDispatchByReferenceId(dispatchReferenceId)
                    ?: return@launch
                try {
                    val template = generateDispatchTemplate(this@DispatchActivity, data)
                    printer.printTemplate(template)
                    withContext(Dispatchers.Main) {
                        Toast(this@DispatchActivity).showCustomToast(
                            "Dispatch saved",
                            this@DispatchActivity,
                            Toast.LENGTH_SHORT
                        )
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e("DispatchActivity", "Printing failed", e)

                    withContext(Dispatchers.Main) {
                        Toast(this@DispatchActivity).showCustomToast(
                            "Printing failed",
                            this@DispatchActivity,
                            Toast.LENGTH_SHORT
                        )
                    }
                }
            }

        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSearchTextReceived(text: String) {
        recyclerDialog.updateSearchKeyword(text)
    }

    override fun onPrinterNormal() {
        viewBinding.confirmDispatch.isEnabled = true
    }

    override fun onPrinterBusy() {
        viewBinding.confirmDispatch.isEnabled = false
    }

}