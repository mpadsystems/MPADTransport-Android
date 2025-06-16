package com.wneth.mpadtransport.views.configurations

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.wneth.mpadtransport.R
import com.wneth.mpadtransport.databinding.ActivityIngressoBinding
import com.wneth.mpadtransport.models.IncentiveWithRoleModel
import com.wneth.mpadtransport.models.IngressoModel
import com.wneth.mpadtransport.models.IngressoDeductionModel
import com.wneth.mpadtransport.models.IngressoDeductionWithNameModel
import com.wneth.mpadtransport.models.TerminalModel
import com.wneth.mpadtransport.utilities.DBBackupWorker
import com.wneth.mpadtransport.utilities.addTextChangedListener
import com.wneth.mpadtransport.utilities.amountToPHP
import com.wneth.mpadtransport.utilities.validatedDeviceName
import com.wneth.mpadtransport.utilities.dialogs.DialogRecyclerItems
import com.wneth.mpadtransport.utilities.disableForDuration
import com.wneth.mpadtransport.utilities.interfaces.RecyclerDialogEventListener
import com.wneth.mpadtransport.utilities.printer.Printer
import com.wneth.mpadtransport.utilities.printer.templates.generateIngressoTemplate
import com.wneth.mpadtransport.viewmodels.DispatchActivityViewModel
import com.wneth.mpadtransport.viewmodels.configurations.IngressoActivityViewModel

import com.wneth.mpadtransport.utilities.interfaces.SearchFragmentListener
import com.wneth.mpadtransport.utilities.setVisibility
import com.wneth.mpadtransport.utilities.showCustomToast
import com.wneth.mpadtransport.utilities.toProperCase
import com.wneth.mpadtransport.utilities.writeLogToSDCard

class IngressoActivity : AppCompatActivity(), SearchFragmentListener, RecyclerDialogEventListener {

    companion object {
        val deductionItems = MutableLiveData<List<IngressoDeductionWithNameModel>>()
    }

    private lateinit var viewModel: IngressoActivityViewModel
    private lateinit var viewBinding: ActivityIngressoBinding
    private lateinit var viewModelDispatch: DispatchActivityViewModel
    private lateinit var printer: Printer
    private lateinit var recyclerDialog: DialogRecyclerItems
    private var _ingressoTemplate: String = ""

    //COMMISSIONS
    private var _conductorCommission: List<IncentiveWithRoleModel>? = null
    private var _driverCommission: List<IncentiveWithRoleModel>? = null
    private var _driverBonus: List<IncentiveWithRoleModel>? = null
    private var _conductorBonus: List<IncentiveWithRoleModel>? = null


    var _tmpConductorActualCommissionAmount = 0.00
    var _tmpConductorActualBonusAmount = 0.00
    var _tmpDriverActualCommissionAmount = 0.00
    var _tmpDriverActualBonusAmount = 0.00

    // HEADER
    private var _finalRevenue: Double = 0.00
    private var _revenue: Double = 0.00
    private var _terminalId: Int = 0
    private var _additionalTicketAmount: Double = 0.00
    private var _cancelledTicketAmount: Double = 0.00

    private var _dispatchId: Int = 0

    // DEDUCTIONS
    private var _expenseComputeAfterRevenue = 0.00
    private var _expenseComputeAfterNet = 0.00
    private var _totalWithHolding = 0.00
    private var _totalExpenses = 0.00
    private var _totalCommission: Double = 0.00
    private var _totalBonus: Double = 0.00


    private var _finalNetCollection: Double = 0.00
    private var _partialRemitAmount: Double = 0.00
    private var _finalRemitAmount: Double = 0.00
    private var _shortOverAmount: Double = 0.00
    private var _shortOverTarget: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewBinding and ViewModel
        viewModel = ViewModelProvider(this)[IngressoActivityViewModel::class.java]
        viewModelDispatch = ViewModelProvider(this)[DispatchActivityViewModel::class.java]
        viewBinding = ActivityIngressoBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        try {
            printer = Printer(this)
            printer.initPrinter()
        }catch (ex: Exception){

        }

