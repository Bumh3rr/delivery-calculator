package com.bumh3r.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.bumh3r.data.local.entity.RegistroPedido
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroDao {
    @Insert
    suspend fun insert(registro: RegistroPedido): Long

    @Query("SELECT * FROM registros ORDER BY timestamp DESC")
    fun getTodos(): Flow<List<RegistroPedido>>

    @Query("SELECT * FROM registros WHERE fecha = :fecha ORDER BY timestamp DESC")
    fun getPorFecha(fecha: String): Flow<List<RegistroPedido>>

    @Delete
    suspend fun delete(registro: RegistroPedido)

    @Query("DELETE FROM registros")
    suspend fun deleteTodos()
}
