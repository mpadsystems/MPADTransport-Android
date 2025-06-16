package com.wneth.mpadtransport.configurations

import com.wneth.mpadtransport.viewmodels.BaseViewModel
import com.wneth.mpadtransport.viewmodels.DispatchActivityViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(baseViewModel: BaseViewModel)
}