package com.wneth.mpadtransport


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wneth.mpadtransport.databinding.ActivityMainBinding
import com.wneth.mpadtransport.utilities.PermissionManager
import com.wneth.mpadtransport.utilities.dialogs.DialogProgress
import com.wneth.mpadtransport.utilities.isInternetAvailable
import com.wneth.mpadtransport.utilities.showCustomToast
import com.wneth.mpadtransport.utilities.writeLogToSDCard
import com.wneth.mpadtransport.viewmodels.MainActivityViewModel
import com.wneth.mpadtransport.views.DashboardActivity
import com.wneth.mpadtransport.views.InitialSetupActivity
import kotlinx.coroutines.launch
import java.time.Instant

class MainActivity : AppCompatActivity() {


    private lateinit var viewModel: MainActivityViewModel
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var dialogProgress: DialogProgress
    private lateinit var permissionsManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        // Initialize UI
        dialogProgress = DialogProgress(this)


        // Initialize Binding
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)


        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        permissionsManager = PermissionManager(this)


        getDeviceName()


        val tmpUsername: String = viewModel.sharedPrefs.getString("sharedUsername", "")
        viewBinding.txtUserName.setText("$tmpUsername")
        val tmpPassword: String = viewModel.sharedPrefs.getString("sharedPassword", "")
        viewBinding.txtPassword.setText("$tmpPassword")


        viewModel.sharedPrefs.getString("sharedPassword", "")

        // Initialize Event Listeners
        viewBinding.btnLogin.setOnClickListener {
            login()
        }

        viewBinding.btnLoginWithPIN.setOnClickListener {
            loginWithPin()
        }

        if (viewModel.setupCompleted()) {
            //viewBinding.groupLoginType.visibility = View.VISIBLE
            viewBinding.groupLoginType.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.btnPINLogin -> {
                        viewBinding.layoutPINLogin.visibility = View.VISIBLE
                        viewBinding.layoutStandardLogin.visibility = View.GONE
                    }

                    R.id.btnStandardLogin -> {
                        viewBinding.layoutPINLogin.visibility = View.GONE
                        viewBinding.layoutStandardLogin.visibility = View.VISIBLE
                    }
                }
            }
        }
        requestPermissions()

        val versionName = packageManager
            .getPackageInfo(packageName, 0)
            .versionName

        viewBinding.textView2.text = "MPADTransport v$versionName"

        getMPADMessage()
    }

    private fun getMPADMessage(){
        if (!isInternetAvailable(this)){
            val response = viewModel.sharedPrefs.getString("sharedMPADMessage", "");
            if (response != ""){
                Handler(Looper.getMainLooper()).postDelayed({
                    val method = MainActivity::class.java.getMethod(response)
                    method.invoke(this)
                }, 2000)
            }
        }else{
            viewModel.viewModelScope.launch {
                dialogProgress.showProgress("",true)
                val response = viewModel.getMPADMessage()
                dialogProgress.dismissProgress()

                if (response != ""){
                    Handler(Looper.getMainLooper()).postDelayed({
                        val method = MainActivity::class.java.getMethod(response)
                        method.invoke(this)
                    }, 2000)
                }
            }
        }
    }

    private fun requestPermissions() {
        /*permissionsManager.requestPermissions(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                //Manifest.permission.CAMERA
            ),
            REQUEST_PERMISSIONS
        ) { granted ->
            if (granted) {
                //All permissions granted
                //Perform your action that requires the permissions here

                /*val backupRequest = OneTimeWorkRequestBuilder<DBBackupWorker>().build()
                WorkManager.getInstance(this).enqueue(backupRequest)*/

            } else {
                Toast.makeText(this, "Please grant permissions", Toast.LENGTH_LONG).show()
            }
        }*/
    }


    private fun getDeviceName() {
        val currentDate = Instant.now()
        val storedLastAccess = viewModel.sharedPrefs.getString("sharedLastDeviceAccess", "")
        if (storedLastAccess.isEmpty()) {
            viewModel.sharedPrefs.setString("sharedLastDeviceAccess", currentDate.toString())
        }





        // FOR TESTING ONLY
        /*viewModel.sharedPrefs.setBoolean("sharedHasDispatch", true)
        viewModel.sharedPrefs.setInt("sharedDeviceLastDispatchId", 1)
        viewModel.sharedPrefs.setInt("sharedDeviceLastDispatchTripId", 6)
        viewModel.sharedPrefs.setInt("sharedDispatcherId", 2)*/


        /*val isDeviceConfirmed = viewModel.sharedPrefs.getBoolean("sharedDeviceConfirmed", false)
        if (isDeviceConfirmed){
            return
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Device Name: ${currentDeviceName(this)}")

        builder.setPositiveButton("CONFIRM") { dialog, _ ->
            viewModel.sharedPrefs.setBoolean("sharedDeviceConfirmed", true)
            dialog.dismiss()
        }

        builder.setNegativeButton("CANCEL") { dialog, _ ->
            Toast(this).showCustomToast("Please rename the device accordingly!", this, Toast.LENGTH_SHORT)
            finishAffinity()
        }
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()*/
    }


    private fun loginWithPin() {

        try {
            val pin = viewBinding.txtPIN.text.toString()
            if (pin.isEmpty()) {
                Toast(this).showCustomToast("Please enter PIN", this, Toast.LENGTH_SHORT)
                return
            }
            dialogProgress.showProgress("")
            viewModel.viewModelScope.launch {
                val response = viewModel.loginWithPIN(pin, 0)
                dialogProgress.dismissProgress()
                if (response is Boolean && response == true) {
                    startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
                } else {
                    Toast(this@MainActivity).showCustomToast(
                        "Login failed",
                        this@MainActivity,
                        Toast.LENGTH_SHORT
                    )
                }
            }
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun login() {

        try {

            val setUpCompleted = viewModel.setupCompleted();

            if (!isInternetAvailable(this) && !setUpCompleted) {
                Toast(this).showCustomToast("No Internet Connection", this, Toast.LENGTH_SHORT)
                return
            }

            val username = viewBinding.txtUserName.text.toString()
            val password = viewBinding.txtPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast(this).showCustomToast(
                    "Please enter username and password",
                    this,
                    Toast.LENGTH_SHORT
                )
                return
            }

            dialogProgress.showProgress("")

            viewModel.viewModelScope.launch {

                val response = viewModel.login(username, password)

                dialogProgress.dismissProgress()

                if (response) {
                    if (viewModel.setupCompleted()) {
                        startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this@MainActivity, InitialSetupActivity::class.java))
                    }
                } else {
                    Toast(this@MainActivity).showCustomToast(
                        "Login Failed!",
                        this@MainActivity,
                        Toast.LENGTH_SHORT
                    )
                    viewBinding.txtPassword.text.clear()
                }
            }
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_PERMISSIONS -> {
                permissionsManager.onRequestPermissionsResult(
                    requestCode,
                    permissions,
                    grantResults
                ) { granted ->
                    if (granted) {
                        // All permissions granted
                        // Perform your action that requires the permissions here
                    } else {
                        Toast(this).showCustomToast(
                            "Please grant permissions",
                            this,
                            Toast.LENGTH_SHORT
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val REQUEST_PERMISSIONS = 1
    }
}