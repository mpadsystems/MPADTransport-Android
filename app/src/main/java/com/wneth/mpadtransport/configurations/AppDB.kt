package com.wneth.mpadtransport.configurations


import android.content.Context
import android.os.Environment
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.wneth.mpadtransport.models.AccountModel
import com.wneth.mpadtransport.models.BusModel
import com.wneth.mpadtransport.models.CompanyModel
import com.wneth.mpadtransport.models.DiscountModel
import com.wneth.mpadtransport.models.DispatchModel
import com.wneth.mpadtransport.models.DispatchTripModel
import com.wneth.mpadtransport.models.DeductionModel
import com.wneth.mpadtransport.models.DeviceModel
import com.wneth.mpadtransport.models.DeviceSettingModel
import com.wneth.mpadtransport.models.FareModel
import com.wneth.mpadtransport.models.FareSetupModel
import com.wneth.mpadtransport.models.HotspotModel
import com.wneth.mpadtransport.models.IncentiveModel
import com.wneth.mpadtransport.models.RemittanceModel
import com.wneth.mpadtransport.models.RoleModel
import com.wneth.mpadtransport.models.RouteModel
import com.wneth.mpadtransport.models.RouteSegmentModel
import com.wneth.mpadtransport.models.TerminalModel
import com.wneth.mpadtransport.models.TicketReceiptModel
import com.wneth.mpadtransport.models.UserModel
import com.wneth.mpadtransport.models.InspectionModel
import com.wneth.mpadtransport.models.IngressoModel
import com.wneth.mpadtransport.models.IngressoDeductionModel
import com.wneth.mpadtransport.repositories.IAccountRepository
import com.wneth.mpadtransport.repositories.IBusRepository
import com.wneth.mpadtransport.repositories.ICompanyRepository
import com.wneth.mpadtransport.repositories.IDeductionRepository
import com.wneth.mpadtransport.repositories.IDeviceSettingsRepository
import com.wneth.mpadtransport.repositories.IDiscountRepository
import com.wneth.mpadtransport.repositories.IDispatchRepository
import com.wneth.mpadtransport.repositories.IFareRepository
import com.wneth.mpadtransport.repositories.IHotspotRepository
import com.wneth.mpadtransport.repositories.IIncentiveRepository
import com.wneth.mpadtransport.repositories.IIngressoRepository
import com.wneth.mpadtransport.repositories.IInspectionRepository
import com.wneth.mpadtransport.repositories.IRemittanceRepository
import com.wneth.mpadtransport.repositories.IRouteRepository
import com.wneth.mpadtransport.repositories.ITerminalRepository
import com.wneth.mpadtransport.repositories.ITicketReceiptRepository
import com.wneth.mpadtransport.repositories.IUserRepository
import java.io.File


@Database(entities = [
    UserModel::class,
    CompanyModel::class,
    AccountModel::class,
    BusModel::class,
    TerminalModel::class,
    DiscountModel::class,
    RouteModel::class,
    RouteSegmentModel::class,
    DispatchModel::class,
    DispatchTripModel::class,
    FareModel::class,
    FareSetupModel::class,
    HotspotModel::class,
    TicketReceiptModel::class,
    RemittanceModel::class,
    DeductionModel::class,
    IncentiveModel::class,
    RoleModel::class,
    IngressoModel::class,
    InspectionModel::class,
    IngressoDeductionModel::class,
    DeviceSettingModel::class,
    DeviceModel::class
], version = 1, exportSchema = true)

abstract class AppDB : RoomDatabase() {
    abstract fun userRepository(): IUserRepository
    abstract fun companyRepository(): ICompanyRepository
    abstract fun accountRepository(): IAccountRepository
    abstract fun busRepository(): IBusRepository
    abstract fun terminalRepository(): ITerminalRepository
    abstract fun discountRepository(): IDiscountRepository
    abstract fun routeRepository(): IRouteRepository
    abstract fun dispatchRepository(): IDispatchRepository
    abstract fun fareRepository(): IFareRepository
    abstract fun ticketReceiptRepository(): ITicketReceiptRepository
    abstract fun hotspotRepository(): IHotspotRepository
    abstract fun remittanceRepository(): IRemittanceRepository
    abstract fun deductionRepository(): IDeductionRepository
    abstract fun incentiveRepository(): IIncentiveRepository
    abstract fun ingressoRepository(): IIngressoRepository
    abstract fun deviceSettingsRepository(): IDeviceSettingsRepository
    abstract fun inspectionRepository(): IInspectionRepository

    companion object {
        @Volatile
        private var db_instance: AppDB? = null

        fun getAppDBInstance(context: Context): AppDB {
            return db_instance ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDB::class.java,
                    "MPADTransportDB.db3"
                )
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
                db_instance = instance
                instance
            }
        }
    }
}