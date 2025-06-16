package com.wneth.mpadtransport.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.wneth.mpadtransport.databinding.ActivityRemittanceBinding
import com.wneth.mpadtransport.models.RemittanceModel
import com.wneth.mpadtransport.models.TerminalModel
import com.wneth.mpadtransport.utilities.DrawableBox
import com.wneth.mpadtransport.utilities.amountToPHP
import com.wneth.mpadtransport.utilities.validatedDeviceName
import com.wneth.mpadtransport.utilities.dialogs.DialogRecyclerItems
import com.wneth.mpadtransport.utilities.interfaces.PrinterEventListener
import com.wneth.mpadtransport.utilities.interfaces.RecyclerDialogEventListener
import com.wneth.mpadtransport.utilities.interfaces.SearchFragmentListener
import com.wneth.mpadtransport.utilities.printer.Printer
import com.wneth.mpadtransport.utilities.printer.templates.generateRemittanceTemplate
import com.wneth.mpadtransport.utilities.setVisibility
import com.wneth.mpadtransport.utilities.showCustomToast
import com.wneth.mpadtransport.utilities.writeLogToSDCard
import com.wneth.mpadtransport.viewmodels.RemittanceActivityViewModel
import com.wneth.mpadtransport.views.configurations.RemittanceDataActivity
import com.wneth.mpadtransport.views.configurations.TicketReceiptDataActivity


class RemittanceActivity : AppCompatActivity(), SearchFragmentListener, RecyclerDialogEventListener,
    PrinterEventListener {

    private lateinit var viewBinding: ActivityRemittanceBinding
    private lateinit var viewModel: RemittanceActivityViewModel
    private lateinit var recyclerDialog: DialogRecyclerItems
    private lateinit var printer: Printer

    private lateinit var signaturePad: DrawableBox
    private var _tmpTerminalId: Int = 0
    private var _tmpTotalRevenue: Double = 0.0
    private var _tmpUremittedAmount: Double = 0.0
    private var _tmpRemittedAmount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize View Binding
        viewBinding = ActivityRemittanceBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[RemittanceActivityViewModel::class.java]

        try {
            printer = Printer(this)
            printer.initPrinter()
        }catch (ex: Exception){

        }


        signaturePad = viewBinding.signaturePad

        // Event Listener
        viewBinding.btnSetTerminal.setOnClickListener {
            recyclerDialog = DialogRecyclerItems.newInstance(2)
            recyclerDialog.show(supportFragmentManager, "DialogForm")
            recyclerDialog.recyclerEventListener = this
        }

        viewBinding.btnShowReceipts.setOnClickListener {
            startActivity(Intent(this, TicketReceiptDataActivity::class.java))
        }

        viewBinding.btnClearSignature.setOnClickListener {
            signaturePad.clear()
        }

        viewBinding.btnContinueRemit.setOnClickListener {
            submitRemittance()
        }

        viewBinding.btnViewRemittance.setOnClickListener {
            startActivity(Intent(this, RemittanceDataActivity::class.java))
        }
        initializeLatestDispatchRemittances()
        initializeSettings()
    }

    private fun initializeSettings() {
        try {
            val deviceSettings = viewModel.deviceSettings()!!
            setVisibility(viewBinding.btnShowReceipts, deviceSettings.showRemittanceViewTickets)
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun initializeLatestDispatchRemittances() {
        try {
            viewBinding.txtRemitAmount.setText("0.00")
            val dispatchId = viewModel.sharedPrefs.getInt("sharedDeviceLastDispatchId", 0)
            val remittance = viewModel.computeRemittancesByDispatchId(dispatchId);
            _tmpTotalRevenue = remittance.totalRevenue ?: 0.00
            _tmpUremittedAmount = remittance.totalUnremitted ?: 0.00
            _tmpRemittedAmount = remittance.totalRemitted ?: 0.00

            viewBinding.txtTotalRevenue.text = _tmpTotalRevenue.amountToPHP()
            viewBinding.txtTotalRemitted.text = _tmpRemittedAmount.amountToPHP()
            viewBinding.txtTotalUnremitted.text = _tmpUremittedAmount.amountToPHP()
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

    private fun submitRemittance() {
        try {

            val remitAmount = viewBinding.txtRemitAmount.text

            if (_tmpTerminalId == 0) {
                Toast(this).showCustomToast("Please select a terminal", this, Toast.LENGTH_SHORT)
                return
            }

            if (!signaturePad.hasDrawing()) {
                Toast(this).showCustomToast(
                    "Please attach your signature",
                    this,
                    Toast.LENGTH_SHORT
                )
                return
            }

            if (remitAmount.isEmpty() || remitAmount.toString()
                    .toDouble() <= 0 || remitAmount.toString().toDouble() > _tmpUremittedAmount
            ) {
                Toast(this).showCustomToast("Invalid remit amount", this, Toast.LENGTH_SHORT)
                return
            }


            val signatureByteArray = signaturePad.getSignatureAsByteArray()
            val base64Signature = RemittanceModel.fromByteArray(signatureByteArray)
            val deviceName = validatedDeviceName(this@RemittanceActivity);
            val remittance = RemittanceModel(
                id = 0,
                deviceName = deviceName,
                companyId = viewModel.sharedPrefs.getInt("sharedCompanyId", 0),
                dispatchReferenceId = viewModel.sharedPrefs.getInt("sharedDeviceLastDispatchId", 0),
                terminalId = _tmpTerminalId,
                remittedById = viewModel.sharedPrefs.getInt("sharedConductorId", 0),
                receivedById = viewModel.sharedPrefs.getInt("sharedCashierId", 0),
                signature = base64Signature,
                amount = remitAmount.toString().toDouble()
            )
            viewModel.insertRemittance(remittance)
            printRemittedDetails()
            initializeLatestDispatchRemittances()
            signaturePad.clear()
            Toast(this).showCustomToast("Remittance submitted", this, Toast.LENGTH_SHORT)
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun printRemittedDetails() {
        try {
            viewBinding.btnContinueRemit.isEnabled = false
            val dispatchId = viewModel.sharedPrefs.getInt("sharedDeviceLastDispatchId", 0)

            val dispatch = viewModel.dispatchRepository.getDispatchByReferenceId(dispatchId)
            val remittance = viewModel.getRemittancesByDispatchId(dispatchId)
            if (dispatch != null && remittance.isNotEmpty()) {
                val template = generateRemittanceTemplate(this, dispatch, remittance.last())
                printer.printTemplate(template)
            } else {

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
        viewBinding.btnContinueRemit.isEnabled = true
    }

    override fun onPrinterBusy() {
        viewBinding.btnContinueRemit.isEnabled = false
    }
}