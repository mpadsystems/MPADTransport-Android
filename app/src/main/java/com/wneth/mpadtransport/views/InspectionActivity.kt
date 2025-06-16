package com.wneth.mpadtransport.views

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.wneth.mpadtransport.databinding.ActivityInspectionBinding
import com.wneth.mpadtransport.models.DispatchTripReportModel
import com.wneth.mpadtransport.models.DispatchWithNameModel
import com.wneth.mpadtransport.models.InspectionModel
import com.wneth.mpadtransport.models.RouteSegmentModel
import com.wneth.mpadtransport.utilities.DrawableBox
import com.wneth.mpadtransport.utilities.addTextChangedListener
import com.wneth.mpadtransport.utilities.validatedDeviceName
import com.wneth.mpadtransport.viewmodels.InspectionActivityViewModel
import com.wneth.mpadtransport.viewmodels.TicketReceiptActivityViewModel

import com.wneth.mpadtransport.utilities.generateISODateTime
import com.wneth.mpadtransport.utilities.printer.Printer
import com.wneth.mpadtransport.utilities.printer.templates.generateInspectionReportTemplate
import com.wneth.mpadtransport.utilities.showCustomToast
import com.wneth.mpadtransport.utilities.writeLogToSDCard
import com.wneth.mpadtransport.viewmodels.DispatchActivityViewModel

class InspectionActivity : AppCompatActivity() {

    private lateinit var printer: Printer

    private lateinit var viewModelInspection: InspectionActivityViewModel
    private lateinit var viewModelTicketReceipt: TicketReceiptActivityViewModel
    private lateinit var viewModelDispatch: DispatchActivityViewModel
    private var _dispatchReferenceId: Int = 0
    private var _dispatchTripReferenceId: Int = 0


    private lateinit var viewBinding: ActivityInspectionBinding
    private lateinit var signaturePad: DrawableBox
    private var _tmpPassengerCount: Int = 0
    private var _tmpActualPassengerCount: Int = 0

    private var _tmpDirectionId: Int = 0
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
    private var _tmpSegmentId: Int = 0
    private var _tmpSegmentName: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize View Binding
        viewBinding = ActivityInspectionBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewModelTicketReceipt = ViewModelProvider(this)[TicketReceiptActivityViewModel::class.java]
        viewModelInspection = ViewModelProvider(this)[InspectionActivityViewModel::class.java]
        viewModelDispatch = ViewModelProvider(this)[DispatchActivityViewModel::class.java]

        // Initialize Printer
        try {
            printer = Printer(this)
            printer.initPrinter()
        }catch (ex: Exception){

        }

        signaturePad = viewBinding.signaturePad
        _dispatchReferenceId =
            viewModelInspection.sharedPrefs.getInt("sharedDeviceLastDispatchId", 0)
        _dispatchTripReferenceId =
            viewModelInspection.sharedPrefs.getInt("sharedDeviceLastDispatchTripId", 0)

        initializedDispatchRoute()

        viewBinding.btnClearSignature.setOnClickListener {
            signaturePad.clear()
        }

        addTextChangedListener(viewBinding.txtKMSearch, "") { value ->
            handleTxtKMChanged(value)
        }

        addTextChangedListener(viewBinding.txtActualPassengerCount, "") { value ->
            handleTxtActualCountChanged(value)
        }

