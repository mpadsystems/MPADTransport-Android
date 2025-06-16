package com.wneth.mpadtransport.views

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.wneth.mpadtransport.R
import com.wneth.mpadtransport.databinding.ActivityTicketReceiptBinding
import com.wneth.mpadtransport.models.DiscountModel
import com.wneth.mpadtransport.models.RouteSegmentModel
import com.wneth.mpadtransport.models.TicketPaymentDataModel
import com.wneth.mpadtransport.models.TicketReceiptWithNameModel
import com.wneth.mpadtransport.utilities.addTextChangedListener
import com.wneth.mpadtransport.utilities.amountToPHP
import com.wneth.mpadtransport.utilities.validatedDeviceName
import com.wneth.mpadtransport.utilities.dialogs.DialogPayment
import com.wneth.mpadtransport.utilities.dialogs.DialogProgress
import com.wneth.mpadtransport.utilities.dialogs.DialogRecyclerItems
import com.wneth.mpadtransport.utilities.dpToPx
import com.wneth.mpadtransport.utilities.formatId
import com.wneth.mpadtransport.utilities.generateISODateTime
import com.wneth.mpadtransport.utilities.getRouteDirectionName
import com.wneth.mpadtransport.utilities.interfaces.PaymentDialogEventListener
import com.wneth.mpadtransport.utilities.interfaces.PrinterEventListener

