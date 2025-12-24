package com.avilesrodriguez.data.di

import android.content.Context
import com.avilesrodriguez.data.datasource.dataStore.AuthPreferencesDataSource
import com.avilesrodriguez.data.repository.AccountRepository
import com.avilesrodriguez.data.repository.AuthPreferencesRepository
import com.avilesrodriguez.data.repository.StoreRepository
import com.avilesrodriguez.domain.interfaces.IAccountRepository
import com.avilesrodriguez.domain.interfaces.IAuthPreferences
import com.avilesrodriguez.domain.interfaces.IStoreRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourcesModule {
    @Provides
    @Singleton
    fun provideAuthPreferencesDataSource(@ApplicationContext context: Context): AuthPreferencesDataSource {
        return AuthPreferencesDataSource(context)
    }
}


@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun provideAuthPreferencesRepository(impl: AuthPreferencesRepository): IAuthPreferences

    @Binds
    abstract fun provideAccountRepository(impl: AccountRepository): IAccountRepository

    @Binds
    abstract fun provideStoreRepository(impl: StoreRepository): IStoreRepository

}