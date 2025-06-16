package com.wneth.mpadtransport.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wneth.mpadtransport.R
import com.wneth.mpadtransport.databinding.ActivityConfigurationBinding
import com.wneth.mpadtransport.utilities.apiUrl
import com.wneth.mpadtransport.utilities.dialogs.DialogProgress
import com.wneth.mpadtransport.utilities.interfaces.RecyclerDialogEventListener
import com.wneth.mpadtransport.utilities.recyclerviews.MenuRecyclerViewAdapter
import com.wneth.mpadtransport.utilities.showCustomToast
import com.wneth.mpadtransport.utilities.writeLogToSDCard
import com.wneth.mpadtransport.viewmodels.ConfigurationActivityViewModel
import com.wneth.mpadtransport.views.configurations.BusActivity
import com.wneth.mpadtransport.views.configurations.CompanyActivity
import com.wneth.mpadtransport.views.configurations.DiscountActivity
import com.wneth.mpadtransport.views.configurations.DispatchDataActivity
import com.wneth.mpadtransport.views.configurations.EmployeeActivity
import com.wneth.mpadtransport.views.configurations.FareActivity
import com.wneth.mpadtransport.views.configurations.HotspotActivity
import com.wneth.mpadtransport.views.configurations.IngressoActivity
import com.wneth.mpadtransport.views.configurations.RemittanceDataActivity
import com.wneth.mpadtransport.views.configurations.RouteActivity
import com.wneth.mpadtransport.views.configurations.TerminalActivity
import com.wneth.mpadtransport.views.configurations.TicketReceiptDataActivity
import kotlinx.coroutines.launch

class ConfigurationActivity : AppCompatActivity(), RecyclerDialogEventListener {


