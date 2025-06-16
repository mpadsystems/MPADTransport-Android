package com.wneth.mpadtransport.views


import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.wneth.mpadtransport.R
import com.wneth.mpadtransport.databinding.ActivityMpadpayRegistrationFormBinding
import com.wneth.mpadtransport.models.UserWithRole
import com.wneth.mpadtransport.utilities.amountToPHP
import com.wneth.mpadtransport.utilities.dialogs.DialogProgress
import com.wneth.mpadtransport.utilities.formatToHumanDateTime
import com.wneth.mpadtransport.utilities.generateISODateTime
import com.wneth.mpadtransport.utilities.interfaces.PrinterEventListener
import com.wneth.mpadtransport.utilities.printer.Printer
import com.wneth.mpadtransport.utilities.showCustomToast
import com.wneth.mpadtransport.utilities.validatedDeviceName
import com.wneth.mpadtransport.utilities.writeLogToSDCard
import com.wneth.mpadtransport.viewmodels.MPADPayActivityViewModel
import com.wneth.mpadtransport.viewmodels.configurations.EmployeeActivityViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale


class MPADPayRegistrationActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    PrinterEventListener {


    // >>Variables Declarations
    /*
    * Code Below
    */
    private var gender: Array<String> = arrayOf("","Male", "Female", "Other")

    private var genderSpinnerArrayAdapter: ArrayAdapter<String>? = null

    private lateinit var spinnerGender: Spinner

    private lateinit var viewModel: MPADPayActivityViewModel
    private lateinit var viewModelEmployee: EmployeeActivityViewModel
    private lateinit var viewBinding: ActivityMpadpayRegistrationFormBinding
    private var user: UserWithRole? = null
    private var userRole: Int = 0

    // ===============================
    private lateinit var printer: Printer
    private lateinit var dialogProgress: DialogProgress



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[MPADPayActivityViewModel::class.java]
        viewModelEmployee = ViewModelProvider(this)[EmployeeActivityViewModel::class.java]

        dialogProgress = DialogProgress(this)

        // Initialize viewBinding
        viewBinding = ActivityMpadpayRegistrationFormBinding.inflate(layoutInflater)

        setContentView(viewBinding.root)
        // ===============================


        // >>Initialize UI
        spinnerGender = findViewById(R.id.spnnGender)
        populateGenderSpinner()
        // ===============================


        try {
            printer = Printer(this)
            printer.initPrinter()
        }catch (ex: Exception){

        }


        // >>Initialize Event Listeners
        /*viewBinding.txtBirthdate.setOnClickListener {
            showDatePickerDialog()
        }*/

        viewBinding.btnSaveForm.setOnClickListener {
            lifecycleScope.launch {
                saveEmployee()
            }
        }
    }


    private suspend fun saveEmployee() {
        try {
            // Validate inputs
            if (viewBinding.txtFullName.text.toString().trim().isEmpty()) {
                viewBinding.txtFullName.error = "Full name is required"
                viewBinding.txtFullName.requestFocus()
                return
            }

            if (viewBinding.txtPhoneNumber.text.toString().trim().isEmpty()) {
                viewBinding.txtPhoneNumber.error = "Phone number is required"
                viewBinding.txtPhoneNumber.requestFocus()
                return
            }

            if (viewBinding.txtInitialDeposit.text.toString().trim().isEmpty()) {
                viewBinding.txtInitialDeposit.error = "Initial deposit is required"
                viewBinding.txtInitialDeposit.requestFocus()
                return
            }

            dialogProgress.showProgress("")

            val tmpPersonnel = viewModelEmployee.getEmployee(viewModel.sharedPrefs.getInt("sharedMPADPayPersonnelId", 0))
            val personnel = tmpPersonnel?.fullName ?: "Unknown"

            val userData = mapOf(
                "id" to "0",
                "FullName" to viewBinding.txtFullName.text.toString(),
                //"Gender" to viewBinding.spnnGender.selectedItemPosition.toString(),
                //"BirthDate" to selectedDate.toString(),
                //"Address" to viewBinding.txtAddress.text.toString(),
                "PhoneNumber" to viewBinding.txtPhoneNumber.text.toString(),
                "InitialDeposit" to viewBinding.txtInitialDeposit.text.toString(),
                "CompanyName" to viewModel.sharedPrefs.getString("sharedCompanyName", ""),
                "DeviceName" to viewModel.sharedPrefs.getString("sharedValidatedDeviceName", ""),
                "RegisteredByName" to personnel,
                "SecretCode" to "dsfHj!48@io30pLskmHtj_23mSkmALD2DvVns!"
            )
            val response = viewModel.registerMPADPayUser(userData)
            if (response.status == 200) {
                val template = generateRegistrationTemplate(
                    viewBinding.txtFullName.text.toString(),
                    viewBinding.txtInitialDeposit.text.toString().toDouble().amountToPHP()
                )
                printer.printTemplate(template)
                dialogProgress.dismissProgress()
                finish()
                Toast(this).showCustomToast("Registration successful!", this, Toast.LENGTH_LONG)
            } else {
                Toast(this).showCustomToast("${response.message}", this, Toast.LENGTH_LONG)
            }

        } catch (e: Exception) {
            dialogProgress.dismissProgress()
            writeLogToSDCard(this, e.message.toString())
            Toast(this).showCustomToast("${e.message.toString()}", this, Toast.LENGTH_LONG)
        }
    }



    private fun populateGenderSpinner(){
        genderSpinnerArrayAdapter = ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, gender)
        spinnerGender.setAdapter(genderSpinnerArrayAdapter)
    }


    private lateinit var calendar: Calendar;
    private lateinit var selectedDate: LocalDate

    private fun showDatePickerDialog(){
        calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, this, year, month, day)
        datePickerDialog.show()
    }


    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
        calendar.set(year, month, dayOfMonth)
        val dateFormat = SimpleDateFormat("MMM. d, yyyy", Locale.US)
        val formattedDate = dateFormat.format(calendar.time)

        viewBinding.txtBirthdate.setText(formattedDate)
    }

    override fun onPrinterNormal() {
        //
    }

    override fun onPrinterBusy() {
        viewBinding.btnSaveForm.isEnabled = false
    }


    private fun generateRegistrationTemplate(fullName: String, balance: String): String {
        val template = StringBuilder()
        template.append("[C]${viewModel.sharedPrefs.getString("sharedCompanyName")}[/C]\n")
        template.append("[C]MPADPay Registration[/C]\n")
        template.append("[C]Device: ${validatedDeviceName(this)}[/C]\n")
        template.append("[BD][/BD]")
        template.append("[L]Date:  ${generateISODateTime().formatToHumanDateTime()}[/L]\n")
        template.append("[L]FullName: ${fullName}[/L]\n")
        template.append("[L]Balance: ${balance}[/L]\n")
        template.append("[L]Password: ${123456}[/L]\n")
        template.append("[BD][/BD]")
        template.append("[BL][/BL]")
        template.append("[QR]https://pay.mpadsystems.com[/QR]")
        return template.toString()
    }
}