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
@Entity(tableName = "accounts")
data class AccountModel (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("Id")
    val id: Int,

    @ColumnInfo(name = "userId")
    @SerializedName("UserId")
    val userId: Int,

    @ColumnInfo(name = "username")
    @SerializedName("Username")
    val userName: String,

    @ColumnInfo(name = "password")
    @SerializedName("Password")
    val password: String,

    @ColumnInfo(name = "permissions")
    @SerializedName("Permissions")
    val permissions: String,

    @ColumnInfo(name = "isActive")
    @SerializedName("IsActive")
    val isActive: Boolean = true,

    @ColumnInfo(name = "dateCreated")
    @SerializedName("DateCreated")
    val dateCreated: String
)