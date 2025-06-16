package com.wneth.mpadtransport.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.wneth.mpadtransport.utilities.generateISODateTime
import java.time.LocalDate
import java.time.LocalDateTime


/*
* NOTES
* Roles
* 1. SuperUser
* 2. Administrator
* 3. Cashier
* 4. Inspector
* 5. Dispatcher
* 6. Conductor
* 7. Driver
*/


@Entity(tableName = "users")
data class UserModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("Id")
    val id: Int,

    @ColumnInfo(name = "companyId")
    @SerializedName("CompanyId")
    val companyId: Int = 1,

    @ColumnInfo(name = "roleId")
    @SerializedName("RoleId")
    val roleId: Int,

    @ColumnInfo(name = "fullName")
    @SerializedName("FullName")
    val fullName: String,

    @ColumnInfo(name = "gender")
    @SerializedName("Gender")
    val gender: Int,

    @ColumnInfo(name = "birthDate")
    @SerializedName("BirthDate")
    val birthDate: String,

    @ColumnInfo(name = "address")
    @SerializedName("Address")
    val address: String?,

    @ColumnInfo(name = "email")
    @SerializedName("Email")
    val email: String?,

    @ColumnInfo(name = "phoneNumber")
    @SerializedName("PhoneNumber")
    val phoneNumber: String?,

    @ColumnInfo(name = "isActive")
    @SerializedName("IsActive")
    val isActive: Boolean = true,

    @ColumnInfo(name = "dateCreated")
    @SerializedName("DateCreated")
    val dateCreated: String,
)

data class UserWithRole(
    val id: Int,
    val companyId: Int,
    val roleId: Int,
    val roleName: String,
    val fullName: String,
    val gender: Int,
    val birthDate: String,
    val address: String?,
    val email: String?,
    val phoneNumber: String?,
    val isActive: Boolean = true,
    val dateCreated: String,
)