package com.wneth.mpadtransport.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.wneth.mpadtransport.databinding.ActivityReverseTripBinding
import com.wneth.mpadtransport.models.DispatchTripModel
import com.wneth.mpadtransport.models.DispatchTripReportModel
import com.wneth.mpadtransport.models.DispatchWithNameModel
import com.wneth.mpadtransport.models.RouteModel
import com.wneth.mpadtransport.models.TerminalModel
import com.wneth.mpadtransport.utilities.validatedDeviceName
import com.wneth.mpadtransport.utilities.dialogs.DialogRecyclerItems
import com.wneth.mpadtransport.utilities.interfaces.PrinterEventListener
import com.wneth.mpadtransport.utilities.interfaces.RecyclerDialogEventListener
import com.wneth.mpadtransport.utilities.interfaces.SearchFragmentListener
import com.wneth.mpadtransport.utilities.printer.Printer
import com.wneth.mpadtransport.utilities.printer.templates.generateTripReportTemplate
import com.wneth.mpadtransport.utilities.setVisibility
import com.wneth.mpadtransport.utilities.showCustomToast
import com.wneth.mpadtransport.utilities.writeLogToSDCard
import com.wneth.mpadtransport.viewmodels.DispatchActivityViewModel
import com.wneth.mpadtransport.viewmodels.TicketReceiptActivityViewModel
import com.wneth.mpadtransport.views.configurations.TicketReceiptDataActivity

