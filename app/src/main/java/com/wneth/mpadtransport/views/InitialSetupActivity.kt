package com.wneth.mpadtransport.views

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wneth.mpadtransport.R
import com.wneth.mpadtransport.databinding.ActivityInitialSetupBinding
import com.wneth.mpadtransport.utilities.dialogs.DialogProgress
import com.wneth.mpadtransport.utilities.dpToPx
import com.wneth.mpadtransport.utilities.hideSystemUIOnCreate
import com.wneth.mpadtransport.utilities.hideSystemUIOnWindowFocusChanged
import com.wneth.mpadtransport.utilities.isInternetAvailable
import com.wneth.mpadtransport.utilities.showCustomToast
import com.wneth.mpadtransport.utilities.writeLogToSDCard
import com.wneth.mpadtransport.viewmodels.InitialSetupActivityViewModel
import kotlinx.coroutines.launch

class InitialSetupActivity : AppCompatActivity() {
    private lateinit var viewModel: InitialSetupActivityViewModel
    private lateinit var viewBinding: ActivityInitialSetupBinding
    private lateinit var dialogProgress: DialogProgress

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemUIOnCreate(window)

        // Initialize UI
        dialogProgress = DialogProgress(this)

        // Initialize Binding
        viewBinding = ActivityInitialSetupBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[InitialSetupActivityViewModel::class.java]

        // >>Initialize Intent Data
        val intent = intent
        val reSync = intent.extras?.getBoolean("reSync", false)

        if (reSync == true) {
            viewBinding.btnStartSetup.isEnabled = false
            viewBinding.btnStartSetup.text = "Please wait..."
            startSetup()
        }


        viewBinding.btnStartSetup.setOnClickListener {
            it.isEnabled = false
            viewBinding.btnStartSetup.text = "Please wait..."
            startSetup()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUIOnWindowFocusChanged(window.decorView, window)
        }
    }

    private fun startSetup() {
        try {
            if (!isInternetAvailable(this)) {
                Toast(this).showCustomToast("No Internet Connection", this, Toast.LENGTH_SHORT)
                return
            }

            val setupStatusView: LinearLayout = findViewById(R.id.setupStatus)
            setupStatusView.visibility = View.VISIBLE

            val syncStates = mapOf(
                "Syncing Users" to viewModel.usersSynced,
                "Syncing Company information" to viewModel.companySynced,
                "Syncing Accounts" to viewModel.accountsSynced,
                "Syncing Buses" to viewModel.busesSynced,
                "Syncing Terminals" to viewModel.terminalsSynced,
                "Syncing Discounts" to viewModel.discountsSynced,
                "Syncing Routes" to viewModel.routesSynced,
                "Syncing Fares" to viewModel.fareSynced,
                "Syncing Hotspots" to viewModel.hotspotsSynced,
                "Syncing Deductions" to viewModel.deductionsSynced,
                "Syncing Incentives" to viewModel.incentivesSynced,
                "Syncing Roles" to viewModel.rolesSynced,
                "Syncing Device Settings" to viewModel.deviceSettingsSynced
            )

            val loadingImages = mutableMapOf<String, ImageView>()

            syncStates.forEach { (label, liveData) ->
                val itemView = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginEnd = 4.dpToPx(context)
                    }
                    orientation = LinearLayout.HORIZONTAL
                }

                val imageView = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        40.dpToPx(context),
                        40.dpToPx(context)
                    )
                    setImageResource(R.drawable.appicon_loading)
                }

                val textView = TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        weight = 1f
                        marginStart = 1.dpToPx(context)
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                    }
                    text = label
                    setTypeface(
                        ResourcesCompat.getFont(
                            this@InitialSetupActivity,
                            R.font.convergence
                        ) ?: Typeface.DEFAULT
                    )
                    setPadding(
                        5.dpToPx(context),
                        10.dpToPx(context),
                        5.dpToPx(context),
                        5.dpToPx(context)
                    )
                }

                itemView.addView(imageView)
                itemView.addView(textView)
                setupStatusView.addView(itemView)

                loadingImages[label] = imageView
            }

            viewModel.viewModelScope.launch {
                viewModel.startSetup(this@InitialSetupActivity)

                syncStates.forEach { (label, liveData) ->
                    liveData.observe(this@InitialSetupActivity) { response ->
                        loadingImages[label]?.setImageResource(
                            if (response) R.drawable.appicon_check else R.drawable.appicon_cross // Assuming you have a cross icon for failure
                        )
                        if (!response) {
                            Toast(this@InitialSetupActivity).showCustomToast(
                                "$label sync failed, Please try again.",
                                this@InitialSetupActivity,
                                Toast.LENGTH_SHORT
                            )
                        }
                    }
                }

                viewModel.setupCompleted.observe(this@InitialSetupActivity) { response ->
                    //val message = if (response) "Setup successful" else "Setup failed"
                    //Toast.makeText(this@InitialSetupActivity, message, Toast.LENGTH_SHORT).show()
                    if (response) {
                        startActivity(
                            Intent(
                                this@InitialSetupActivity,
                                DashboardActivity::class.java
                            )
                        )
                        finish()
                    }
                }
            }
        } catch (e: Exception) {
            writeLogToSDCard(this, e.message.toString())
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

}