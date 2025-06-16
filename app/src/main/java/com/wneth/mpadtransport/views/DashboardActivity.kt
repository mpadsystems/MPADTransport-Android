package com.wneth.mpadtransport.views


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.wneth.mpadtransport.MainActivity
import com.wneth.mpadtransport.R
import com.wneth.mpadtransport.databinding.ActivityDashboardBinding
import com.wneth.mpadtransport.models.DispatchTripReportModel
import com.wneth.mpadtransport.models.DispatchWithNameModel
import com.wneth.mpadtransport.models.HttpResponseModel
import com.wneth.mpadtransport.models.TicketReceiptWithNameModel
import com.wneth.mpadtransport.models.UserModel
import com.wneth.mpadtransport.utilities.DBBackupWorker
import com.wneth.mpadtransport.utilities.PermissionManager
import com.wneth.mpadtransport.utilities.validatedDeviceName
import com.wneth.mpadtransport.utilities.dialogs.DialogAuthentication
import com.wneth.mpadtransport.utilities.hideSystemUIOnCreate
import com.wneth.mpadtransport.utilities.hideSystemUIOnWindowFocusChanged
import com.wneth.mpadtransport.utilities.interfaces.DialogEventListener
import com.wneth.mpadtransport.utilities.interfaces.RecyclerDialogEventListener
import com.wneth.mpadtransport.utilities.printer.Printer
import com.wneth.mpadtransport.utilities.printer.templates.generateTripReportTemplate
import com.wneth.mpadtransport.utilities.recyclerviews.MenuRecyclerViewAdapter
import com.wneth.mpadtransport.utilities.showCustomToast
import com.wneth.mpadtransport.utilities.systemDeviceName
import com.wneth.mpadtransport.utilities.writeLogToSDCard
import com.wneth.mpadtransport.viewmodels.DashboardActivityViewModel
import com.wneth.mpadtransport.viewmodels.DispatchActivityViewModel
import com.wneth.mpadtransport.viewmodels.MainActivityViewModel
import com.wneth.mpadtransport.viewmodels.TicketReceiptActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import androidx.activity.OnBackPressedCallback


class DashboardActivity : AppCompatActivity(), DialogEventListener, RecyclerDialogEventListener {

    /*
    *   Module ID
    *   1. DispatchModel
    *   2. RemittanceModel
    *   3. Ticketing
    *   4. Reverse Trip
    *   5. Inspection
    *   6. Trip Report
    *   7. Configuration
    * */

    private lateinit var backupReceiver: BroadcastReceiver
    private lateinit var permissionsManager: PermissionManager
    private var backupFrequency: Int = 20

    private lateinit var printer: Printer

    private val backDispatcher = onBackPressedDispatcher
    private lateinit var viewModelMain: MainActivityViewModel
    private lateinit var viewModelDashboard: DashboardActivityViewModel
    private lateinit var viewModelDispatch: DispatchActivityViewModel
    private lateinit var viewModelReceipt: TicketReceiptActivityViewModel


    private lateinit var viewBinding: ActivityDashboardBinding
    private lateinit var dialogAuth: DialogAuthentication

    private var _tmpModuleId: Int = 0
    private var tmpDispatchId: Int = 0

    // Module visibility
    private var dispatchModule: Boolean = true
    private var remittanceModule: Boolean = false
    private var ticketReceiptModule: Boolean = false
    private var reverseTripModule: Boolean = false
    private var inspectionModule: Boolean = false
    private var tripReportModule: Boolean = false
    private var mpadPayModule: Boolean = true
    private var configurationModule: Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemUIOnCreate(window)


        // Initialize Printer
        try {
            printer = Printer(this)
            printer.initPrinter()
        }catch (ex: Exception){

        }