        _dispatchId = viewModel.sharedPrefs.getInt("sharedDeviceLastDispatchId", 0)

        // Event Listener
        viewBinding.btnSetTerminal.setOnClickListener {
            recyclerDialog = DialogRecyclerItems.newInstance(2)
            recyclerDialog.show(supportFragmentManager, "DialogForm")
            recyclerDialog.recyclerEventListener = this
        }

        viewBinding.btnAddExpenses.setOnClickListener {
            val intent = Intent(this, DeductionActivity::class.java)
            intent.putExtra("deductionType", 1)
            startActivity(intent)
        }

        viewBinding.btnComputeIngresso.setOnClickListener {
            computeIngresso()
        }

        viewBinding.btnAddWithHolding.setOnClickListener {
            val intent = Intent(this, DeductionActivity::class.java)
            intent.putExtra("deductionType", 2)
            startActivity(intent)
        }

        addTextChangedListener(viewBinding.txtAdditionalTickets, "") { value ->
            handleAdditionalTicketChanged(value)
        }

        addTextChangedListener(viewBinding.txtCancelledTickets, "") { value ->
            handleCancelledTicketChanged(value)
        }

        addTextChangedListener(viewBinding.txtFinalRemitAmount, "") { value ->
            handleRemitAmountChanged(value)
        }

        viewBinding.btnSubmitIngresso.setOnClickListener {
            submitIngresso()
        }

        viewBinding.btnCopyIngresso.setOnClickListener {
            printer.printTemplate(_ingressoTemplate)
            it.disableForDuration(2000)
        }

        viewBinding.lblDriverName.text =
            viewModel.getUserInfoById(
                viewModel.sharedPrefs.getInt(
                    "sharedDriverId",
                    0
                )
            )?.fullName?.toProperCase()
        viewBinding.lblConductorName.text = viewModel.getUserInfoById(
            viewModel.sharedPrefs.getInt(
                "sharedConductorId", 0
            )
        )?.fullName?.toProperCase()