        viewBinding.btnSubmitInspection.setOnClickListener {
            submitInspection()
        }
    }


    private fun initializedDispatchRoute() {
        try {
            val dispatchTrip = viewModelTicketReceipt.getLatestDispatchTrip()
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
                _tmpDispatchTripId =
                    viewModelInspection.sharedPrefs.getInt("sharedDeviceLastDispatchTripId", 0)
                _tmpRouteName = trip?.routeName ?: ""
            }
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun handleTxtKMChanged(value: String) {
        try {
            if (value.isEmpty()) {
                viewBinding.txtSegmentName.text = ""
                viewBinding.txtPassengerCount.text = "0 Passengers"
                return
            }
            _tmpSegmentId = getSegmentInfo(value.toInt())?.id ?: 0
            _tmpSegmentName = getSegmentInfo(value.toInt())?.name ?: ""

            val passenger = viewModelInspection.getDispatchTripKMPassengerCount(
                _tmpDispatchId,
                _tmpDispatchTripId,
                value.toInt()
            )
            _tmpPassengerCount = passenger
            viewBinding.txtSegmentName.text = _tmpSegmentName
            viewBinding.txtPassengerCount.text = "${_tmpPassengerCount.toString()} Passengers"
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleTxtActualCountChanged(value: String) {
        try {
            if (value.isEmpty() || viewBinding.txtKMSearch.text.isEmpty()) {
                return
            }
            viewBinding.txtCountDifference.text = (_tmpPassengerCount - value.toInt()).toString()
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun getSegmentInfo(km: Int): RouteSegmentModel? {
        val segment = viewModelTicketReceipt.getRouteSegmentByRouteIdAndKM(_tmpRouteId, km)
        try {

            return segment
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
        return segment
    }


    private fun submitInspection() {
        try {
            if (_tmpSegmentId == 0 || viewBinding.txtKMSearch.text.isEmpty()) {
                Toast(this).showCustomToast("Invalid Landmark!", this, Toast.LENGTH_SHORT)
            }

            val signatureByteArray = signaturePad.getSignatureAsByteArray()
            val base64Signature = InspectionModel.fromByteArray(signatureByteArray)
            _tmpActualPassengerCount =
                viewBinding.txtActualPassengerCount.text.toString().toIntOrNull() ?: 0

            if (viewBinding.txtActualPassengerCount.text.isEmpty()) {
                Toast(this).showCustomToast(
                    "Please enter actual passenger count",
                    this,
                    Toast.LENGTH_SHORT
                )
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

            val deviceName = validatedDeviceName(this@InspectionActivity)

            var receipts = viewModelTicketReceipt.getAllReceiptWithNamesByDispatchReferenceId(
                _dispatchReferenceId
            )

            val inspectionHeader = viewModelInspection.getDispatchInspectionHeader(_dispatchReferenceId)

            receipts = receipts.filter { receipt ->
                when (receipt.routeDirectionId) {
                    // For forward direction (e.g., 1): check if the passenger is still on board at the selected KM
                    1 -> receipt.fromSegmentKM <= viewBinding.txtKMSearch.text.toString()
                        .toInt() && receipt.toSegmentKM > viewBinding.txtKMSearch.text.toString()
                        .toInt() &&
                            receipt.dispatchTripReferenceId == _dispatchTripReferenceId
                    // For reverse direction (e.g., 2): check if the passenger is still on board at the selected KM
                    2 -> receipt.fromSegmentKM >= viewBinding.txtKMSearch.text.toString()
                        .toInt() && receipt.toSegmentKM < viewBinding.txtKMSearch.text.toString()
                        .toInt() &&
                            receipt.dispatchTripReferenceId == _dispatchTripReferenceId

                    else -> false
                }
            }

            /*if (receipts.isEmpty()) {
                Toast(this).showCustomToast("No Passenger found for this inspection", this, Toast.LENGTH_SHORT)
                return
            }*/

            val inspectionModel = InspectionModel(
                id = 0,
                dispatchReferenceId = _dispatchReferenceId,
                dispatchTripReferenceId = _dispatchTripReferenceId,
                companyId = viewModelInspection.sharedCompanyId,
                deviceName = deviceName,
                inspectorId = viewModelInspection.sharedPrefs.getInt("sharedInspectorId", 0),
                directionId = _tmpDirectionId,
                routeId = _tmpRouteId,
                segmentId = _tmpSegmentId,
                passengerCount = _tmpPassengerCount,
                actualPassengerCount = _tmpActualPassengerCount,
                signature = base64Signature,
                dateCreated = generateISODateTime(),
            )
            val isSaved = viewModelInspection.insertInspection(inspectionModel)

            if (isSaved) {
                val dispatch = viewModelDispatch.getDispatchByReferenceId(_dispatchReferenceId)

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
                val inspectionReport = DispatchTripReportModel(
                    dispatch = safeDispatch,
                    receipts = receipts
                )


                val template = generateInspectionReportTemplate(
                    this,
                    inspectionReport,
                    _tmpPassengerCount,
                    _tmpActualPassengerCount,
                    viewBinding.txtKMSearch.text.toString().toInt(),
                    _tmpSegmentName,
                    inspectionHeader
                )
                printer.printTemplate(template)
                Toast(this).showCustomToast("Inspection Submitted", this, Toast.LENGTH_SHORT)
                finish()
            } else {
                Toast(this).showCustomToast("Error saving Inspection", this, Toast.LENGTH_SHORT)
            }
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }

    }
}