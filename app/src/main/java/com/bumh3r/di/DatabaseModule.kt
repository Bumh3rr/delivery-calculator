package com.bumh3r.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bumh3r.data.local.AppDatabase
import com.bumh3r.data.local.dao.ConfiguracionDao
import com.bumh3r.data.local.dao.RegistroDao
import com.bumh3r.data.local.entity.Configuracion
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Módulo Hilt que provee la base de datos Room y sus DAOs. Sigue el mismo
 * patrón usado en solvyx-app: un objeto @Module con @Provides @Singleton
 * por cada dependencia.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        configuracionDaoProvider: Provider<ConfiguracionDao>
    ): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "repartidor_database")
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Fila de configuración por defecto (id = 1), para que la
                // app nunca se quede sin parámetros de cálculo. Se usa un
                // Provider (en vez de inyectar el DAO directo) porque en
                // este punto el propio AppDatabase todavía se está
                // construyendo.
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    configuracionDaoProvider.get().upsert(Configuracion())
                }
            }
        })
        .build()

    @Provides
    @Singleton
    fun provideRegistroDao(db: AppDatabase): RegistroDao = db.registroDao()

    @Provides
    @Singleton
    fun provideConfiguracionDao(db: AppDatabase): ConfiguracionDao = db.configuracionDao()
}
