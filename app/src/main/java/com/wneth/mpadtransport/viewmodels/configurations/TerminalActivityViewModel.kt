package com.wneth.mpadtransport.viewmodels.configurations

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.wneth.mpadtransport.configurations.MPADTransportApp
import com.wneth.mpadtransport.models.TerminalModel
import com.wneth.mpadtransport.viewmodels.BaseViewModel

class TerminalActivityViewModel (application: Application): BaseViewModel(application){

    private lateinit var _terminals: MutableLiveData<List<TerminalModel>>
    var terminals: MutableLiveData<List<TerminalModel>>
        get() = _terminals
        set(value) {
            _terminals = value
        }

    init {
        (application as MPADTransportApp).getAppComponent().inject(this)
        terminals = MutableLiveData()
        getTerminals()
    }

    private fun getTerminals() {
        val tmpTerminals = terminalRepository.getTerminals()
        terminals.postValue(tmpTerminals!!)
    }

    fun filterTerminals(keyword: String) {
        val tmpTerminals = terminalRepository.filterTerminals(keyword)
        terminals.postValue(tmpTerminals!!)
    }

    fun getTerminal(id:Int): TerminalModel? {
        val tmpBus = terminalRepository.getTerminal(id)
        return tmpBus
    }

    fun deleteTerminal(terminal: TerminalModel){
        terminalRepository.delete(terminal)
        getTerminals()
    }
}