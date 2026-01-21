package com.avilesrodriguez.presentation.fakeData

import com.avilesrodriguez.domain.model.industries.IndustriesType
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.model.user.UserType

fun generateFakeReferrals(): List<Referral> = listOf(
    Referral(
        id = "1",
        clientId = "1",
        providerId = "2",
        name = "Juan Perez",
        nameLowercase = "juan perez",
        email = "john.hessin.clarke@examplepetstore.com",
        numberPhone = "0987654321",
        status = ReferralStatus.PENDING,
        createdAt = System.currentTimeMillis(),
        voucherUrl = ""
    ),
    Referral(
        id = "2",
        clientId = "1",
        providerId = "2",
        name = "Juana Liceo",
        nameLowercase = "juana liceo",
        email = "juana.liceo@petstore.com",
        numberPhone = "0999654321",
        status = ReferralStatus.PAID,
        createdAt = System.currentTimeMillis(),
        voucherUrl = ""
    ),
    Referral(
        id = "3",
        clientId = "1",
        providerId = "3",
        name = "Mariam Pinilla",
        nameLowercase = "mariam pinilla",
        email = "mariam12@google.com",
        numberPhone = "0989678503",
        status = ReferralStatus.PROCESSING,
        createdAt = System.currentTimeMillis(),
        voucherUrl = ""
    )
)

val referral = Referral(
    id = "2",
    clientId = "1",
    providerId = "2",
    name = "Juana Liceo",
    nameLowercase = "juana liceo",
    email = "juana.liceo@petstore.com",
    numberPhone = "0999654321",
    status = ReferralStatus.PENDING,
    createdAt = System.currentTimeMillis(),
    voucherUrl = ""
)

val userClient = UserData.Client(
    uid = "1",
    isActive = true,
    name = "Brayan Muelas",
    email = "byron@gmail.com",
    photoUrl = "https://i.pravatar.cc/150?u=2",
    type = UserType.CLIENT,
    nameLowercase = "brayan muelas",
    identityCard = "1098765432",
    countNumberPay = "12223440455",
    bankName = "Produbanco",
    accountType = "Ahorros",
    moneyEarned = "1000",
    moneyReceived = "950",
    totalReferrals = 10,
    pendingPayments = 1
)

val userProvider = UserData.Provider(
    uid = "2",
    isActive = true,
    name = "Seguros Atlantida",
    email = "support@evicertia.com",
    photoUrl = "https://i.pravatar.cc/150?u=40",
    type = UserType.PROVIDER,
    nameLowercase = "seguros atlantida",
    ciOrRuc = "1234567890123",
    moneyPaid = "15000",
    moneyToPay = "1000",
    referralsConversion = "0.80",
    industry = IndustriesType.INSURANCE,
    companyDescription = "Seguros Atlantida, a insurance company",
    paymentRating = 4.5,
    totalPayouts = 50,
    website = "https://www.segurosatlantida.ec/personas"
)