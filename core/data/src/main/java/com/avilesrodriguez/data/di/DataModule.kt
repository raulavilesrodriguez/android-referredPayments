package com.avilesrodriguez.data.di

import com.avilesrodriguez.data.repository.AccountRepository
import com.avilesrodriguez.domain.interfaces.IAccountRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun provideAccountRepository(impl: AccountRepository): IAccountRepository

}