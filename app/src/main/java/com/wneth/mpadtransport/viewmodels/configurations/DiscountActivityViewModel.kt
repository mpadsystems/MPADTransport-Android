package com.wneth.mpadtransport.viewmodels.configurations

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.wneth.mpadtransport.configurations.MPADTransportApp
import com.wneth.mpadtransport.models.DiscountModel
import com.wneth.mpadtransport.viewmodels.BaseViewModel


class DiscountActivityViewModel(application: Application): BaseViewModel(application){

    private lateinit var _discounts: MutableLiveData<List<DiscountModel>>
    var discounts: MutableLiveData<List<DiscountModel>>
        get() = _discounts
        set(value) {
            _discounts = value
        }


    init {
        (application as MPADTransportApp).getAppComponent().inject(this)
        discounts = MutableLiveData()
        getDiscounts()
    }

    private fun getDiscounts() {
        val tmpDiscounts = discountRepository.getDiscounts()
        discounts.postValue(tmpDiscounts)
    }

    fun filterDiscounts(keyword: String) {
        val tmpDiscounts = discountRepository.filterDiscounts(keyword)
        discounts.postValue(tmpDiscounts!!)
    }

    fun getDiscount(id:Int): DiscountModel? {
        val tmpDiscount = discountRepository.getDiscountById(id)
        return tmpDiscount
    }

    fun deleteBus(discount: DiscountModel){
        discountRepository.delete(discount)
        getDiscounts()
    }
}