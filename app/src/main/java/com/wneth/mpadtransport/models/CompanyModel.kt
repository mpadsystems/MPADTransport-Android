package com.wneth.mpadtransport.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.wneth.mpadtransport.utilities.generateISODateTime
import java.time.LocalDateTime


/*
* NOTES
*
*
*
*/
@Entity(tableName = "company")
data class CompanyModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("Id")
    val id: Int,

    @ColumnInfo(name = "companyName")
    @SerializedName("CompanyName")
    val companyName: String,

    @ColumnInfo(name = "email")
    @SerializedName("Email")
    val email: String,

    @ColumnInfo(name = "phoneNumber")
    @SerializedName("PhoneNumber")
    val phoneNumber: String,

    @ColumnInfo(name = "tin")
    @SerializedName("TIN")
    val tin: String,

    @ColumnInfo(name = "address")
    @SerializedName("Address")
    val address: String,

    @ColumnInfo(name = "isActive")
    @SerializedName("IsActive")
    val isActive: Boolean = true,

    @ColumnInfo(name = "dateCreated")
    @SerializedName("DateCreated")
    val dateCreated: String,

    )
