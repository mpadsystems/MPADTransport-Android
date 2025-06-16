package com.wneth.mpadtransport.views


import android.app.Activity
import android.content.Intent
import android.media.AudioAttributes
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.wneth.mpadtransport.databinding.ActivityMpadpayBinding
import com.wneth.mpadtransport.utilities.PermissionManager
import java.util.Locale

class MPADPayScannerActivity : AppCompatActivity() {

    private lateinit var toneGenerator: ToneGenerator

    private lateinit var optionsBuilder: GmsBarcodeScannerOptions.Builder
    private var allowManualInput = false
    private var enableAutoZoom = true

    private lateinit var viewBinding:ActivityMpadpayBinding
    private lateinit var permissionsManager: PermissionManager
    private var _ticketAmount: Double = 0.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        optionsBuilder = GmsBarcodeScannerOptions.Builder()
        // Initialize View Binding
        viewBinding = ActivityMpadpayBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        permissionsManager = PermissionManager(this)

        toneGenerator = ToneGenerator(AudioAttributes.USAGE_NOTIFICATION, 100)
        //barcodeResultView = findViewById(R.id.barcode_result_view)

        _ticketAmount = intent.getDoubleExtra("TicketAmount", 0.00)

        onScanButtonClicked();
    }


    private fun onScanButtonClicked() {

        if (allowManualInput) {
            optionsBuilder.allowManualInput()
        }
        if (enableAutoZoom) {
            optionsBuilder.enableAutoZoom()
        }

        val moduleInstall = ModuleInstall.getClient(this)
        val moduleInstallRequest = ModuleInstallRequest.newBuilder()
            .addApi(GmsBarcodeScanning.getClient(this))
            .build()

        moduleInstall.installModules(moduleInstallRequest)
            .addOnSuccessListener { response ->
                if (response.areModulesAlreadyInstalled()) {
                    // Module already installed, proceed with scanning
                    startScanning()
                } else {
                    // Module was just installed, wait briefly then scan
                    Handler(Looper.getMainLooper()).postDelayed({
                        startScanning()
                    }, 1000)
                }
            }
            .addOnFailureListener { e ->
                // Handle installation failure
            }

    }

    private fun startScanning() {
        optionsBuilder = GmsBarcodeScannerOptions.Builder()
        val gmsBarcodeScanner = GmsBarcodeScanning.getClient(this, optionsBuilder.build())
        gmsBarcodeScanner
            .startScan()
            .addOnSuccessListener { barcode: Barcode ->
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP)
                val data = Intent().apply {
                    putExtra("TransactionCode", barcode.rawValue)
                }
                setResult(Activity.RESULT_OK, data)
                finish()
            }
            .addOnFailureListener { e: Exception ->
                val data = Intent().apply {
                    putExtra("TransactionCode", "Error")
                }
                setResult(Activity.RESULT_OK, data)
                finish()
            }
            .addOnCanceledListener {
                val data = Intent().apply {
                    putExtra("TransactionCode", "Cancelled")
                }
                setResult(Activity.RESULT_OK, data)
                finish()
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_ALLOW_MANUAL_INPUT, allowManualInput)
        outState.putBoolean(KEY_ENABLE_AUTO_ZOOM, enableAutoZoom)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        allowManualInput = savedInstanceState.getBoolean(KEY_ALLOW_MANUAL_INPUT)
        enableAutoZoom = savedInstanceState.getBoolean(KEY_ENABLE_AUTO_ZOOM)
    }

    private fun getSuccessfulMessage(barcode: Barcode): String {
        val barcodeValue =
            String.format(
                Locale.US,
                "Display Value: %s\nRaw Value: %s\nFormat: %s\nValue Type: %s",
                barcode.displayValue,
                barcode.rawValue,
                barcode.format,
                barcode.valueType
            )
        return barcodeValue
    }

    private fun getErrorMessage(e: Exception): String? {
        return if (e is MlKitException) {
            when (e.errorCode) {
                MlKitException.CODE_SCANNER_CAMERA_PERMISSION_NOT_GRANTED ->
                    "Permission not granted"
                MlKitException.CODE_SCANNER_APP_NAME_UNAVAILABLE ->
                    "Unavailable"
                else -> e.message
            }
        } else {
            e.message
        }
    }






    companion object {
        private const val KEY_ALLOW_MANUAL_INPUT = "allow_manual_input"
        private const val KEY_ENABLE_AUTO_ZOOM = "enable_auto_zoom"
    }
}