    private lateinit var viewModel: ConfigurationActivityViewModel
    private lateinit var viewBinding: ActivityConfigurationBinding
    private lateinit var dialogProgress: DialogProgress

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ConfigurationActivityViewModel::class.java]

        // Initialize Binding
        viewBinding = ActivityConfigurationBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Initialize UI
        dialogProgress = DialogProgress(this)


        initializeMenu()
    }

    private fun initializeMenu() {
        try {
            val menuRecyclerView: RecyclerView = findViewById(R.id.menuItems)
            menuRecyclerView.layoutManager = GridLayoutManager(this, 1)

            val menu = mapOf(
                "type" to "list",
                "data" to listOf(
                    mapOf(
                        "id" to 0,
                        "navigateTo" to "CompanyActivity",
                        "visible" to true,
                        "title" to "Company",
                        "subTitle" to "Company Information",
                        "icon" to R.drawable.appicon_company
                    ),
                    mapOf(
                        "id" to 0,
                        "navigateTo" to "TicketReceiptDataActivity",
                        "visible" to true,
                        "title" to "Receipts/Tickets",
                        "subTitle" to "Tickets Data",
                        "icon" to R.drawable.appicon_ticket
                    ),
                    mapOf(
                        "id" to 0,
                        "navigateTo" to "RemittanceDataActivity",
                        "visible" to true,
                        "title" to "Remittances",
                        "subTitle" to "Partial Remits",
                        "icon" to R.drawable.appicon_remittance
                    ),
                    mapOf(
                        "id" to 0,
                        "navigateTo" to "IngressoActivity",
                        "visible" to true,
                        "title" to "Ingresso",
                        "subTitle" to "Accounting Management",
                        "icon" to R.drawable.appicon_ingresso
                    ),
                    mapOf(
                        "id" to 0,
                        "navigateTo" to "DispatchDataActivity",
                        "visible" to true,
                        "title" to "Dispatch",
                        "subTitle" to "Dispatch Data",
                        "icon" to R.drawable.appicon_dispatch
                    ),
                    mapOf(
                        "id" to 0,
                        "navigateTo" to "EmployeeActivity",
                        "visible" to true,
                        "title" to "Employees",
                        "subTitle" to "Employee & Account Management",
                        "icon" to R.drawable.appicon_employee
                    ),
                    mapOf(
                        "id" to 0,
                        "navigateTo" to "HotspotActivity",
                        "visible" to true,
                        "title" to "Hotspots",
                        "subTitle" to "Hotspots",
                        "icon" to R.drawable.appicon_router
                    ),
                    mapOf(
                        "id" to 0,
                        "navigateTo" to "FareActivity",
                        "visible" to true,
                        "title" to "Fares",
                        "subTitle" to "Fare Setup",
                        "icon" to R.drawable.appicon_money
                    ),
                    mapOf(
                        "id" to 0,
                        "navigateTo" to "BusActivity",
                        "visible" to true,
                        "title" to "Buses",
                        "subTitle" to "Bus List",
                        "icon" to R.drawable.appicon_bus
                    ),
                    mapOf(
                        "id" to 0,
                        "navigateTo" to "RouteActivity",
                        "visible" to true,
                        "title" to "Routes",
                        "subTitle" to "Routes Information",
                        "icon" to R.drawable.appicon_map
                    ),
                    mapOf(
                        "id" to 0,
                        "navigateTo" to "TerminalActivity",
                        "visible" to true,
                        "title" to "Terminals",
                        "subTitle" to "Terminal Locations",
                        "icon" to R.drawable.appicon_terminal
                    ),
                    mapOf(
                        "id" to 0,
                        "navigateTo" to "DiscountActivity",
                        "visible" to true,
                        "title" to "Discounts",
                        "subTitle" to "Discount Information",
                        "icon" to R.drawable.appicon_discount
                    ),
                    mapOf(
                        "id" to 0,
                        "navigateTo" to "SettingsActivity",
                        "visible" to false,
                        "title" to "Device Settings",
                        "subTitle" to "Sync/Backup Settings",
                        "icon" to R.drawable.appicon_device
                    ),
                    mapOf(
                        "id" to 0,
                        "navigateTo" to "UpdateOptionActivity",
                        "visible" to true,
                        "title" to "Update Options",
                        "subTitle" to "Update Device Data & Application",
                        "icon" to R.drawable.appicon_sync
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
        if (navigateTo == "EmployeeActivity") {
            startActivity(Intent(this, EmployeeActivity::class.java))
        }
        if (navigateTo == "CompanyActivity") {
            startActivity(Intent(this, CompanyActivity::class.java))
        }
        if (navigateTo == "BusActivity") {
            startActivity(Intent(this, BusActivity::class.java))
        }
        if (navigateTo == "TerminalActivity") {
            startActivity(Intent(this, TerminalActivity::class.java))
        }
        if (navigateTo == "DiscountActivity") {
            startActivity(Intent(this, DiscountActivity::class.java))
        }
        if (navigateTo == "RouteActivity") {
            startActivity(Intent(this, RouteActivity::class.java))
        }
        if (navigateTo == "DispatchDataActivity") {
            startActivity(Intent(this, DispatchDataActivity::class.java))
        }
        if (navigateTo == "FareActivity") {
            startActivity(Intent(this, FareActivity::class.java))
        }
        if (navigateTo == "UpdateOptionActivity") {
            showSyncDeviceDialog()
        }
        if (navigateTo == "HotspotActivity") {
            startActivity(Intent(this, HotspotActivity::class.java))
        }
        if (navigateTo == "TicketReceiptDataActivity") {
            startActivity(Intent(this, TicketReceiptDataActivity::class.java))
        }
        if (navigateTo == "RemittanceDataActivity") {
            startActivity(Intent(this, RemittanceDataActivity::class.java))
        }
        if (navigateTo == "IngressoActivity") {
            startActivity(Intent(this, IngressoActivity::class.java))
        }
    }

    private fun showSyncDeviceDialog() {
        try {
            val options =
                arrayOf("Sync Ingresso", "Update Device Data", "Update MPAD", "Backup Database")

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Choose sync option")

            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        dialogProgress.showProgress("")
                        viewModel.viewModelScope.launch {
                            val response = viewModel.syncIngresso()
                            if (response == 1) {
                                Toast(this@ConfigurationActivity).showCustomToast(
                                    "Ingresso synced successfully",
                                    this@ConfigurationActivity,
                                    Toast.LENGTH_SHORT
                                )
                                val page = Intent(
                                    this@ConfigurationActivity,
                                    InitialSetupActivity::class.java
                                )
                                page.putExtra("reSync", true)
                                startActivity(page)
                            } else if (response == 0) {
                                Toast(this@ConfigurationActivity).showCustomToast(
                                    "Ingresso already synced",
                                    this@ConfigurationActivity,
                                    Toast.LENGTH_SHORT
                                )
                            } else {
                                Toast(this@ConfigurationActivity).showCustomToast(
                                    "Error syncing Ingresso",
                                    this@ConfigurationActivity,
                                    Toast.LENGTH_SHORT
                                )
                            }
                            dialogProgress.dismissProgress()
                        }
                    }

                    1 -> {
                        startActivity(Intent(this, InitialSetupActivity::class.java))
                    }

                    2 -> {
                        val companyId = viewModel.sharedPrefs.getInt("sharedCompanyId", 0)
                        val webUrl =
                            "${apiUrl}/File/DownloadFile?token=${viewModel.sharedApiToken}&companyId=${companyId}&fileName=mpad-transport-apk"
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
                        this.startActivity(browserIntent)
                    }

                    3 -> {

                    }
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
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

}