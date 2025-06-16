package com.wneth.mpadtransport.viewmodels.configurations

import android.app.Application
import com.wneth.mpadtransport.models.CompanyModel
import com.wneth.mpadtransport.viewmodels.BaseViewModel

class CompanyActivityViewModel  (application: Application): BaseViewModel(application){

    fun getCompanyDetails(): CompanyModel? {
        return companyRepository.getCompanyDetails()
    }
}