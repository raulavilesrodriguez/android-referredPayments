package com.avilesrodriguez.data.di

import com.avilesrodriguez.data.datasource.retrofit.PayPhoneApi
import com.avilesrodriguez.data.repository.AccountRepository
import com.avilesrodriguez.data.repository.AuthPreferencesRepository
import com.avilesrodriguez.data.repository.FCMTokenRepository
import com.avilesrodriguez.data.repository.InternalTokenRepository
import com.avilesrodriguez.data.repository.LocalFCMPreferenceRepository
import com.avilesrodriguez.data.repository.MessageRepository
import com.avilesrodriguez.data.repository.PaymentRepository
import com.avilesrodriguez.data.repository.ProductProviderRepository
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
import com.avilesrodriguez.domain.interfaces.IPaymentRepository
import com.avilesrodriguez.domain.interfaces.IProductProviderRepository
import com.avilesrodriguez.domain.interfaces.IReferralRepository
import com.avilesrodriguez.domain.interfaces.IStorageRepository
import com.avilesrodriguez.domain.interfaces.IStoreRepository
import com.avilesrodriguez.domain.interfaces.ITransactionsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

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

    @Binds
    abstract fun provideProductProviderRepository(impl: ProductProviderRepository): IProductProviderRepository
}

@Module
@InstallIn(SingletonComponent::class)
object PaymentModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): okhttp3.OkHttpClient {
        val logging = okhttp3.logging.HttpLoggingInterceptor()
        logging.setLevel(okhttp3.logging.HttpLoggingInterceptor.Level.BODY)
        return okhttp3.OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun providePayPhoneApi(client: okhttp3.OkHttpClient): PayPhoneApi {
        return Retrofit.Builder()
            // Usamos la URL directa del servicio que te dio Firebase
            .baseUrl("https://createpaymentlink-wylj67jajq-uc.a.run.app/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PayPhoneApi::class.java)
    }

    @Provides
    @Singleton
    fun providePaymentRepository(api: PayPhoneApi): IPaymentRepository {
        return PaymentRepository(api)
    }
}