        deductionItems.value = emptyList()
        initializeSettings()
        populateTotalDispatchRevenue()
        initAutoComputeExpenses()
    }


    private fun initializeSettings() {
        try {
            val deviceSettings = viewModel.deviceSettings()!!
            setVisibility(viewBinding.iLExpenses, deviceSettings.showIngressoExpenses)
            setVisibility(viewBinding.iLWithholding, deviceSettings.showIngressoWithholding)
            setVisibility(viewBinding.iLTotalCommission, deviceSettings.showIngressoTotalCommission)
            setVisibility(
                viewBinding.iLDriverCommission,
                deviceSettings.showIngressoDriverCommission
            )
            setVisibility(
                viewBinding.iLConductorCommission, deviceSettings.showIngressoConductorCommission
            )
            setVisibility(viewBinding.iLNetCollection, deviceSettings.showIngressoNetCollection)
            setVisibility(viewBinding.iLDriverBonus, deviceSettings.showIngressoDriverBonus)
            setVisibility(viewBinding.iLConductorBonus, deviceSettings.showIngressoConductorBonus)
            setVisibility(viewBinding.iLPartialRemit, deviceSettings.showIngressoPartialRemit)
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()

        populateDeductionItems()
        populateTotalDispatchRevenue()
        computeRevenueAndNet()
    }


    private fun populateDeductionItems() {
        try {
            deductionItems.observe(this) { deduction ->
                _totalExpenses = deduction.filter { it.deductionType == 1 }.sumOf { it.amount }
                _totalWithHolding = deduction.filter { it.deductionType == 2 }.sumOf { it.amount }

                _expenseComputeAfterRevenue = deduction.filter { it.deductionType == 1 && it.computeAfter ==1 }.sumOf { it.amount }
                _expenseComputeAfterNet = deduction.filter { it.deductionType == 1 && it.computeAfter ==2 }.sumOf { it.amount }

                viewBinding.txtExpenses.text = _totalExpenses.amountToPHP()
                viewBinding.txtWithholding.text = _totalWithHolding.amountToPHP()
            }
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun initAutoComputeExpenses() {
        try {
            deductionItems.value = deductionItems.value?.filter { !it.autoCompute }
            val expenses = viewModel.getAutoComputeExpenses(false)
            val currentExpenses = deductionItems.value ?: emptyList()

            //AutoCompute based on _finalRevenue
            val expensesWithThreshold = viewModel.getAutoComputeExpenses(true, _finalRevenue)
            deductionItems.value = currentExpenses + (expensesWithThreshold + expenses)
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun populateTotalDispatchRevenue() {
        try {
            val revenue = viewModel.getTotalRevenueByDispatchId(_dispatchId)

            _revenue = revenue.totalRevenue
            _finalRevenue = _revenue
            _partialRemitAmount = revenue.totalRemitted

            viewBinding.txtRevenue.text = _finalRevenue.amountToPHP()
            viewBinding.txtTotalRevenue.text = _finalRevenue.amountToPHP()
            viewBinding.txtPartialRemit.text = _partialRemitAmount.amountToPHP()
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun populateDeductions() {
        try {
            val incentives = viewModel.getIncentives()

            val commissions = incentives?.filter { it.type == "Commission" }
            val bonuses = incentives?.filter { it.type == "Bonus" }

            // COMMISSION
            _driverCommission = commissions?.filter {
                it.roleId == 7 && it.isActive
            }
            _conductorCommission = commissions?.filter {
                it.roleId == 6 && it.isActive
            }

            // BONUS
            _driverBonus = bonuses?.filter {
                it.roleId == 7 && it.isActive
            }
            _conductorBonus = bonuses?.filter {
                it.roleId == 6 && it.isActive
            }
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun handleAdditionalTicketChanged(value: String) {
        try {
            var finalValue = 0.00
            finalValue = if (value.isEmpty()) {
                0.00
            } else {
                value.toDouble()
            }
            _additionalTicketAmount = finalValue.toDouble()
            computeRevenueAndNet()
            populateDeductionItems()
            initAutoComputeExpenses()
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleCancelledTicketChanged(value: String) {
        try {
            var finalValue = 0.00
            finalValue = if (value.isEmpty()) {
                0.00
            } else {
                value.toDouble()
            }
            _cancelledTicketAmount = finalValue.toDouble()
            computeRevenueAndNet()
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun computeRevenueAndNet() {
        try {
            _finalRevenue = (_revenue + _additionalTicketAmount) - _cancelledTicketAmount
            _finalNetCollection = _finalRevenue + _totalWithHolding

            viewBinding.txtTotalRevenue.text = _finalRevenue.amountToPHP()
            viewBinding.txtNetCollection.text = _finalNetCollection.amountToPHP()
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    // ComputeAfter
    // 1. After Total Revenue
    // 2. After NET Collection
    private fun computeIngresso() {
        try {
            populateDeductions()
            computeRevenueAndNet()

            _finalRevenue = _finalRevenue - _expenseComputeAfterRevenue
            _finalNetCollection = _finalNetCollection - _expenseComputeAfterNet


            var tmpIncentiveComputeAfter = 0 // This will determine if the Incentives (bonus and commission) will be deducted from REVENUE or NET
            var referenceValue = 0.00; //If tmpIncentiveComputeAfter == 1, the referenceValue will act as the REVENUE, otherwise will act as the NET


            // Find the matching conductor commission and set `computeAfter`
            _conductorCommission?.forEach { commission ->
                tmpIncentiveComputeAfter = commission.computeAfter

                referenceValue = if (tmpIncentiveComputeAfter == 1){
                    _finalRevenue
                }else{
                    _finalNetCollection
                }

                if (referenceValue >= commission.thresholdRangeA && referenceValue <= commission.thresholdRangeB) {
                    _tmpConductorActualCommissionAmount = if (commission.isPercent) {
                        referenceValue * (commission.fraction / 100)
                    } else {
                        commission.fraction
                    }
                    tmpIncentiveComputeAfter = commission.computeAfter
                }
            }

            // Find the matching conductor bonus and set `computeAfter`
            _conductorBonus?.forEach { bonus ->

                tmpIncentiveComputeAfter = bonus.computeAfter
                referenceValue = if (tmpIncentiveComputeAfter == 1){
                    _finalRevenue
                }else{
                    _finalNetCollection
                }

                if (referenceValue >= bonus.thresholdRangeA && referenceValue <= bonus.thresholdRangeB) {
                    _tmpConductorActualBonusAmount = if (bonus.isPercent) {
                        referenceValue * (bonus.fraction / 100)
                    } else {
                        bonus.fraction
                    }
                }
            }

            // Find the matching driver commission and set `computeAfter`
            _driverCommission?.forEach { commission ->

                tmpIncentiveComputeAfter = commission.computeAfter
                referenceValue = if (tmpIncentiveComputeAfter == 1){
                    _finalRevenue
                }else{
                    _finalNetCollection
                }

                if (referenceValue >= commission.thresholdRangeA && referenceValue <= commission.thresholdRangeB) {
                    _tmpDriverActualCommissionAmount = if (commission.isPercent) {
                        referenceValue * (commission.fraction / 100)
                    } else {
                        commission.fraction
                    }
                }
            }

            // Find the matching driver bonus and set `computeAfter`
            _driverBonus?.forEach { bonus ->

                tmpIncentiveComputeAfter = bonus.computeAfter
                referenceValue = if (tmpIncentiveComputeAfter == 1){
                    _finalRevenue
                }else{
                    _finalNetCollection
                }

                if (referenceValue >= bonus.thresholdRangeA && referenceValue <= bonus.thresholdRangeB) {
                    _tmpDriverActualBonusAmount = if (bonus.isPercent) {
                        referenceValue * (bonus.fraction / 100)
                    } else {
                        bonus.fraction
                    }
                }
            }



            _totalCommission =
                _tmpConductorActualCommissionAmount + _tmpDriverActualCommissionAmount
            _totalBonus = _tmpConductorActualBonusAmount + _tmpDriverActualBonusAmount


            // Finalized the computation
            if (tmpIncentiveComputeAfter == 1){
                _finalRevenue = _finalRevenue - _totalCommission
            }else{
                _finalNetCollection = _finalNetCollection - _totalCommission
            }





            // Update UI
            viewBinding.txtTotalCommission.text = _totalCommission.amountToPHP()
            viewBinding.txtNetCollection.text = _finalNetCollection.amountToPHP()
            viewBinding.txtTotalRevenue.text = _finalRevenue.amountToPHP()
            viewBinding.txtDriverCommision.text = _tmpDriverActualCommissionAmount.amountToPHP()
            viewBinding.txtConductorCommision.text = _tmpConductorActualCommissionAmount.amountToPHP()
            viewBinding.txtDriverBonus.text = _tmpDriverActualBonusAmount.amountToPHP()
            viewBinding.txtConductorBonus.text = _tmpConductorActualBonusAmount.amountToPHP()

        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun handleRemitAmountChanged(value: String) {
        try {
            var finalValue = 0.00
            finalValue = if (value.isEmpty()) {
                0.00
            } else {
                value.toDouble()
            }
            _finalRemitAmount = finalValue.toDouble()
            _shortOverAmount = _finalNetCollection - _finalRemitAmount

            if (_shortOverAmount < 0.0) {
                viewBinding.shortOverDeductionPanel.visibility = View.VISIBLE
            } else {
                viewBinding.shortOverDeductionPanel.visibility = View.GONE
            }

            viewBinding.txtShortOver.text = _shortOverAmount.amountToPHP()
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }







    private fun submitIngresso() {
        try {
            viewBinding.btnComputeIngresso.performClick()
            viewBinding.btnComputeIngresso.performClick()
            viewBinding.btnComputeIngresso.performClick()
            viewBinding.btnComputeIngresso.performClick()

            val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)

            if (radioGroup.visibility == View.VISIBLE) {
                val checkedRadioButtonId = radioGroup.checkedRadioButtonId
                if (checkedRadioButtonId != -1) {
                    val selectedRadioButton = findViewById<RadioButton>(checkedRadioButtonId)
                    _shortOverTarget = selectedRadioButton.text.toString()
                } else {
                    _shortOverTarget = ""
                }
            } else {
                _shortOverTarget = ""
            }


            if (_shortOverAmount < 0.0 && _shortOverTarget.isEmpty()) {
                Toast(this).showCustomToast(
                    "Please select short over target",
                    this,
                    Toast.LENGTH_SHORT
                )
                return
            }

            if (_terminalId == 0) {
                Toast(this).showCustomToast("Please select a terminal", this, Toast.LENGTH_SHORT)
                return
            }

            if (_finalRemitAmount == 0.0) {
                Toast(this).showCustomToast(
                    "Please enter final remit amount",
                    this,
                    Toast.LENGTH_SHORT
                )
                return
            }

            //IngressoModel
            val ingressoReferenceId =
                viewModel.sharedPrefs.getInt("sharedDeviceLastIngressoId", 0) + 1

            if (!viewModel.validateReferenceId(ingressoReferenceId, "ingresso")) {
                Toast(this).showCustomToast(
                    "Dispatch already exists",
                    this,
                    Toast.LENGTH_SHORT
                )
                return
            }

            val backupSuccess = DBBackupWorker.performBackup(this)
            if (backupSuccess) {
                Toast(this).showCustomToast( "Database backup successful!", this, Toast.LENGTH_SHORT)
            }else{
                Toast(this).showCustomToast( "Database backup failed.", this, Toast.LENGTH_SHORT)
            }

            val deviceName = validatedDeviceName(this@IngressoActivity);
            val finalIngresso = IngressoModel(
                id = 0,
                deviceName = deviceName,
                referenceId = ingressoReferenceId,
                companyId = viewModel.sharedPrefs.getInt("sharedCompanyId", 0),
                dispatchReferenceId = _dispatchId,
                terminalId = _terminalId,
                revenue = _revenue,
                addedTicketAmount = _additionalTicketAmount,
                cancelledTicketAmount = _cancelledTicketAmount,
                finalRevenue = _finalRevenue,
                driverCommission = _tmpDriverActualCommissionAmount,
                conductorCommission = _tmpConductorActualCommissionAmount,
                totalCommission = _totalCommission,
                driverBonus = _tmpDriverActualBonusAmount,
                conductorBonus = _tmpConductorActualBonusAmount,
                netCollection = _finalNetCollection,
                partialRemittedAmount = _partialRemitAmount,
                finalRemittedAmount = _finalRemitAmount,
                shortOverAmount = _shortOverAmount,
                shortOverTarget = _shortOverTarget,
                status = 0
            )

            var deductionList: List<IngressoDeductionModel> = emptyList()
            deductionItems.observe(this) { deductionsWithType ->
                deductionList = deductionsWithType?.map { item ->
                    IngressoDeductionModel(
                        id = 0,
                        companyId = viewModel.sharedPrefs.getInt("sharedCompanyId", 0),
                        deviceName = deviceName,
                        dispatchReferenceId = _dispatchId,
                        ingressoReferenceId = item.ingressoReferenceId,
                        deductionId = item.deductionId,
                        amount = item.amount
                    )
                } ?: emptyList()
            }

            viewModel.insertFinalIngresso(finalIngresso, deductionList)
            val deductionsList =
                viewModel.getIngressoDeductionByReferenceId(finalIngresso.referenceId)
            _ingressoTemplate = generateIngressoTemplate(
                this,
                finalIngresso,
                viewModelDispatch.getDispatchByReferenceId(_dispatchId)!!,
                deductionsList
            )
            printer.printTemplate(_ingressoTemplate)
            viewModel.sharedPrefs.setInt("sharedDeviceLastIngressoId", ingressoReferenceId)

            viewBinding.btnSubmitIngresso.isEnabled = false

        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    override fun onRecyclerItemClicked(
        id: Int, position: Int, title: String, subTitle: String, navigateTo: String, data: Any?
    ) {
        if (data is TerminalModel) {
            _terminalId = data.id
            viewBinding.txtTerminal.text = data.name
        }
    }

    override fun onRecyclerItemLongClicked(
        id: Int, position: Int, title: String, subTitle: String, navigateTo: String
    ) {
        //
    }

    override fun onSearchTextReceived(text: String) {
        recyclerDialog.updateSearchKeyword(text)
    }


}