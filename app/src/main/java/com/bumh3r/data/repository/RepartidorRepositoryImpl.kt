package com.bumh3r.data.repository

import com.bumh3r.data.local.dao.ConfiguracionDao
import com.bumh3r.data.local.dao.RegistroDao
import com.bumh3r.data.local.entity.Configuracion
import com.bumh3r.data.local.entity.RegistroPedido
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RepartidorRepositoryImpl @Inject constructor(
    private val registroDao: RegistroDao,
    private val configuracionDao: ConfiguracionDao
) : RepartidorRepository {

    override fun getRegistros(): Flow<List<RegistroPedido>> = registroDao.getTodos()

    override fun getRegistrosPorFecha(fecha: String): Flow<List<RegistroPedido>> =
        registroDao.getPorFecha(fecha)

    override suspend fun guardarRegistro(registro: RegistroPedido): Long =
        registroDao.insert(registro)

    override suspend fun eliminarRegistro(registro: RegistroPedido) =
        registroDao.delete(registro)

    override suspend fun eliminarTodosLosRegistros() = registroDao.deleteTodos()

    override fun getConfiguracion(): Flow<Configuracion?> = configuracionDao.getConfiguracion()

    override suspend fun guardarConfiguracion(configuracion: Configuracion) =
        configuracionDao.upsert(configuracion)
}
