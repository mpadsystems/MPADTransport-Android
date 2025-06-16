package com.wneth.mpadtransport.configurations

import android.app.Application
import android.content.Context
import com.wneth.mpadtransport.repositories.IAccountRepository
import com.wneth.mpadtransport.repositories.IBusRepository
import com.wneth.mpadtransport.repositories.IIncentiveRepository
import com.wneth.mpadtransport.repositories.ICompanyRepository
import com.wneth.mpadtransport.repositories.IDeductionRepository
import com.wneth.mpadtransport.repositories.IDeviceSettingsRepository
import com.wneth.mpadtransport.repositories.IDiscountRepository
import com.wneth.mpadtransport.repositories.IDispatchRepository
import com.wneth.mpadtransport.repositories.IFareRepository
import com.wneth.mpadtransport.repositories.IHotspotRepository
import com.wneth.mpadtransport.repositories.IIngressoRepository
import com.wneth.mpadtransport.repositories.IInspectionRepository
import com.wneth.mpadtransport.repositories.IRemittanceRepository
import com.wneth.mpadtransport.repositories.IRoleRepository
import com.wneth.mpadtransport.repositories.IRouteRepository
import com.wneth.mpadtransport.repositories.ITerminalRepository
import com.wneth.mpadtransport.repositories.ITicketReceiptRepository
import com.wneth.mpadtransport.repositories.IUserRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val application: Application) {

    @Singleton
    @Provides
    fun provideAppContext(): Context {
        return application.applicationContext
    }

    @Singleton
    @Provides
    fun getRoomDBInstance(context: Context): AppDB {
        return AppDB.getAppDBInstance(context)
    }


    @Singleton
    @Provides
    fun getUserRepo(appDB: AppDB): IUserRepository {
        return appDB.userRepository()
    }

    @Singleton
    @Provides
    fun getCompanyRepo(appDB: AppDB): ICompanyRepository {
        return appDB.companyRepository()
    }

    @Singleton
    @Provides
    fun getAccountRepo(appDB: AppDB): IAccountRepository {
        return appDB.accountRepository()
    }

    @Singleton
    @Provides
    fun getBusRepo(appDB: AppDB): IBusRepository {
        return appDB.busRepository()
    }

    @Singleton
    @Provides
    fun getTerminalRepo(appDB: AppDB): ITerminalRepository {
        return appDB.terminalRepository()
    }

    @Singleton
    @Provides
    fun getDiscountRepo(appDB: AppDB): IDiscountRepository {
        return appDB.discountRepository()
    }

    @Singleton
    @Provides
    fun getRouteRepo(appDB: AppDB): IRouteRepository {
        return appDB.routeRepository()
    }

    @Singleton
    @Provides
    fun getDispatchRepo(appDB: AppDB): IDispatchRepository {
        return appDB.dispatchRepository()
    }

    @Singleton
    @Provides
    fun getFareRepo(appDB: AppDB): IFareRepository {
        return appDB.fareRepository()
    }

    @Singleton
    @Provides
    fun getTicketReceiptRepo(appDB: AppDB): ITicketReceiptRepository {
        return appDB.ticketReceiptRepository()
    }

    @Singleton
    @Provides
    fun getHotspotRepo(appDB: AppDB): IHotspotRepository {
        return appDB.hotspotRepository()
    }

    @Singleton
    @Provides
    fun getRemittanceRepo(appDB: AppDB): IRemittanceRepository {
        return appDB.remittanceRepository()
    }

    @Singleton
    @Provides
    fun getDeductionRepo(appDB: AppDB): IDeductionRepository {
        return appDB.deductionRepository()
    }


    @Singleton
    @Provides
    fun getIncentiveRepo(appDB: AppDB): IIncentiveRepository {
        return appDB.incentiveRepository()
    }


    @Singleton
    @Provides
    fun getIngressoRepo(appDB: AppDB): IIngressoRepository {
        return appDB.ingressoRepository()
    }

    @Singleton
    @Provides
    fun getDeviceSettingsRepo(appDB: AppDB): IDeviceSettingsRepository {
        return appDB.deviceSettingsRepository()
    }

    @Singleton
    @Provides
    fun getInspectionRepo(appDB: AppDB): IInspectionRepository {
        return appDB.inspectionRepository()
    }




}