class ReverseTripActivity : AppCompatActivity(), SearchFragmentListener,
    RecyclerDialogEventListener, PrinterEventListener {

    private lateinit var viewModel: DispatchActivityViewModel
    private lateinit var viewBinding: ActivityReverseTripBinding
    private lateinit var viewModelDispatch: DispatchActivityViewModel
    private lateinit var viewModelReceipt: TicketReceiptActivityViewModel
    private lateinit var dialog: DialogRecyclerItems

    private var _tmpTerminalId: Int = 0
    private var _tmpRouteId: Int = 0
    private var _tmpRouteName: String = "";
    private var _tmpRouteDirection: Int = 0
    private lateinit var printer: Printer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[DispatchActivityViewModel::class.java]
        viewModelDispatch = ViewModelProvider(this)[DispatchActivityViewModel::class.java]
        viewModelReceipt = ViewModelProvider(this)[TicketReceiptActivityViewModel::class.java]
        viewBinding = ActivityReverseTripBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Initialize Printer
        try {
            printer = Printer(this)
            printer.initPrinter()
        }catch (ex: Exception){

        }

        viewBinding.btnSetTerminal.setOnClickListener {
            dialog = DialogRecyclerItems.newInstance(2)
            dialog.show(supportFragmentManager, "DialogForm")
            dialog.recyclerEventListener = this
        }

        viewBinding.btnReverse.setOnClickListener { view ->
            saveReversedTrip(view)
        }

        viewBinding.btnShowReceipts.setOnClickListener {
            startActivity(Intent(this, TicketReceiptDataActivity::class.java))
        }

        viewBinding.btnChangeRoute.setOnClickListener {
            changeRoute()
        }

        getLatestDispatchTripRoute();
        initializeSettings()
    }

    private fun initializeSettings() {
        try {
            val deviceSettings = viewModel.deviceSettings()!!
            setVisibility(viewBinding.btnShowReceipts, deviceSettings.showReverseViewTickets)
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun changeRoute() {
        try {
            if (_tmpTerminalId == 0) {
                Toast.makeText(this, "Please select a terminal", Toast.LENGTH_SHORT).show()
                return
            }
            dialog = DialogRecyclerItems.newInstance(1)
            dialog.show(supportFragmentManager, "DialogForm")
            dialog.recyclerEventListener = this
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun getLatestDispatchTripRoute() {
        try {
            val latestDispatch = viewModel.getLatestDispatchTrip()
            _tmpRouteId = latestDispatch?.routeId ?: 0
            _tmpRouteName = latestDispatch?.routeName ?: ""
            _tmpRouteDirection = latestDispatch?.directionId ?: 0
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveReversedTrip(view: View) {
        try {
            if (_tmpTerminalId == 0) {
                Toast(this).showCustomToast("Please select a terminal", this, Toast.LENGTH_SHORT)
                return;
            }
            val route = viewModel.getReversedRoute(_tmpRouteId, _tmpRouteDirection)
            if (route == null) {
                Toast(this).showCustomToast("No reversed route found", this, Toast.LENGTH_SHORT)
                return
            }

            val dispatchTripReferenceId =
                viewModel.sharedPrefs.getInt("sharedDeviceLastDispatchTripId", 0) + 1
            val deviceName = validatedDeviceName(this@ReverseTripActivity);
            val dispatchTrip = DispatchTripModel(
                id = 0,
                deviceName = deviceName,
                referenceId = dispatchTripReferenceId,
                dispatchReferenceId = viewModel.sharedPrefs.getInt("sharedDeviceLastDispatchId", 0),
                routeId = route.id ?: 0,
                directionId = route.directionId,
                companyId = route.companyId ?: 0,
                terminalId = _tmpTerminalId,
                reversedById = viewModel.sharedPrefs.getInt("sharedReversedById", 0),
            )
            viewModel.insertDispatchTrip(dispatchTrip)
            printTripTicketSummary()
            viewModel.sharedPrefs.setInt("sharedDeviceLastDispatchTripId", dispatchTripReferenceId)
            Toast(this).showCustomToast("Reverse successful", this, Toast.LENGTH_SHORT)
            finish()
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun printTripTicketSummary() {
        try {
            viewBinding.btnReverse.isEnabled = false
            val dispatchReference = viewModel.sharedPrefs.getInt("sharedDeviceLastDispatchId", 0)
            val dispatchTripReferenceId =
                viewModel.sharedPrefs.getInt("sharedDeviceLastDispatchTripId", 0)


            val receipts =
                viewModelReceipt.getAllReceiptWithNamesByDispatchReferenceId(dispatchReference)
            val dispatch = viewModelDispatch.getDispatchByReferenceId(dispatchReference)

            val maxDispatchTripId = receipts.maxOfOrNull { it.dispatchTripReferenceId }
            val filteredReceipts =
                receipts.filter { it.dispatchTripReferenceId == maxDispatchTripId }

            //val filteredReceipts: List<TicketReceiptWithNameModel> = receipts

            val safeDispatch = dispatch ?: DispatchWithNameModel(
                dispatchId = 0,
                dispatchReferenceId = 0,
                dispatchTripReferenceId = 0,
                companyId = 0,
                deviceName = "",
                routeId = 0,
                routeName = "",
                directionId = 0,
                terminalId = 0,
                terminalName = "",
                dispatcherId = 0,
                dispatcherName = "",
                driverId = 0,
                driverName = "",
                conductorId = 0,
                conductorName = "",
                busId = 0,
                busNumber = 0,
                busPlateNumber = "",
                status = 0,
                dateCreated = ""
            )
            val tripReport = DispatchTripReportModel(
                dispatch = safeDispatch,
                receipts = filteredReceipts
            )
            val template = generateTripReportTemplate(this, tripReport)
            printer.printTemplate(template)
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
        if (data is TerminalModel) {
            _tmpTerminalId = data.id
            viewBinding.txtTerminal.text = data.name
        }

        if (data is RouteModel) {
            routeChangeConfirmation(data)
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

    private fun routeChangeConfirmation(route: RouteModel) {
        try {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmation")
            builder.setMessage("Are you sure you want to change route to '${route.name}'?")

            builder.setPositiveButton("Yes") { dialog, which ->
                if (_tmpRouteId == 0) {
                    Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
                    Toast(this).showCustomToast(
                        "Please select a new route",
                        this,
                        Toast.LENGTH_SHORT
                    )
                } else {
                    routeChangeConfirmed(route)
                    finish()
                }
            }

            builder.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun routeChangeConfirmed(route: RouteModel) {
        try {
            _tmpRouteId = route.id
            val dispatchTripReferenceId =
                viewModel.sharedPrefs.getInt("sharedDeviceLastDispatchTripId", 0) + 1
            val deviceName = validatedDeviceName(this@ReverseTripActivity);
            val dispatchTrip = DispatchTripModel(
                id = 0,
                deviceName = deviceName,
                referenceId = dispatchTripReferenceId,
                dispatchReferenceId = viewModel.sharedPrefs.getInt("sharedDeviceLastDispatchId", 0),
                routeId = _tmpRouteId ?: 0,
                directionId = route.directionId,
                companyId = route.companyId ?: 0,
                terminalId = _tmpTerminalId,
                reversedById = viewModel.sharedPrefs.getInt("sharedReversedById", 0),
            )
            viewModel.insertDispatchTrip(dispatchTrip)
            viewModel.sharedPrefs.setInt("sharedDeviceLastDispatchTripId", dispatchTripReferenceId)
            printTripTicketSummary()
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSearchTextReceived(text: String) {
        dialog.updateSearchKeyword(text)
    }

    override fun onPrinterNormal() {
        viewBinding.btnReverse.isEnabled = true
    }

    override fun onPrinterBusy() {
        viewBinding.btnReverse.isEnabled = false
    }

}

