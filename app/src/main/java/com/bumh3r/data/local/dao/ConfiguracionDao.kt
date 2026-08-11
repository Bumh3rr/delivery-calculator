package com.bumh3r.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.bumh3r.data.local.entity.Configuracion
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracionDao {
    @Query("SELECT * FROM configuracion WHERE id = 1")
    fun getConfiguracion(): Flow<Configuracion?>

    @Upsert
    suspend fun upsert(config: Configuracion)
}
