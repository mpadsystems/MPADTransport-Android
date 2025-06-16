package com.wneth.mpadtransport.repositories

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.wneth.mpadtransport.models.BusModel
import com.wneth.mpadtransport.models.TerminalModel


@Dao
interface ITerminalRepository {

    @Query("SELECT * FROM terminals WHERE isActive = 1")
    fun getTerminals(): List<TerminalModel>?

    @Query("SELECT * FROM terminals WHERE id = :id AND isActive = 1")
    fun getTerminal(id:Int): TerminalModel?

    @Query("SELECT * FROM terminals WHERE isActive = 1 AND name LIKE '%'||:keyword||'%' OR address LIKE '%'||:keyword||'%'")
    fun filterTerminals(keyword:String): List<TerminalModel>?

    @Insert
    fun insert(terminal: TerminalModel)

    @Insert
    fun insertBulk(terminals: List<TerminalModel>): LongArray

    @Delete
    fun delete(terminal: TerminalModel)

    @Query("DELETE FROM terminals")
    fun deleteAll()

    @Query("DELETE FROM sqlite_sequence WHERE name='terminals'")
    fun resetAutoIncrement()
}