package com.bumh3r.di

import com.bumh3r.data.repository.RepartidorRepository
import com.bumh3r.data.repository.RepartidorRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Enlaza la interfaz del repositorio con su implementación concreta, para
 * que las capas superiores (ViewModels) dependan solo de la abstracción.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRepartidorRepository(
        impl: RepartidorRepositoryImpl
    ): RepartidorRepository
}
