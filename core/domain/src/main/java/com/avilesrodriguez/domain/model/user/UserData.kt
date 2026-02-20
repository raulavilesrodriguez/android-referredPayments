package com.avilesrodriguez.domain.model.user

import com.avilesrodriguez.domain.ext.normalizeName
import com.avilesrodriguez.domain.model.banks.AccountType
import com.avilesrodriguez.domain.model.industries.IndustriesType

sealed class UserData {
    abstract val uid: String
    abstract val isActive: Boolean
    abstract val name: String?
    abstract val email: String
    abstract val photoUrl: String
    abstract val fcmToken: String?
    abstract val type: UserType
    abstract val nameLowercase: String?

    data class Client(
        override val uid: String = "",
        override val isActive: Boolean = true,
        override val name: String? = null,
        override val email: String ="",
        override val photoUrl: String = "",
        override val fcmToken: String? = null,
        override val type: UserType = UserType.CLIENT,
        override val nameLowercase: String? = name?.normalizeName(),
        val identityCard: String? = null, // identity
        val countNumberPay: String? = null, // cuenta para recibir pagos
        val bankName: String? = null,
        val accountType: AccountType = AccountType.SAVINGS, // Ahorros y corriente
        val moneyEarned: Double = 0.0
    ) : UserData()

    data class Provider(
        override val uid: String = "",
        override val isActive: Boolean = true,
        override val name: String? = null,
        override val email: String = "",
        override val photoUrl: String = "",
        override val fcmToken: String? = null,
        override val type: UserType = UserType.PROVIDER,
        override val nameLowercase: String? = name?.normalizeName(),
        val ciOrRuc: String? = null, // Datos fiscales
        val countNumber: String? = null, // cuenta para pagar
        val moneyPaid: Double = 0.0,
        val referralsConversion: String? = null,
        val industry: IndustriesType = IndustriesType.OTHER,
        val companyDescription: String? = null,
        val paymentRating: Double = 0.0,
        val totalPayouts: Int = 0, // Cuantas veces ha pagado con exito
        val website:String? = null,
    ) : UserData()
}
