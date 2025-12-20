package com.avilesrodriguez.domain.model.user

sealed class UserData {
    abstract val uid: String
    abstract val isActive: Boolean
    abstract val name: String?
    abstract val email: String
    abstract val photoUrl: String
    abstract val fcmToken: String?
    abstract val type: UserType

    data class Client(
        override val uid: String = "",
        override val isActive: Boolean = true,
        override val name: String? = null,
        override val email: String ="",
        override val photoUrl: String = "",
        override val fcmToken: String? = null,
        override val type: UserType = UserType.CLIENT,
        val nameLowercase: String? = null,
        val identityCard: String? = null, // identity
        val countNumberPay: String? = null, // cuenta para recibir pagos
        val moneyEarned: String? = null,
        val moneyReceived: String? = null
    ) : UserData()

    data class Provider(
        override val uid: String = "",
        override val isActive: Boolean = true,
        override val name: String? = null,
        override val email: String = "",
        override val photoUrl: String = "",
        override val fcmToken: String? = null,
        override val type: UserType = UserType.PROVIDER,
        val nameLowercase: String? = null,
        val ciOrRuc: String? = null, // Datos fiscales
        val countNumber: String? = null, // cuenta para pagar
        val moneyPaid: String? = null,
        val moneyToPay: String? = null,
        val referralsConversion: String? = null
    ) : UserData()
}