        viewModelMain = ViewModelProvider(this)[MainActivityViewModel::class.java]
        viewModelDashboard = ViewModelProvider(this)[DashboardActivityViewModel::class.java]
        viewModelDispatch = ViewModelProvider(this)[DispatchActivityViewModel::class.java]
        viewModelReceipt = ViewModelProvider(this)[TicketReceiptActivityViewModel::class.java]

        permissionsManager = PermissionManager(this)

        // Initialize Binding
        viewBinding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)


        initializeSettings()
        scheduleDatabaseBackup()

        permissionsManager = PermissionManager(this)
        getDeviceName()

        backupReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Toast(this@DashboardActivity).showCustomToast(
                    "Database backup successful",
                    this@DashboardActivity,
                    Toast.LENGTH_SHORT
                )
            }
        }
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(backupReceiver, IntentFilter("DatabaseBackupStatus"))


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                _tmpModuleId = 100
                dialogAuth = DialogAuthentication.newInstance(_tmpModuleId)
                dialogAuth.show(supportFragmentManager, "DialogForm")
                dialogAuth.dialogEventListener = this@DashboardActivity
            }
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_HOME -> {
                // Do nothing when Home button is pressed
                true // Return true to prevent default action
            }

            KeyEvent.KEYCODE_APP_SWITCH -> {
                // Do nothing when Recent Apps (Task List) button is pressed
                true // Return true to prevent default action
            }

            else -> super.onKeyDown(keyCode, event)
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUIOnWindowFocusChanged(window.decorView, window)
        }
    }


    override fun onResume() {
        super.onResume()

        initializeDashboard()
        viewModelDashboard.viewModelScope.launch {
            viewModelDashboard.showAllSharedPrefs()
        }

        /*val backupRequest = OneTimeWorkRequestBuilder<DBBackupWorker>().build()
        WorkManager.getInstance(this).enqueue(backupRequest)
        Toast.makeText(this,"Database created successfully",Toast.LENGTH_SHORT).show()*/
    }

    override fun onRestart() {
        super.onRestart()
        initializeDashboard()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(backupReceiver)
    }


    private fun getDeviceName() {
        try {
            val validatedDeviceName =
                viewModelMain.sharedPrefs.getString("sharedValidatedDeviceName", "");
            val validatedDeviceId = viewModelMain.sharedPrefs.getInt("sharedValidatedDeviceId", 0);

            if (validatedDeviceName != "") {
                if (systemDeviceName(this@DashboardActivity) != validatedDeviceName) {
                    Toast(this@DashboardActivity).showCustomToast(
                        "The device was renamed since the last validation. App cannot be used.",
                        this@DashboardActivity,
                        Toast.LENGTH_LONG
                    )
                    Handler(Looper.getMainLooper()).postDelayed({
                        finishAffinity()
                    }, 3000)
                    return
                }
            }

            val isDeviceConfirmed =
                viewModelMain.sharedPrefs.getBoolean("sharedDeviceConfirmed", false)
            if (isDeviceConfirmed) {
                return
            }


            val sharedApiToken = viewModelMain.sharedPrefs.getString("sharedApiToken", "")
            val companyId = viewModelMain.sharedPrefs.getInt("sharedCompanyId", 0)

            // Launch a coroutine in the ViewModel's scope
            viewModelMain.viewModelScope.launch(Dispatchers.IO) {
                try {
                    val deviceName = validatedDeviceName(this@DashboardActivity)
                    val response =
                        viewModelMain.api.get("Device/$companyId/$deviceName", sharedApiToken)
                    withContext(Dispatchers.Main) {
                        showAlertDialog(response)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun showAlertDialog(response: HttpResponseModel) {
        try {
            var message = ""
            val validatedValue = response.data["Validated"].toString().toIntOrNull() ?: -1

            when {
                validatedValue == 0 -> {
                    message =
                        "This device is already assigned. Please unassign it and run the app again."
                    Toast(this@DashboardActivity).showCustomToast(
                        message,
                        this@DashboardActivity,
                        Toast.LENGTH_LONG
                    )
                    Handler(Looper.getMainLooper()).postDelayed({
                        finishAffinity()
                    }, 3000)
                    return
                }

                validatedValue > 0 -> {
                    message = "Device validated!"
                    viewModelMain.sharedPrefs.setBoolean("sharedDeviceConfirmed", true)
                    viewModelMain.sharedPrefs.setString(
                        "sharedValidatedDeviceName",
                        systemDeviceName(this@DashboardActivity)
                    )
                    Toast(this@DashboardActivity).showCustomToast(
                        message,
                        this@DashboardActivity,
                        Toast.LENGTH_SHORT
                    )
                    val page = Intent(this@DashboardActivity, InitialSetupActivity::class.java)
                    page.putExtra("reSync", true)
                    startActivity(page)
                    return
                }

                validatedValue == -1 -> {
                    message = "This device is not recognized. Do you still want to use it?"
                }

                else -> {
                    message = "Unknown response. Please try again."
                }
            }

            // Show AlertDialog for unrecognized devices
            AlertDialog.Builder(this@DashboardActivity)
                .setTitle("Device Validation")
                .setMessage(message)
                .setPositiveButton("Yes") { dialog, _ ->
                    viewModelMain.sharedPrefs.setBoolean("sharedDeviceConfirmed", true)
                    viewModelMain.sharedPrefs.setString(
                        "sharedValidatedDeviceName",
                        systemDeviceName(this@DashboardActivity)
                    )
                    dialog.dismiss()
                    Toast(this@DashboardActivity).showCustomToast(
                        "Device usage confirmed!",
                        this@DashboardActivity,
                        Toast.LENGTH_SHORT
                    )
                    val page = Intent(this@DashboardActivity, InitialSetupActivity::class.java)
                    page.putExtra("reSync", true)
                    startActivity(page)
                }
                .setNegativeButton("No") { _, _ ->
                    finishAffinity()
                }
                .setCancelable(false)
                .show()
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun initializeSettings() {
        try {
            val deviceSettings = viewModelMain.deviceSettings()!!
            backupFrequency = deviceSettings.backUpFrequency
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun scheduleDatabaseBackup() {
        try {
            if (backupFrequency > 0) {
                val backupRequest = OneTimeWorkRequestBuilder<DBBackupWorker>()
                    .setInitialDelay(
                        backupFrequency.toLong(),
                        TimeUnit.MINUTES
                    ) // Add this line to set delay
                    .setInputData(
                        Data.Builder()
                            .putLong("delayInMinutes", backupFrequency.toLong())
                            .build()
                    )
                    .build()

                WorkManager.getInstance(this).enqueueUniqueWork(
                    "DatabaseBackup",
                    ExistingWorkPolicy.REPLACE,
                    backupRequest
                )
            }
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun initializeDashboard() {
        try {
            if (viewModelDashboard.hasDispatchData()) {
                dispatchModule = false;
                remittanceModule = true
                ticketReceiptModule = true
                reverseTripModule = true
                inspectionModule = true
                tripReportModule = true
                mpadPayModule = true
            } else {
                dispatchModule = true;
                remittanceModule = false
                ticketReceiptModule = false
                reverseTripModule = false
                inspectionModule = false
                tripReportModule = false
                mpadPayModule = true
            }

            if (viewModelDashboard.getUnsyncedIngressoCount() > 0) {
                viewBinding.txtUnsyncedIngresso.visibility = View.VISIBLE
                viewBinding.txtUnsyncedIngresso.text =
                    "${viewModelDashboard.getUnsyncedIngressoCount()} Unsynced Ingresso"
            } else {
                viewBinding.txtUnsyncedIngresso.visibility = View.GONE
            }
            initializeMenu()
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun initializeMenu() {
        try {
            val menuRecyclerView: RecyclerView = findViewById(R.id.menuItems)
            menuRecyclerView.layoutManager = GridLayoutManager(this, 2)

            val menu = mapOf(
                "type" to "card",
                "data" to listOf(
                    mapOf(
                        "id" to 1,
                        "navigateTo" to "DispatchActivity",
                        "visible" to dispatchModule,
                        "title" to "Dispatch",
                        "subTitle" to "",
                        "icon" to R.drawable.appicon_dispatch
                    ),
                    mapOf(
                        "id" to 2,
                        "navigateTo" to "RemittanceActivity",
                        "visible" to remittanceModule,
                        "title" to "Partial Remit",
                        "subTitle" to "",
                        "icon" to R.drawable.appicon_money
                    ),
                    mapOf(
                        "id" to 3,
                        "navigateTo" to "TicketReceiptActivity",
                        "visible" to ticketReceiptModule,
                        "title" to "Ticketing",
                        "subTitle" to "",
                        "icon" to R.drawable.appicon_ticket
                    ),
                    mapOf(
                        "id" to 4,
                        "navigateTo" to "ReverseTripActivity",
                        "visible" to reverseTripModule,
                        "title" to "Reverse Trip",
                        "subTitle" to "",
                        "icon" to R.drawable.appicon_reverse
                    ),
                    mapOf(
                        "id" to 5,
                        "navigateTo" to "InspectionActivity",
                        "visible" to inspectionModule,
                        "title" to "Inspection",
                        "subTitle" to "",
                        "icon" to R.drawable.appicon_inspection
                    ),
                    mapOf(
                        "id" to 6,
                        "navigateTo" to "TripReportActivity",
                        "visible" to tripReportModule,
                        "title" to "Trip Report",
                        "subTitle" to "",
                        "icon" to R.drawable.appicon_report
                    ),
                    mapOf(
                        "id" to 7,
                        "navigateTo" to "ConfigurationActivity",
                        "visible" to configurationModule,
                        "title" to "Configurations",
                        "subTitle" to "",
                        "icon" to R.drawable.appicon_configurations
                    ),
                    mapOf(
                        "id" to 8,
                        "navigateTo" to "MPADPayRegistrationActivity",
                        "visible" to true,
                        "title" to "MPAD Pay",
                        "subTitle" to "",
                        "icon" to R.drawable.appicon_mpadpay
                    ),
                    mapOf(
                        "id" to 0,
                        "navigateTo" to "MainActivity",
                        "visible" to true,
                        "title" to "Exit",
                        "subTitle" to "",
                        "icon" to R.drawable.appicon_exit
                    )
                )
            )

            val adapter = MenuRecyclerViewAdapter(applicationContext, menu)
            adapter.recyclerViewListener = this
            menuRecyclerView.adapter = adapter
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

        try {
            if (navigateTo == "MainActivity") {
                //startActivity(Intent(this, MainActivity::class.java))
                _tmpModuleId = 100
                dialogAuth = DialogAuthentication.newInstance(_tmpModuleId)
                dialogAuth.show(supportFragmentManager, "DialogForm")
                dialogAuth.dialogEventListener = this
            }
            if (navigateTo == "DispatchActivity") {
                _tmpModuleId = 1
                dialogAuth = DialogAuthentication.newInstance(_tmpModuleId)
                dialogAuth.show(supportFragmentManager, "DialogForm")
                dialogAuth.dialogEventListener = this
            }
            if (navigateTo == "RemittanceActivity") {
                _tmpModuleId = 2
                dialogAuth = DialogAuthentication.newInstance(_tmpModuleId)
                dialogAuth.show(supportFragmentManager, "DialogForm")
                dialogAuth.dialogEventListener = this
            }
            if (navigateTo == "TicketReceiptActivity") {
                _tmpModuleId = 3
                dialogAuth = DialogAuthentication.newInstance(_tmpModuleId)
                dialogAuth.show(supportFragmentManager, "DialogForm")
                dialogAuth.dialogEventListener = this
            }
            if (navigateTo == "ReverseTripActivity") {
                _tmpModuleId = 4
                dialogAuth = DialogAuthentication.newInstance(_tmpModuleId)
                dialogAuth.show(supportFragmentManager, "DialogForm")
                dialogAuth.dialogEventListener = this
            }
            if (navigateTo == "InspectionActivity") {
                _tmpModuleId = 5
                dialogAuth = DialogAuthentication.newInstance(_tmpModuleId)
                dialogAuth.show(supportFragmentManager, "DialogForm")
                dialogAuth.dialogEventListener = this
            }
            if (navigateTo == "TripReportActivity") {
                _tmpModuleId = 6
                dialogAuth = DialogAuthentication.newInstance(_tmpModuleId)
                dialogAuth.show(supportFragmentManager, "DialogForm")
                dialogAuth.dialogEventListener = this
            }
            if (navigateTo == "ConfigurationActivity") {
                _tmpModuleId = 7
                dialogAuth = DialogAuthentication.newInstance(_tmpModuleId)
                dialogAuth.show(supportFragmentManager, "DialogForm")
                dialogAuth.dialogEventListener = this
            }
            if (navigateTo == "MPADPayRegistrationActivity") {
                _tmpModuleId = 8
                dialogAuth = DialogAuthentication.newInstance(_tmpModuleId)
                dialogAuth.show(supportFragmentManager, "DialogForm")
                dialogAuth.dialogEventListener = this
            }
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRecyclerItemLongClicked(
        id: Int,
        position: Int,
        title: String,
        subTitle: String,
        navigateTo: String
    ) {

    }




    override fun onDialogCompleted(data: Any?) {
        if (data is UserModel) {
            if (_tmpModuleId == 1) {
                viewModelMain.sharedPrefs.setInt("sharedDispatcherId", data.id)
                startActivity(Intent(this, DispatchActivity::class.java))
            }
            if (_tmpModuleId == 2) {
                viewModelMain.sharedPrefs.setInt("sharedCashierId", data.id)
                startActivity(Intent(this, RemittanceActivity::class.java))
            }
            if (_tmpModuleId == 3) {
                viewModelMain.sharedPrefs.setInt("sharedConductorId", data.id)
                startActivity(Intent(this, TicketReceiptActivity::class.java))
            }
            if (_tmpModuleId == 4) {
                viewModelMain.sharedPrefs.setInt("sharedReversedById", data.id)
                startActivity(Intent(this, ReverseTripActivity::class.java))
            }
            if (_tmpModuleId == 5) {
                viewModelMain.sharedPrefs.setInt("sharedInspectorId", data.id)
                startActivity(Intent(this, InspectionActivity::class.java))
            }
            if (_tmpModuleId == 6) {
                //viewModelMain.sharedPrefs.setInt("sharedConductorId",data.id)
                val dispatchReference =
                    viewModelDispatch.sharedPrefs.getInt("sharedDeviceLastDispatchId", 0)
                val receipts =
                    viewModelReceipt.getAllReceiptWithNamesByDispatchReferenceId(dispatchReference)
                val dispatch = viewModelDispatch.getDispatchByReferenceId(dispatchReference)

                val filteredReceipts: List<TicketReceiptWithNameModel>
                if (data.roleId == 6) {
                    filteredReceipts = receipts.filter { it.conductorId == data.id }
                } else {
                    filteredReceipts = receipts
                }


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
            }
            if (_tmpModuleId == 7) {
                viewModelMain.sharedPrefs.setInt("sharedAdministratorId", data.id)
                startActivity(Intent(this, ConfigurationActivity::class.java))
            }
            if (_tmpModuleId == 8) {
                viewModelMain.sharedPrefs.setInt("sharedMPADPayPersonnelId", data.id)
                startActivity(Intent(this, MPADPayActivity::class.java))
            }
            if (_tmpModuleId == 100) {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
        }
    }
}