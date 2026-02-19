package com.transporte.equipajeapp.di

import android.content.Context
import com.transporte.equipajeapp.data.local.PreferencesManager
import com.transporte.equipajeapp.data.remote.SoapClient
import com.transporte.equipajeapp.data.repository.AuthRepositoryImpl
import com.transporte.equipajeapp.data.repository.EquipajeRepositoryImpl
import com.transporte.equipajeapp.data.repository.ServicioRepositoryImpl
import com.transporte.equipajeapp.domain.repository.AuthRepository
import com.transporte.equipajeapp.domain.repository.EquipajeRepository
import com.transporte.equipajeapp.domain.repository.ServicioRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Configuración de inyección de dependencias
 * 
 * NOTA: Para usar WebService REAL (SOAP), usar esta configuración.
 * Para usar MOCKS (pruebas offline), ver instrucciones en WEBSERVICE_DELTA_CONFIG.md
 */

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSoapClient(): SoapClient = SoapClient()

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager = PreferencesManager(context)

    @Provides
    @Singleton
    fun provideAuthRepository(
        soapClient: SoapClient,
        preferencesManager: PreferencesManager
    ): AuthRepository = AuthRepositoryImpl(soapClient, preferencesManager)

    @Provides
    @Singleton
    fun provideServicioRepository(
        soapClient: SoapClient,
        preferencesManager: PreferencesManager
    ): ServicioRepository = ServicioRepositoryImpl(soapClient, preferencesManager)

    @Provides
    @Singleton
    fun provideEquipajeRepository(
        soapClient: SoapClient,
        preferencesManager: PreferencesManager
    ): EquipajeRepository = EquipajeRepositoryImpl(soapClient, preferencesManager)
}