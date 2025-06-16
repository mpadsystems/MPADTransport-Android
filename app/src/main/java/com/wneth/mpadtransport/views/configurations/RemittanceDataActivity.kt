package com.wneth.mpadtransport.views.configurations

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wneth.mpadtransport.R
import com.wneth.mpadtransport.databinding.ActivityRemittanceDataBinding
import com.wneth.mpadtransport.utilities.amountToPHP
import com.wneth.mpadtransport.utilities.formatToHumanDateTime
import com.wneth.mpadtransport.utilities.interfaces.PrinterEventListener
import com.wneth.mpadtransport.utilities.interfaces.RecyclerDialogEventListener
import com.wneth.mpadtransport.utilities.printer.Printer
import com.wneth.mpadtransport.utilities.recyclerviews.MenuRecyclerViewAdapter
import com.wneth.mpadtransport.viewmodels.RemittanceActivityViewModel

class RemittanceDataActivity : AppCompatActivity(), RecyclerDialogEventListener, PrinterEventListener {

    // >>Variables Declarations
    /*
    * Code Below
    */

    private lateinit var viewModel: RemittanceActivityViewModel
    private lateinit var viewBinding: ActivityRemittanceDataBinding
    private var _tmpDispatchId: Int = 0
    private lateinit var printer: Printer
    // =================================



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewModel
        /*
        * Code Below
        */
        viewModel = ViewModelProvider(this)[RemittanceActivityViewModel::class.java]
        // =================================


        // Initialize viewBinding
        /*
        * Code Below
        */
        viewBinding = ActivityRemittanceDataBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        // =================================

        printer = Printer(this,this)
        printer.initPrinter()

        populateRemittance()

        _tmpDispatchId = viewModel.sharedPrefs.getInt("sharedDeviceLastDispatchId", 0)
        /*viewBinding.fabPrint.setOnClickListener { view ->
            viewBinding.fabPrint.isEnabled = false
            val dispatch = viewModel.dispatchRepository.getDispatchByReferenceId(_tmpDispatchId)
            val remittance = viewModel.getRemittancesByDispatchId(_tmpDispatchId)
            if (dispatch != null && remittance.isNotEmpty()) {
                val template = generateRemittanceTemplate(this, dispatch, remittance.last())
                printer.printTemplate(template)
            }else{
                Toast(this).showCustomToast("No remittance found",this, Toast.LENGTH_SHORT)
            }
        }*/
    }



    private fun populateRemittance() {
        val dispatchId = viewModel.sharedPrefs.getInt("sharedDeviceLastDispatchId",0)
        val remittances = viewModel.getRemittancesByDispatchId(dispatchId)


        val menuRecyclerView: RecyclerView = findViewById(R.id.recyclerItems);
        val manager = GridLayoutManager(this, 1)
        menuRecyclerView.layoutManager = manager

        val data: List<Map<String, Any?>> = if (remittances.isEmpty()) {
            listOf(
                mapOf(
                    "id" to "",
                    "navigateTo" to "",
                    "visible" to true,
                    "title" to "Oops! No Data Found",
                    "subTitle" to "No remittance submitted yet",
                    "icon" to R.drawable.appicon_carcrash
                )
            )
        } else {
            remittances.map { remittance ->
                mapOf(
                    "id" to remittance.id,
                    "navigateTo" to "",
                    "visible" to true,
                    "title" to "${remittance.terminalName} Terminal",
                    "subTitle" to "Amount: ${remittance.remittedAmount?.amountToPHP()}\n" +
                            "Remitted By: ${remittance.remittedByName}\n" +
                            "Received By: ${remittance.receivedByName}\n" +
                            "Date: ${remittance.dateCreated.formatToHumanDateTime()}\n",
                    "icon" to null
                )
            }
        }



        val menu = mapOf(
            "type" to "list",
            "data" to data
        )

        val adapter = MenuRecyclerViewAdapter(applicationContext, menu)
        adapter.recyclerViewListener = this
        menuRecyclerView.adapter = adapter
    }


    override fun onRecyclerItemClicked(id: Int, position: Int, title: String, subTitle: String, navigateTo: String, data: Any?) {
        // To be implemented
    }

    override fun onRecyclerItemLongClicked(id: Int, position: Int, title: String, subTitle: String, navigateTo: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle("Select Action")
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .setItems(arrayOf("Delete", "Disable", "Enable")) { dialog, which ->
                when (which) {
                    0 -> {
                        Toast.makeText(this, "Deleted", Toast.LENGTH_LONG).show()
                    }
                    1 -> {
                        Toast.makeText(this, "Disabled", Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        Toast.makeText(this, "Enabled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    override fun onPrinterNormal() {
        //viewBinding.fabPrint.isEnabled = true
    }

    override fun onPrinterBusy() {
        //viewBinding.fabPrint.isEnabled = false
    }

}