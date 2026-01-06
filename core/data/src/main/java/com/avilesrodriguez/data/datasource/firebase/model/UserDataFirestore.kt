package com.avilesrodriguez.data.datasource.firebase.model

import com.avilesrodriguez.domain.model.industries.IndustriesType
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.model.user.UserType
import com.google.firebase.firestore.PropertyName

sealed class UserDataFirestore {
    abstract val uid: String?
    abstract val isActive: Boolean?
    abstract val name: String?
    abstract val email: String?
    abstract val photoUrl: String?
    abstract val fcmToken: String?
    abstract val type: String?

    data class Client(
        override val uid: String? = null,
        @get:PropertyName("isActive")
        override val isActive: Boolean? = null,
        override val name: String? = null,
        override val email: String? = null,
        override val photoUrl: String? = null,
        override val fcmToken: String? = null,
        override val type: String? = null,
        val nameLowercase: String? = null,
        val identityCard: String? = null, // identity
        val countNumberPay: String? = null, // cuenta para recibir pagos
        val moneyEarned: String? = null,
        val moneyReceived: String? = null
    ) : UserDataFirestore()

    data class Provider(
        override val uid: String? = null,
        @get:PropertyName("isActive")
        override val isActive: Boolean? = null,
        override val name: String? = null,
        override val email: String? = null,
        override val photoUrl: String? = null,
        override val fcmToken: String? = null,
        override val type: String? = null,
        val nameLowercase: String? = null,
        val ciOrRuc: String? = null, // Datos fiscales
        val countNumber: String? = null, // cuenta para pagar
        val moneyPaid: String? = null,
        val moneyToPay: String? = null,
        val referralsConversion: String? = null,
        val industry: String? = null
    ) : UserDataFirestore()
}

fun UserData.toUserDataFirestore(): UserDataFirestore{
    return when(this){
        is UserData.Client -> UserDataFirestore.Client(
            uid = uid,
            isActive = isActive,
            name = name,
            email = email,
            photoUrl = photoUrl,
            fcmToken = fcmToken,
            type = type.name,
            identityCard = identityCard,
            countNumberPay = countNumberPay,
            moneyEarned = moneyEarned,
            moneyReceived = moneyReceived
        )
        is UserData.Provider -> UserDataFirestore.Provider(
            uid = uid,
            isActive = isActive,
            name = name,
            email = email,
            photoUrl = photoUrl,
            fcmToken = fcmToken,
            type = type.name,
            nameLowercase = nameLowercase,
            ciOrRuc = ciOrRuc,
            countNumber = countNumber,
            moneyPaid = moneyPaid,
            moneyToPay = moneyToPay,
            referralsConversion = referralsConversion,
            industry = industry.name
        )
    }
}

fun UserDataFirestore.toDomain(): UserData? {
    return when (this) {
        is UserDataFirestore.Client -> UserData.Client(
            uid = uid ?: "",
            name = name,
            email = email ?: "",
            photoUrl = photoUrl ?: "",
            fcmToken = fcmToken,
            type = UserType.CLIENT,
            nameLowercase = nameLowercase,
            identityCard = identityCard,
            countNumberPay = countNumberPay,
            moneyEarned = moneyEarned,
            moneyReceived = moneyReceived
        )
        is UserDataFirestore.Provider -> {
            val domainIndustriesType = when (industry?.uppercase()){
                IndustriesType.INSURANCE.name -> IndustriesType.INSURANCE
                IndustriesType.REAL_ESTATE.name -> IndustriesType.REAL_ESTATE
                IndustriesType.OPTICS.name -> IndustriesType.OPTICS
                IndustriesType.FITNESS.name -> IndustriesType.FITNESS
                IndustriesType.HEALTH.name -> IndustriesType.HEALTH
                IndustriesType.BEAUTY.name -> IndustriesType.BEAUTY
                IndustriesType.TRAVEL.name -> IndustriesType.TRAVEL
                IndustriesType.RETAIL.name -> IndustriesType.RETAIL
                IndustriesType.FINANCIAL.name -> IndustriesType.FINANCIAL
                else -> IndustriesType.OTHER
            }
            UserData.Provider(
            uid = uid ?: "",
            name = name,
            email = email ?: "",
            photoUrl = photoUrl ?: "",
            fcmToken = fcmToken,
            type = UserType.PROVIDER,
            nameLowercase = nameLowercase,
            ciOrRuc = ciOrRuc,
            countNumber = countNumber,
            moneyPaid = moneyPaid,
            moneyToPay = moneyToPay,
            referralsConversion = referralsConversion,
            industry = domainIndustriesType
        )}
    }
}

