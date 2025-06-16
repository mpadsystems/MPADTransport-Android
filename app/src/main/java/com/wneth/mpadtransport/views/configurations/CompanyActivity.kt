package com.wneth.mpadtransport.views.configurations


import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.wneth.mpadtransport.databinding.ActivityCompanyBinding
import com.wneth.mpadtransport.viewmodels.configurations.CompanyActivityViewModel


class CompanyActivity : AppCompatActivity() {

    // >>Variables Declarations
    /*
    * Code Below
    */
    private lateinit var viewModel: CompanyActivityViewModel
    private lateinit var viewBinding: ActivityCompanyBinding
    // =================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        // Initialize ViewModel
        /*
        * Code Below
        */
        viewModel = ViewModelProvider(this)[CompanyActivityViewModel::class.java]



        // Initialize Binding
        /*
        * Code Below
        */
        viewBinding = ActivityCompanyBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        // ===============================

        loadCompanyDetails();
    }

    private fun loadCompanyDetails() {
        val company = viewModel.getCompanyDetails()
        viewBinding.txtCompanyName.text = company!!.companyName ?: "Default CompanyModel"
        viewBinding.txtPhoneNumber.text = company.phoneNumber ?: "-"
        viewBinding.txtEmailAddress.text = company.email ?: "-"
        viewBinding.txtTIN.text = company.tin ?: "-"
        viewBinding.txtOfficeAddress.text = company.address ?: "-"
    }
}