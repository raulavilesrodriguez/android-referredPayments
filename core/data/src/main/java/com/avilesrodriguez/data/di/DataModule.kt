package com.avilesrodriguez.data.di

import com.avilesrodriguez.data.repository.AccountRepository
import com.avilesrodriguez.data.repository.StoreRepository
import com.avilesrodriguez.domain.interfaces.IAccountRepository
import com.avilesrodriguez.domain.interfaces.IStoreRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun provideAccountRepository(impl: AccountRepository): IAccountRepository

    @Binds
    abstract fun provideStoreRepository(impl: StoreRepository): IStoreRepository

}