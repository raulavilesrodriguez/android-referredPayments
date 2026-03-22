package com.avilesrodriguez.data.di

import com.avilesrodriguez.data.repository.AccountRepository
import com.avilesrodriguez.data.repository.AuthPreferencesRepository
import com.avilesrodriguez.data.repository.FCMTokenRepository
import com.avilesrodriguez.data.repository.InternalTokenRepository
import com.avilesrodriguez.data.repository.LocalFCMPreferenceRepository
import com.avilesrodriguez.data.repository.MessageRepository
import com.avilesrodriguez.data.repository.ReferralRepository
import com.avilesrodriguez.data.repository.StorageRepository
import com.avilesrodriguez.data.repository.StoreRepository
import com.avilesrodriguez.data.repository.TransactionsRepository
import com.avilesrodriguez.domain.interfaces.IAccountRepository
import com.avilesrodriguez.domain.interfaces.IAuthPreferences
import com.avilesrodriguez.domain.interfaces.IFCMTokenRepository
import com.avilesrodriguez.domain.interfaces.IInternalTokenRepository
import com.avilesrodriguez.domain.interfaces.ILocalFCMPreference
import com.avilesrodriguez.domain.interfaces.IMessageRepository
import com.avilesrodriguez.domain.interfaces.IReferralRepository
import com.avilesrodriguez.domain.interfaces.IStorageRepository
import com.avilesrodriguez.domain.interfaces.IStoreRepository
import com.avilesrodriguez.domain.interfaces.ITransactionsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    
    @Binds
    abstract fun provideAuthPreferencesRepository(impl: AuthPreferencesRepository): IAuthPreferences
    
    @Binds
    abstract fun provideLocalFCMPreferenceRepository(impl: LocalFCMPreferenceRepository): ILocalFCMPreference

    @Binds
    abstract fun provideAccountRepository(impl: AccountRepository): IAccountRepository

    @Binds
    abstract fun provideStoreRepository(impl: StoreRepository): IStoreRepository

    @Binds
    abstract fun provideStorageRepository(impl: StorageRepository): IStorageRepository

    @Binds
    abstract fun provideReferralRepository(impl: ReferralRepository): IReferralRepository

    @Binds
    abstract fun provideMessageRepository(impl: MessageRepository): IMessageRepository

    @Binds
    abstract fun provideIFCMTokenRepository(impl: FCMTokenRepository): IFCMTokenRepository

    @Binds
    abstract fun provideIInternalTokenRepository(impl: InternalTokenRepository): IInternalTokenRepository

    @Binds
    abstract fun provideTransactionsRepository(impl: TransactionsRepository): ITransactionsRepository
}
