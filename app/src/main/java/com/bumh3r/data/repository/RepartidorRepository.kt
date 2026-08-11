package com.bumh3r.data.repository

import com.bumh3r.data.local.entity.Configuracion
import com.bumh3r.data.local.entity.RegistroPedido
import kotlinx.coroutines.flow.Flow

/**
 * Punto único de acceso a los datos locales de la app. Las capas de UI
 * dependen de esta interfaz, no de Room directamente.
 */
interface RepartidorRepository {
    fun getRegistros(): Flow<List<RegistroPedido>>
    fun getRegistrosPorFecha(fecha: String): Flow<List<RegistroPedido>>
    suspend fun guardarRegistro(registro: RegistroPedido): Long
    suspend fun eliminarRegistro(registro: RegistroPedido)
    suspend fun eliminarTodosLosRegistros()

    fun getConfiguracion(): Flow<Configuracion?>
    suspend fun guardarConfiguracion(configuracion: Configuracion)
}
