package com.wneth.mpadtransport.repositories


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Transaction
import com.wneth.mpadtransport.models.AccountModel
import com.wneth.mpadtransport.models.CompanyModel
import com.wneth.mpadtransport.models.UserModel


@Dao
interface IAccountRepository {
    @Query("SELECT * FROM accounts")
    fun getAccounts(): List<AccountModel>?

    @Query("SELECT id FROM accounts")
    fun getAccountIds(): List<Int>?

    @Transaction
    @Query("SELECT * FROM users WHERE isActive = 1 AND id = (SELECT userId FROM accounts WHERE userName = :username AND password = :password)")
    fun login(username:String, password:String): UserModel?

    @Transaction
    @Query("SELECT * FROM users WHERE isActive = 1 AND id = (SELECT userId FROM accounts WHERE password = :pin AND roleId IN (:roles))")
    fun loginWithPIN(pin:String, roles: List<String>): UserModel?

    @Transaction
    @Query("SELECT * FROM users WHERE isActive = 1 AND id IN (SELECT userId FROM accounts WHERE password = :pin AND roleId IN (:roles) AND userId = :userId)")
    fun loginWithPINAndUserId(pin: String, roles: List<String>, userId: Int): UserModel?

    @Insert
    fun insert(account: AccountModel)

    @Insert
    fun insertBulk(accounts: List<AccountModel>): LongArray

    /*@Update
    fun update(token: String, userId: String)*/

    @Query("DELETE FROM accounts")
    fun deleteAll()

    @Query("DELETE FROM sqlite_sequence WHERE name='accounts'")
    fun resetAutoIncrement()
}