package com.bumh3r.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bumh3r.data.local.dao.ConfiguracionDao
import com.bumh3r.data.local.dao.RegistroDao
import com.bumh3r.data.local.entity.Configuracion
import com.bumh3r.data.local.entity.RegistroPedido

/**
 * Base de datos local de la app. La fila por defecto de [Configuracion]
 * (id = 1) se inserta desde el callback configurado en
 * [com.bumh3r.di.DatabaseModule].
 */
@Database(
    entities = [RegistroPedido::class, Configuracion::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun registroDao(): RegistroDao
    abstract fun configuracionDao(): ConfiguracionDao
}
