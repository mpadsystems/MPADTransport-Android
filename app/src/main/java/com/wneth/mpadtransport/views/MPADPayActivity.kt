package com.wneth.mpadtransport.views

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.wneth.mpadtransport.R
import com.wneth.mpadtransport.databinding.ActivityMpadpayBinding
import com.wneth.mpadtransport.utilities.amountToPHP
import com.wneth.mpadtransport.utilities.dialogs.DialogProgress
import com.wneth.mpadtransport.utilities.dpToPx
import com.wneth.mpadtransport.utilities.formatToHumanDateTime
import com.wneth.mpadtransport.utilities.generateISODateTime
import com.wneth.mpadtransport.utilities.interfaces.PrinterEventListener
import com.wneth.mpadtransport.utilities.printer.Printer
import com.wneth.mpadtransport.utilities.showCustomToast
import com.wneth.mpadtransport.utilities.validatedDeviceName
import com.wneth.mpadtransport.utilities.writeLogToSDCard
import com.wneth.mpadtransport.viewmodels.MPADPayActivityViewModel
import com.wneth.mpadtransport.viewmodels.configurations.EmployeeActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MPADPayActivity : AppCompatActivity(), PrinterEventListener {

    private lateinit var viewBinding: ActivityMpadpayBinding
    private lateinit var viewModel: MPADPayActivityViewModel
    private lateinit var printer: Printer
    private lateinit var dialogProgress: DialogProgress
    private lateinit var viewModelEmployee: EmployeeActivityViewModel
    private lateinit var buyCreditDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[MPADPayActivityViewModel::class.java]
        viewModelEmployee = ViewModelProvider(this)[EmployeeActivityViewModel::class.java]

        viewBinding = ActivityMpadpayBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.btnRegisterUser.setOnClickListener(){
            val intent = Intent(this, MPADPayRegistrationActivity::class.java)
            startActivity(intent)
        }

        dialogProgress = DialogProgress(this)
        printer = Printer(this, this)
        printer.initPrinter()

        viewBinding.btnAddCredit.setOnClickListener {
            showCreditDialog()
        }
    }

    private fun showCreditDialog() {

        val tmpPersonnel = viewModelEmployee.getEmployee(viewModel.sharedPrefs.getInt("sharedMPADPayPersonnelId", 0))
        val personnel = tmpPersonnel?.fullName ?: "Unknown"

        try {
            // Create a container layout for inputs
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(20.dpToPx(this@MPADPayActivity), 0, 20.dpToPx(this@MPADPayActivity), 0)
            }

            // Create LayoutParams for input fields with bottom margin
            val inputLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16.dpToPx(this@MPADPayActivity) // 16dp margin bottom
            }


            // Create Remarks input with label
            val txtPhoneNumberLabel = TextView(this).apply {
                text = "Account"  // Label text
                textSize = 16f
                setPadding(0, 8.dpToPx(this@MPADPayActivity), 0, 4.dpToPx(this@MPADPayActivity))  // Padding for spacing between label and input
            }

            val txtPhoneNumber = EditText(this).apply {
                hint = ""  // Hint text inside the input
                inputType = InputType.TYPE_TEXT_VARIATION_PHONETIC
                textSize = 16f
                setBackgroundResource(R.drawable.app_input_style) // Optional custom style

                // Add padding to avoid hint overlap with the border
                setPadding(
                    10.dpToPx(this@MPADPayActivity),
                    10.dpToPx(this@MPADPayActivity),
                    10.dpToPx(this@MPADPayActivity),
                    10.dpToPx(this@MPADPayActivity)
                )
            }

            layout.addView(txtPhoneNumberLabel)
            layout.addView(txtPhoneNumber)


            // Create Amount input with label
            val txtAmountLabel = TextView(this).apply {
                text = "Amount"  // Label text
                textSize = 16f
                setPadding(0, 8.dpToPx(this@MPADPayActivity), 0, 4.dpToPx(this@MPADPayActivity))  // Padding for spacing between label and input
            }

            val txtAmount = EditText(this).apply {
                hint = ""  // Hint text inside the input
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                textSize = 16f
                setBackgroundResource(R.drawable.app_input_style) // Optional custom style

                // Add padding to avoid hint overlap with the border
                setPadding(
                    10.dpToPx(this@MPADPayActivity),
                    10.dpToPx(this@MPADPayActivity),
                    10.dpToPx(this@MPADPayActivity),
                    10.dpToPx(this@MPADPayActivity)
                )
            }

            layout.addView(txtAmountLabel)
            layout.addView(txtAmount)

            // Build the AlertDialog
            buyCreditDialog = AlertDialog.Builder(this)
                .setTitle("Add Credit")
                .setView(layout)
                .setMessage("Please fill in the details")
                .setPositiveButton("Save") { dialog, _ ->
                    val amount = txtAmount.text.toString().trim()
                    val phoneNumber = txtPhoneNumber.text.toString().trim()

                    if (amount.isEmpty()) {
                        txtAmount.error = "Amount is required"
                        txtAmount.requestFocus()
                        return@setPositiveButton
                    }

                    if (phoneNumber.isEmpty()) {
                        txtPhoneNumber.error = "Phone Number is required"
                        txtPhoneNumber.requestFocus()
                        return@setPositiveButton
                    }

                    // Save data
                    lifecycleScope.launch {
                        addCredit(amount.toDouble(), phoneNumber)
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.create()
            buyCreditDialog.setCanceledOnTouchOutside(false)
            buyCreditDialog.show()
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private suspend fun addCredit(amount: Double, phoneNumber: String) {
        val tmpPersonnel = viewModelEmployee.getEmployee(viewModel.sharedPrefs.getInt("sharedMPADPayPersonnelId", 0))
        val personnel = tmpPersonnel?.fullName ?: "Unknown"

        try {
            val response = withContext(Dispatchers.IO) {
                viewModel.addCredit(amount, phoneNumber, personnel)
            } // Wait for response properly

            withContext(Dispatchers.Main) {
                if (response.status == 200) {
                    val template = generateReceiptTemplate(
                        response.data["FullName"] as String,
                        (response.data["Balance"] as Double).amountToPHP(),
                    )
                    printer.printTemplate(template)
                    dialogProgress.dismissProgress()
                    Toast(this@MPADPayActivity).showCustomToast("Amount Credited successfully!", this@MPADPayActivity, Toast.LENGTH_LONG)

                    // Only dismiss the dialog if response is successful (status 200)
                    buyCreditDialog.dismiss()
                } else {
                    Toast(this@MPADPayActivity).showCustomToast("${response.message}", this@MPADPayActivity, Toast.LENGTH_LONG)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast(this@MPADPayActivity).showCustomToast("Error: ${e.message}", this@MPADPayActivity, Toast.LENGTH_LONG)
            }
        }
    }



    override fun onPrinterNormal() {
        //
    }

    override fun onPrinterBusy() {
        //
    }

    private fun generateReceiptTemplate(fullName: String, balance: String): String {
        val template = StringBuilder()
        template.append("[C]${viewModel.sharedPrefs.getString("sharedCompanyName")}[/C]\n")
        template.append("[C]MPADPay Registration[/C]\n")
        template.append("[C]Device: ${validatedDeviceName(this)}[/C]\n")
        template.append("[BD][/BD]")
        template.append("[L]Date:  ${generateISODateTime().formatToHumanDateTime()}[/L]\n")
        template.append("[L]FullName: ${fullName}[/L]\n")
        template.append("[L]Balance: ${balance}[/L]\n")
        template.append("[BD][/BD]")
        template.append("[BL][/BL]")
        template.append("[QR]https://pay.mpadsystems.com[/QR]")
        return template.toString()
    }

}