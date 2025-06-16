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
@Entity(tableName = "terminals")
data class TerminalModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("Id")
    val id: Int,

    @ColumnInfo(name = "companyId")
    @SerializedName("CompanyId")
    val companyId: Int,

    @ColumnInfo(name = "name")
    @SerializedName("Name")
    val name: String,

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