import com.wneth.mpadtransport.utilities.interfaces.RecyclerDialogEventListener
import com.wneth.mpadtransport.utilities.interfaces.SearchFragmentListener
import com.wneth.mpadtransport.utilities.printer.Printer
import com.wneth.mpadtransport.utilities.printer.templates.generateReceiptTemplate
import com.wneth.mpadtransport.utilities.reverseStringParts
import com.wneth.mpadtransport.utilities.setVisibility
import com.wneth.mpadtransport.utilities.showCustomToast
import com.wneth.mpadtransport.utilities.writeLogToSDCard
import com.wneth.mpadtransport.viewmodels.MPADPayActivityViewModel
import com.wneth.mpadtransport.viewmodels.TicketReceiptActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TicketReceiptActivity : AppCompatActivity(),
    SearchFragmentListener, RecyclerDialogEventListener,
    PaymentDialogEventListener, PrinterEventListener {

    private lateinit var activityLauncher: ActivityResultLauncher<Intent>
    private var cashlessPaymentEnabled = false
    private var ticketQREnabled = false
    private var BaggageEnabled = false
    private var baggageSelected = false
    private var baggageAmount = 0.00

    private var printerBusy = false;

    private var receiptTemplate = ""

    private var paymentReferenceNumber: String = ""
    private var paymentType = 1;
    private var mpadPayBalance:Double = 0.00

    private lateinit var dialog: DialogRecyclerItems
    private lateinit var dialogPayment: DialogPayment

    private lateinit var viewModel: TicketReceiptActivityViewModel
    private lateinit var viewModelMPADPay: MPADPayActivityViewModel
    private lateinit var viewBinding: ActivityTicketReceiptBinding
    private lateinit var printer: Printer
    private lateinit var discounts: List<DiscountModel>

    private var _tmpCurrentReceiptId: Int = 0
    private var _tmpRouteLastKM: Int = 0
    private var _tmpHotspotAmount: Double = 0.0
    private var _tmpDirectionId: Int = 0
    private var _tmpHasFromSegment: Boolean = false
    private var _tmpHasToSegment: Boolean = false

    // For Receipt
    private var _tmpDriverId: Int = 0
    private var _tmpDriverName: String = ""
    private var _tmpConductorId: Int = 0
    private var _tmpConductorName: String = ""
    private var _tmpBusId: Int = 0
    private var _tmpBusNumber: Int = 0
    private var _tmpBusPlateNumber: String = ""
    private var _tmpRouteId: Int = 0
    private var _tmpDispatchId: Int = 0
    private var _tmpDispatchTripId: Int = 0
    private var _tmpRouteName: String = ""
    private var _tmpTotalDiscountedFare: Double = 0.0


    private var _tmpFromKM: Int = 0
    private var _tmpToKM: Int = 0
    private var _tmpFromSegmentId: Int = 0
    private var _tmpFromSegmentName: String = ""
    private var _tmpToSegmentId: Int = 0
    private var _tmpToSegmentName: String = ""
    private var _segmentFor: String = ""
    private lateinit var _routeSegmentKms: List<Int>
    private var segmentFromIndex = 0
    private var segmentToIndex = 0


    // Used to compute fare
    private var _tmpDiscountAmount: Int = 0
    private var _tmpDiscountId: Int = 0
    private var _tmpDiscountName: String = ""
    private var _tmpTotalKM: Int = 0

    private var errorEncountered = false
    private lateinit var dialogProgress: DialogProgress


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Binding
        viewBinding = ActivityTicketReceiptBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[TicketReceiptActivityViewModel::class.java]
        viewModelMPADPay = ViewModelProvider(this)[MPADPayActivityViewModel::class.java]

        // Initialize Printer
        try {
            printer = Printer(this, this)
            printer.initPrinter()
        }catch (ex: Exception){

        }

        dialogProgress = DialogProgress(this)

        // Initialize current receipt id
        _tmpCurrentReceiptId = viewModel.sharedPrefs.getInt("sharedDeviceLastReceiptId", 0)
        _tmpDispatchTripId = viewModel.sharedPrefs.getInt("sharedDeviceLastDispatchTripId", 0)

        //Discounts
        discounts = viewModel.getDiscounts()


        _tmpDiscountName = discounts[0].name
        initializeSettings()
        initializeDiscounts()


        // Initialize Events
        viewBinding.apply {
            btnFromPrev.setOnClickListener { handlePrevButtonClick("FROM") }
            btnFromNext.setOnClickListener { handleNextButtonClick("FROM") }
            btnToPrev.setOnClickListener { handlePrevButtonClick("TO") }
            btnToNext.setOnClickListener { handleNextButtonClick("TO") }
        }


        addTextChangedListener(viewBinding.txtFromKM, "FROM") { value ->
            handleTxtKMChanged("FROM", value)
        }

        addTextChangedListener(viewBinding.txtToKM, "TO") { value ->
            handleTxtKMChanged("TO", value)
        }

        /*viewBinding.txtFromKM.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewBinding.txtFromKM.text.clear()
                handleTxtKMChanged("FROM", "")
            }
        }

        viewBinding.txtToKM.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewBinding.txtToKM.text.clear()
                handleTxtKMChanged("TO", "")
            }
        }*/

        viewBinding.txtFromKM.apply {
            setOnClickListener {
                text.clear()
            }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(windowToken, 0)
                    true
                } else {
                    false
                }
            }
            handleTxtKMChanged("FROM", "")
        }


        viewBinding.txtToKM.apply {
            setOnClickListener {
                text.clear()
            }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(windowToken, 0)
                } else {
                    false
                }
            }
            handleTxtKMChanged("TO", "")
        }



        viewBinding.txtFromSegment.setOnClickListener {
            _segmentFor = "FROM"
            showRouteSegments()
        }

        viewBinding.txtToSegment.setOnClickListener {
            _segmentFor = "TO"
            showRouteSegments()
        }

        viewBinding.btnPrint.setOnClickListener {
            viewBinding.btnPrint.isEnabled = false
            if (cashlessPaymentEnabled) {
                proceedPayment()
            } else {
                printReceipt()
            }
        }


        activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val transactionCode = result.data?.getStringExtra("TransactionCode") ?: "Error"

                if (transactionCode != "Cancelled" && transactionCode != "Error"){
                    lifecycleScope.launch {
                        performMPADPayTransaction(transactionCode)
                    }
                }else{
                    Toast(this).showCustomToast("Invalid QRCode", this, Toast.LENGTH_LONG)
                }
            }
        }

        initializeDispatchRoute()
        initializePreviousReceipts()
    }





    private fun initializeSettings() {
        try {
            val deviceSettings = viewModel.deviceSettings()!!
            setVisibility(viewBinding.tLCashGross, deviceSettings.showTicketingCashGross)
            setVisibility(viewBinding.tLCashGross, deviceSettings.showTicketingCashGross)
            cashlessPaymentEnabled = deviceSettings.showTicketingCashlessPayment

            ticketQREnabled = deviceSettings.showTicketingQR
            BaggageEnabled = deviceSettings.showTicketingBaggage

            if (BaggageEnabled) {
                val baggage = DiscountModel(
                    id = 0,
                    companyId = 0,
                    name = "BAG",
                    description = "Baggage Ticket",
                    fraction = 0,
                    isActive = true,
                    dateCreated = generateISODateTime()
                )
                discounts = discounts + baggage
            }

            receiptTemplate = deviceSettings.receiptTemplate
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            errorEncountered = true
        }
    }


    private fun showRouteSegments() {
        try {
            dialog = DialogRecyclerItems.newInstance(7, _tmpRouteId)
            dialog.show(supportFragmentManager, "DialogForm")
            dialog.recyclerEventListener = this
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            errorEncountered = true
        }
    }


    private fun initializeDispatchRoute() {
        try {

            val dispatchTrip = viewModel.getLatestDispatchTrip()
            dispatchTrip.let { trip ->
                _tmpDriverId = trip?.driverId ?: 0
                _tmpDriverName = trip?.driverName ?: ""
                _tmpConductorId = trip?.conductorId ?: 0
                _tmpConductorName = trip?.conductorName ?: ""
                _tmpBusId = trip?.busId ?: 0
                _tmpBusNumber = trip?.busNumber ?: 0
                _tmpBusPlateNumber = trip?.busPlateNumber ?: ""
                _tmpRouteId = trip?.routeId ?: 0
                _tmpDirectionId = trip?.directionId ?: 0
                _tmpDispatchId = trip?.dispatchReferenceId ?: 0

                _tmpRouteName = if (trip?.directionId == 1) {
                    trip.routeName ?: ""
                } else {
                    reverseStringParts(trip?.routeName ?: "")
                }
                viewBinding.lblRoute.text =
                    "${getRouteDirectionName(trip?.directionId ?: 1)}: ${_tmpRouteName}"

                _routeSegmentKms = viewModel.getRouteSegmentKmsByRouteId(_tmpRouteId)
                _tmpRouteLastKM = _routeSegmentKms[_routeSegmentKms.size - 1]

                // If SouthBound / Else Northbound
                if (_tmpDirectionId == 1) {
                    segmentFromIndex = _routeSegmentKms.indexOf(_routeSegmentKms[0])
                    segmentToIndex = _routeSegmentKms.indexOf(_routeSegmentKms[1])

                    _tmpFromKM = _routeSegmentKms[segmentFromIndex]
                    _tmpToKM = _routeSegmentKms[segmentToIndex]
                    viewBinding.txtFromKM.setText(_tmpFromKM.toString())
                    viewBinding.txtToKM.setText(_tmpToKM.toString())
                    _tmpFromSegmentId = getSegmentInfo(_tmpFromKM, "FROM")?.id ?: 0
                    _tmpToSegmentId = getSegmentInfo(_tmpToKM, "TO")?.id ?: 0
                    _tmpFromSegmentName = getSegmentInfo(_tmpFromKM, "FROM")?.name ?: ""
                    _tmpToSegmentName = getSegmentInfo(_tmpToKM, "TO")?.name ?: ""
                    viewBinding.txtFromSegment.text = _tmpFromSegmentName
                    viewBinding.txtToSegment.text = _tmpToSegmentName

                    handleTxtKMChanged("FROM", _tmpFromKM.toString())
                    handleTxtKMChanged("TO", _tmpToKM.toString())
                } else {
                    segmentFromIndex = _routeSegmentKms.indexOf(_tmpRouteLastKM)
                    segmentToIndex =
                        _routeSegmentKms.indexOf(_routeSegmentKms[_routeSegmentKms.size - 2])

                    _tmpFromKM = _tmpRouteLastKM
                    _tmpToKM = _routeSegmentKms[_routeSegmentKms.size - 2]
                    viewBinding.txtFromKM.setText(_tmpFromKM.toString())
                    viewBinding.txtToKM.setText(_tmpToKM.toString())
                    _tmpFromSegmentId = getSegmentInfo(_tmpFromKM, "FROM")?.id ?: 0
                    _tmpToSegmentId = getSegmentInfo(_tmpToKM, "TO")?.id ?: 0
                    _tmpFromSegmentName = getSegmentInfo(_tmpFromKM, "FROM")?.name ?: ""
                    _tmpToSegmentName = getSegmentInfo(_tmpToKM, "TO")?.name ?: ""
                    viewBinding.txtFromSegment.text = _tmpFromSegmentName
                    viewBinding.txtToSegment.text = _tmpToSegmentName

                    handleTxtKMChanged("FROM", _tmpFromKM.toString())
                    handleTxtKMChanged("TO", _tmpToKM.toString())
                }
                getTotalDistance()
            }
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            errorEncountered = true
        }
    }


    private fun initializePreviousReceipts() {
        try {
            val receipts = viewModel.getAllReceiptWithNamesByDispatchReferenceId(_tmpDispatchId)
            receipts.let { receiptList ->
                if (receiptList.isNotEmpty()) {
                    var totalTicketAmount = 0.0
                    var totalIssuedTicketCount = 0
                    var totalGrossAmount = 0.0

                    for (receipt in receiptList) {
                        if (receipt.dispatchTripReferenceId == _tmpDispatchTripId) {
                            // Compute the total ticket amount of the current dispatch and trip
                            totalTicketAmount += receipt.ticketTotalAmount
                            totalIssuedTicketCount++
                        }
                        totalGrossAmount += receipt.ticketTotalAmount
                    }

                    viewBinding.txtCash.text = totalTicketAmount.amountToPHP()
                    viewBinding.txtGross.text = totalGrossAmount.amountToPHP()
                    viewBinding.txtIssuedTicketCount.text = totalIssuedTicketCount.toString()
                    /*_tmpCurrentReceiptId = receiptList.maxByOrNull { it.id }?.id ?: 0
                    val nextTicket = _tmpCurrentReceiptId + 1
                    viewBinding.txtTicketNo.text = nextTicket.formatId(prefix = "#", suffix = "", length = 5)*/
                } else {
                    // Handle case when receipts list is empty
                    viewBinding.txtCash.text = "0.00" // or any default value
                    viewBinding.txtGross.text = "0.00" // or any default value
                    viewBinding.txtIssuedTicketCount.text = "0" // or any default value

                }
            }
            val nextTicketNumber = _tmpCurrentReceiptId + 1
            viewBinding.txtTicketNo.text =
                "${_tmpDispatchId.formatId("#", "", 5)}${nextTicketNumber.formatId("", "", 9)}"
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            errorEncountered = true
        }
    }


    private fun initializeDiscounts() {
        try {
            viewBinding.radioGroupDiscounts.removeAllViews()
            var isFirstRadioButton = true
            for (discount in discounts) {
                val discountSelection = RadioButton(this)
                discountSelection.text = discount.name
                discountSelection.typeface = ResourcesCompat.getFont(
                    this,
                    R.font.convergence
                ) // Replace with your font resource
                discountSelection.textSize = 15f
                val layoutParams = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.WRAP_CONTENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(10, 10, 10, 10) // Left, Top, Right, Bottom
                discountSelection.layoutParams = layoutParams
                discountSelection.setOnCheckedChangeListener { _, isChecked ->

                    if (isChecked) {
                        if (discount.id == 0) {
                            showBaggageDialog()
                            baggageSelected = true
                            return@setOnCheckedChangeListener
                        }
                        baggageSelected = false
                        _tmpDiscountId = discount.id
                        _tmpDiscountAmount = discount.fraction
                        _tmpDiscountName = discount.name
                        getTotalDistance()
                    }
                }
                viewBinding.radioGroupDiscounts.addView(discountSelection)

                if (isFirstRadioButton) {
                    discountSelection.isChecked = true
                    isFirstRadioButton = false
                    _tmpDiscountId = discount.id
                    _tmpDiscountAmount = discount.fraction
                    _tmpDiscountName = discount.name
                    getTotalDistance()
                }
            }
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            errorEncountered = true
        }
    }


    private fun showBaggageDialog() {
        try {
            val textInputLayout = TextInputLayout(this)

            textInputLayout.setPadding(
                20.dpToPx(this),
                0.dpToPx(this),
                20.dpToPx(this),
                0.dpToPx(this)
            )

            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            input.textSize = 20f
            input.setText(baggageAmount.toString())
            input.textAlignment = EditText.TEXT_ALIGNMENT_CENTER
            input.setBackgroundResource(R.drawable.app_input_style)  // Apply the custom style
            textInputLayout.addView(input)

            val alert = AlertDialog.Builder(this)
                .setTitle("BAGGAGE")
                .setView(textInputLayout)
                .setMessage("Please enter amount")
                .setPositiveButton("OK") { dialog, _ ->

                    val tmpBaggageAmount = input.text.toString()
                    if (tmpBaggageAmount.isEmpty() || tmpBaggageAmount.toString().toDouble()
                            .isNaN()
                    ) {
                        Toast(this).showCustomToast(
                            "Baggage amount is invalid",
                            this,
                            Toast.LENGTH_SHORT
                        )
                        initializeDiscounts()
                        return@setPositiveButton
                    }
                    baggageAmount = tmpBaggageAmount.toDouble()

                    if (baggageAmount < 1.00) {
                        Toast(this).showCustomToast(
                            "Baggage amount is invalid",
                            this,
                            Toast.LENGTH_SHORT
                        )
                        initializeDiscounts()
                    } else {
                        _tmpTotalDiscountedFare = baggageAmount
                        viewBinding.txtAmount.text =
                            _tmpTotalDiscountedFare.coerceAtLeast(0.00).amountToPHP()
                        viewBinding.txtIsHotspot.text = "BAGGAGE"
                        dialog.dismiss()
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                    initializeDiscounts()
                }.create()
            alert.setCanceledOnTouchOutside(false)
            alert.show()
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            errorEncountered = true
        }
    }


    private fun handleTxtKMChanged(route: String, value: String) {

        try {

            if (value.isEmpty() || value.toInt() !in _routeSegmentKms) {
                viewBinding.btnPrint.isEnabled = false
                return
            } else {
                if (!printerBusy){
                    viewBinding.btnPrint.isEnabled = true
                }
            }

            if (route === "FROM") {
                _tmpFromKM = value.toInt()

                if (_tmpDirectionId == 1) {
                    if (_tmpFromKM > _tmpToKM) {
                        _tmpToKM = value.toInt() + 1
                        viewBinding.txtToKM.setText(_tmpToKM.toString())
                    }
                } else {
                    if (_tmpFromKM < _tmpToKM) {
                        _tmpToKM = value.toInt() - 1
                        viewBinding.txtToKM.setText(_tmpToKM.toString())
                    }
                }

            }
            if (route === "TO") {
                _tmpToKM = value.toInt()
                if (_tmpDirectionId == 1) {
                    segmentFromIndex = _routeSegmentKms.indexOf(value.toInt())
                    /*if (_tmpToKM < _tmpFromKM){
                        _tmpFromKM = value.toInt() -1
                        viewBinding.txtFromKM.setText(_tmpFromKM.toString())
                    }*/
                } else {
                    segmentToIndex = _routeSegmentKms.indexOf(value.toInt())
                    /*if (_tmpToKM > _tmpFromKM){
                        _tmpFromKM = value.toInt() +1
                        viewBinding.txtFromKM.setText(_tmpFromKM.toString())
                    }*/
                }
            }


            _tmpToSegmentId = getSegmentInfo(_tmpToKM, route)?.id ?: 0
            _tmpToSegmentName = getSegmentInfo(_tmpToKM, route)?.name ?: ""
            viewBinding.txtToSegment.text = _tmpToSegmentName


            _tmpFromSegmentId = getSegmentInfo(_tmpFromKM, route)?.id ?: 0
            _tmpFromSegmentName = getSegmentInfo(_tmpFromKM, route)?.name ?: ""
            viewBinding.txtFromSegment.text = _tmpFromSegmentName


            getTotalDistance()
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            errorEncountered = true
        }
    }


    private fun handlePrevButtonClick(route: String) {
        try {

            when {
                /*_tmpDirectionId == 1 && _tmpToKM == _tmpRouteLastKM -> {
                    return
                }
                _tmpDirectionId == 2 && _tmpToKM == _routeSegmentKms[0] -> {
                    return
                }*/

                _tmpDirectionId == 1 && route == "FROM" -> {
                    if (_tmpFromKM == _routeSegmentKms[0]) {
                        return
                    }
                    updateFromKM(decrement = true)
                }

                _tmpDirectionId == 1 && route == "TO" -> {
                    if (_tmpToKM > _routeSegmentKms[1]) {
                        updateToKM(decrement = true)
                        if (_tmpToKM == _tmpFromKM) {
                            updateFromKM(decrement = true)
                        }
                    }
                }

                _tmpDirectionId == 2 && route == "FROM" -> {
                    if (_tmpFromKM < _tmpRouteLastKM) {
                        updateFromKM(decrement = false)
                    }
                }

                _tmpDirectionId == 2 && route == "TO" -> {
                    if (_tmpToKM < _routeSegmentKms[_routeSegmentKms.size - 2]) {
                        if (_tmpToKM < _tmpFromKM) {
                            updateToKM(decrement = false)
                            if (_tmpToKM == _tmpFromKM) {
                                updateFromKM(decrement = false)
                            }
                        }
                    }
                }
            }
            getTotalDistance()
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            errorEncountered = true
        }
    }


    private fun handleNextButtonClick(route: String) {
        try {

            when {

                /*_tmpDirectionId == 1 && _tmpToKM == _tmpRouteLastKM -> {
                    return
                }
                _tmpDirectionId == 2 && _tmpToKM == _routeSegmentKms[0] -> {
                    return
                }*/


                _tmpDirectionId == 1 && route == "FROM" -> {
                    if (_tmpFromKM < _routeSegmentKms[_routeSegmentKms.size - 2]) {
                        updateFromKM(decrement = false)
                    }
                    if (_tmpToKM < _tmpRouteLastKM && segmentFromIndex == segmentToIndex) {
                        updateToKM(decrement = false)
                    }
                }

                _tmpDirectionId == 1 && route == "TO" -> {
                    if (_tmpToKM == _tmpRouteLastKM) {
                        return
                    }
                    if (_tmpToKM < _tmpRouteLastKM) {
                        updateToKM(decrement = false)
                    }
                }

                _tmpDirectionId == 2 && route == "FROM" -> {
                    if (_tmpFromKM == _routeSegmentKms[1]) {
                        return
                    }
                    if (_tmpFromKM > _routeSegmentKms[0]) {
                        updateFromKM(decrement = true)
                        if (_tmpToKM > _routeSegmentKms[0] && segmentFromIndex == segmentToIndex) {
                            updateToKM(decrement = true)
                        }
                    }
                }

                _tmpDirectionId == 2 && route == "TO" -> {
                    if (_tmpToKM > _routeSegmentKms[0]) {
                        updateToKM(decrement = true)
                        if (_tmpToKM == _tmpFromKM) {
                            updateFromKM(decrement = true)
                        }
                    }
                }
            }
            getTotalDistance()
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            errorEncountered = true
        }
    }


    private fun updateFromKM(decrement: Boolean) {
        try {
            //_tmpFromKM += if (decrement) -1 else 1
            segmentFromIndex =
                if (decrement) _routeSegmentKms.indexOf(_routeSegmentKms[segmentFromIndex - 1]) else _routeSegmentKms.indexOf(
                    _routeSegmentKms[segmentFromIndex + 1]
                )
            _tmpFromKM = _routeSegmentKms[segmentFromIndex]
            viewBinding.txtFromKM.setText(_tmpFromKM.toString())
            viewBinding.txtFromSegment.text = getSegmentInfo(_tmpFromKM, "FROM")?.name
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            errorEncountered = true
        }
    }


    private fun updateToKM(decrement: Boolean) {
        try {

            //_tmpToKM += if (decrement) -1 else 1
            segmentToIndex =
                if (decrement) _routeSegmentKms.indexOf(_routeSegmentKms[segmentToIndex - 1]) else _routeSegmentKms.indexOf(
                    _routeSegmentKms[segmentToIndex + 1]
                )
            _tmpToKM = _routeSegmentKms[segmentToIndex]
            viewBinding.txtToKM.setText(_tmpToKM.toString())
            viewBinding.txtToSegment.text = getSegmentInfo(_tmpToKM, "TO")?.name
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            errorEncountered = true
        }
    }


    // FareModel Computation
    private fun getTotalDistance() {
        try {
            _tmpTotalKM = if (_tmpDirectionId == 1) {
                _tmpToKM - _tmpFromKM
            } else {
                _tmpFromKM - _tmpToKM
            }

            if (!_tmpHasFromSegment) {
                viewBinding.txtFromSegment.text = "-"
            }

            if (!_tmpHasToSegment) {
                viewBinding.txtToSegment.text = "-"
            }

            computeFare()
            populateRemainingPassenger()
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            errorEncountered = true
        }
    }


    private fun computeFare() {
        try {

            if (_tmpRouteId < 1) {
                return
            }

            if (baggageSelected) {
                _tmpTotalDiscountedFare = baggageAmount
                viewBinding.txtAmount.text =
                    _tmpTotalDiscountedFare.coerceAtLeast(0.00).amountToPHP()
                viewBinding.txtIsHotspot.text = "BAGGAGE"
                return
            }

            val tmpHotspotInfo =
                viewModel.getSegmentHotspotAmount(_tmpRouteId, _tmpFromSegmentId, _tmpToSegmentId)
            _tmpHotspotAmount = tmpHotspotInfo?.amount ?: 0.00

            if (_tmpHotspotAmount > 0.0) {
                viewBinding.txtIsHotspot.text = "HOTSPOT"
            } else {
                viewBinding.txtIsHotspot.text = "-"
            }

            if (tmpHotspotInfo != null) {
                _tmpTotalDiscountedFare =
                    viewModel.computeTotalFare(_tmpTotalKM, _tmpDiscountAmount, tmpHotspotInfo)
            }
            viewBinding.txtAmount.text = _tmpTotalDiscountedFare.coerceAtLeast(0.00).amountToPHP()

            _tmpTotalKM = _tmpTotalKM.coerceAtLeast(0)
            viewBinding.txtTotalKM.text = "${_tmpTotalKM} KM"

            if (!printerBusy) {
                viewBinding.btnPrint.isEnabled =
                    (_tmpHasFromSegment && _tmpHasToSegment) && (_tmpTotalKM != 0 && _tmpTotalDiscountedFare.coerceAtLeast(
                        0.00
                    ) != 0.00)
            }
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            errorEncountered = true
        }
    }


    private fun populateRemainingPassenger() {
        try {
            val remainingPassenger = viewModel.getKMRemainingPassengerReference(
                _tmpDispatchId,
                _tmpDispatchTripId,
                _tmpFromKM,
                _tmpToKM,
                _tmpDirectionId
            )
            viewBinding.txtRemainingPassenger.text = remainingPassenger.toString()
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            errorEncountered = true
        }
    }


    private fun getSegmentInfo(km: Int, route: String): RouteSegmentModel? {
        val segment = viewModel.getRouteSegmentByRouteIdAndKM(_tmpRouteId, km)
        try {

            if (segment != null) {
                if (route == "FROM") {
                    _tmpHasFromSegment = true
                }
                if (route == "TO") {
                    _tmpHasToSegment = true

                }
            } else {
                if (route == "FROM") {
                    _tmpHasFromSegment = false
                }
                if (route == "TO") {
                    _tmpHasToSegment = false
                }
            }

        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            errorEncountered = true
            return segment
        }

        return segment
    }


    private fun proceedPayment() {
        try {
            //Modal for GCASH and PAYMAYA
            /*dialogPayment = DialogPayment.newInstance()
            dialogPayment.show(supportFragmentManager, "DialogForm")
            dialogPayment.dialogEventListener = this*/


            //For MPADPAY
            val options = arrayOf("Cash", "MPADPay")

            // Create an AlertDialog Builder
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Choose Mode of Payment")

            // Set the items and their click listeners
            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        printReceipt()
                    }
                    1 -> {
                        val intent = Intent(this, MPADPayScannerActivity::class.java)
                        activityLauncher.launch(intent)
                    }
                }
            }

            // Create and show the dialog
            val dialog = builder.create()
            dialog.show()

        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            errorEncountered = true
        }
    }


    private suspend fun performMPADPayTransaction(transactionCode:String){
        try {
            dialogProgress.showProgress("")
            val response = viewModelMPADPay.proceedMPADPayTransaction(transactionCode, _tmpTotalDiscountedFare.toString(), _tmpCurrentReceiptId.toString())
            Toast(this).showCustomToast("${response.message}",this, Toast.LENGTH_LONG)
            dialogProgress.dismissProgress()
            if (response.status == 200){
                paymentType = 4
                mpadPayBalance = response.data["Balance"] as Double
                val referenceId = response.data["TransactionReferenceId"] as Int
                paymentReferenceNumber = referenceId.formatId(prefix = "", suffix = "", length = 10)
                printReceipt()
            }

        }catch(e: Exception){
            Toast(this).showCustomToast("Error communicating with the server", this, Toast.LENGTH_LONG)
        }
    }


    private fun printReceipt() {

        try {
            if (errorEncountered){
                Toast.makeText(this, "Error encountered, Please report this to management.", Toast.LENGTH_LONG).show()
                finish()
                return;
            }

            CoroutineScope(Dispatchers.IO).launch {
                // Prepare ticket details
                val newTicketNumber = _tmpCurrentReceiptId + 1

                if (!viewModel.validateReferenceId(newTicketNumber, "ticket_receipt")) {
                    Toast(this@TicketReceiptActivity).showCustomToast(
                        "Dispatch already exists",
                        this@TicketReceiptActivity,
                        Toast.LENGTH_SHORT
                    )
                    return@launch
                }

                val ticketId = "${
                    _tmpDispatchId.toInt().formatId(prefix = "", suffix = "", length = 5)
                }${newTicketNumber.formatId(prefix = "", suffix = "", length = 9)}"

                val deviceName = validatedDeviceName(this@TicketReceiptActivity)
                val data = TicketReceiptWithNameModel(
                    id = 0,
                    deviceName = deviceName,
                    paymentType = paymentType,
                    paymentReferenceNumber = paymentReferenceNumber,
                    referenceId = newTicketNumber, // generateUUID(),
                    companyId = viewModel.sharedPrefs.getInt("sharedCompanyId", 0),
                    companyName = viewModel.sharedPrefs.getString("sharedCompanyName", "") ?: "",
                    dispatchReferenceId = _tmpDispatchId,
                    dispatchTripReferenceId = _tmpDispatchTripId,
                    routeDirectionId = _tmpDirectionId,
                    busId = _tmpBusId,
                    busNumber = _tmpBusNumber,
                    busPlateNumber = _tmpBusPlateNumber,
                    routeId = _tmpRouteId,
                    routeName = _tmpRouteName,
                    fromSegmentId = _tmpFromSegmentId,
                    fromSegmentName = _tmpFromSegmentName,
                    toSegmentId = _tmpToSegmentId,
                    toSegmentName = _tmpToSegmentName,
                    totalDistanceKM = _tmpTotalKM,
                    discountId = _tmpDiscountId,
                    isBaggage = baggageSelected,
                    discountName = _tmpDiscountName,
                    discountAmount = _tmpDiscountAmount,
                    ticketTotalAmount = _tmpTotalDiscountedFare,
                    fromSegmentKM = _tmpFromKM,
                    toSegmentKM = _tmpToKM,
                    driverId = _tmpDriverId,
                    driverName = _tmpDriverName,
                    conductorId = viewModel.sharedPrefs.getInt(
                        "sharedConductorId",
                        _tmpConductorId
                    ),
                    conductorName = _tmpConductorName
                )

                // Generate the receipt template
                val template = generateReceiptTemplate(
                    this@TicketReceiptActivity, data, ticketId, ticketQREnabled, receiptTemplate, mpadPayBalance.amountToPHP()
                )

                // Save the last receipt ID in shared preferences
                viewModel.sharedPrefs.setInt("sharedDeviceLastReceiptId", newTicketNumber)
                _tmpCurrentReceiptId = newTicketNumber

                printerBusy = true
                viewBinding.btnPrint.isEnabled = false

                // Print the receipt template
                printer.printTemplate(template)

                // Insert the receipt data into the database
                withContext(Dispatchers.Main) {
                    viewModel.insertReceipt(data)
                    initializePreviousReceipts()
                    populateRemainingPassenger()
                    initializeDiscounts()
                }
            }
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            errorEncountered = true
        }
    }


    override fun onPrinterNormal() {
        printerBusy = false
        viewBinding.btnPrint.isEnabled = true
    }

    override fun onPrinterBusy() {
        printerBusy = false
        viewBinding.btnPrint.isEnabled = false
    }


    override fun onSearchTextReceived(text: String) {
        dialog.updateSearchKeyword(text)
    }


    override fun onRecyclerItemClicked(
        id: Int,
        position: Int,
        title: String,
        subTitle: String,
        navigateTo: String,
        data: Any?
    ) {

        if (_segmentFor == "FROM") {
            if (data is RouteSegmentModel) {
                _tmpFromKM = data.distanceKM
                _tmpFromSegmentId = data.id
                _tmpFromSegmentName = data.name

                viewBinding.txtFromKM.setText(_tmpFromKM.toString())
                viewBinding.txtFromSegment.text = _tmpFromSegmentName
            }
        } else {
            if (data is RouteSegmentModel) {
                _tmpToKM = data.distanceKM
                _tmpToSegmentId = data.id
                _tmpToSegmentName = data.name

                viewBinding.txtToKM.setText(_tmpToKM.toString())
                viewBinding.txtToSegment.text = _tmpToSegmentName
            }
        }
    }

    override fun onRecyclerItemLongClicked(
        id: Int,
        position: Int,
        title: String,
        subTitle: String,
        navigateTo: String
    ) {
        Toast.makeText(this, "Long clicked", Toast.LENGTH_SHORT).show()
    }


    override fun onPaymentDialogCompleted(data: Any?) {
        runOnUiThread {
            if (data is TicketPaymentDataModel) {
                if (data.referenceNumber == "") {
                    paymentReferenceNumber = ""
                    paymentType = 1;
                    printReceipt()
                } else {
                    paymentReferenceNumber = data.referenceNumber
                    paymentType = data.paymentType;
                    printReceipt()
                }
            }
        }
    }
}


