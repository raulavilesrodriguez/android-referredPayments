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
    ),
    Referral(
        id = "4",
        clientId = "1",
        providerId = "3",
        name = "Gina Cuero",
        nameLowercase = "gina cuero",
        email = "cuero.gina@google.com",
        numberPhone = "0985478901",
        status = ReferralStatus.REJECTED,
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
    status = ReferralStatus.PROCESSING,
    createdAt = System.currentTimeMillis(),
    voucherUrl = ""
)

val userClient = UserData.Client(
    uid = "1u",
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
    moneyEarned = 1000.0
)

val userProvider = UserData.Provider(
    uid = "2u",
    isActive = true,
    name = "Seguros Atlantida",
    email = "support@evicertia.com",
    photoUrl = "https://i.pravatar.cc/150?u=40",
    type = UserType.PROVIDER,
    nameLowercase = "seguros atlantida",
    ciOrRuc = "1234567890123",
    moneyPaid = 15000.0,
    referralsConversion = "0.80",
    industry = IndustriesType.INSURANCE,
    companyDescription = "Seguros Atlantida, a insurance company",
    paymentRating = 4.5,
    totalPayouts = 50,
    website = "https://www.segurosatlantida.ec/personas"
)

val usersProviders = listOf(
    UserData.Provider(
        uid = "2",
        isActive = true,
        name = "Seguros Atlantida",
        email = "support@evicertia.com",
        photoUrl = "https://i.pravatar.cc/150?u=40",
        type = UserType.PROVIDER,
        nameLowercase = "seguros atlantida",
        ciOrRuc = "1234567890123",
        moneyPaid = 15000.0,
        referralsConversion = "0.80",
        industry = IndustriesType.INSURANCE,
        companyDescription = "Seguros Atlantida, a insurance company",
        paymentRating = 4.5,
        totalPayouts = 50,
        website = "https://www.segurosatlantida.ec/personas"
    ),
    UserData.Provider(
        uid = "3",
        isActive = true,
        name = "Optica el Ojo",
        email = "support@ojaso.com",
        photoUrl = "https://i.pravatar.cc/150?u=45",
        type = UserType.PROVIDER,
        nameLowercase = "optica el ojo",
        ciOrRuc = "1234567890199",
        moneyPaid = 8000.0,
        referralsConversion = "0.85",
        industry = IndustriesType.OPTICS,
        companyDescription = "Optica el Ojo, a optics company",
        paymentRating = 4.7,
        totalPayouts = 30,
        website = "https://www.opticalosandes.com.ec/?srsltid=AfmBOoo4EuNV7AfNRWUmWJx_tQFqf--YIamTm_7T-wwoOaff1pNVUbkr"
    ),
    UserData.Provider(
        uid = "4",
        isActive = true,
        name = "Inversiones SP500",
        email = "support@invest500.com",
        photoUrl = "https://i.pravatar.cc/150?u=60",
        type = UserType.PROVIDER,
        nameLowercase = "inversiones sp500",
        ciOrRuc = "1234566990199",
        moneyPaid = 21000.0,
        referralsConversion = "0.9",
        industry = IndustriesType.FINANCIAL,
        companyDescription = "Inversiones SP500, a financial company",
        paymentRating = 4.7,
        totalPayouts = 30,
        website = "https://finance.yahoo.com/"
    ),
    UserData.Provider(
        uid = "5",
        isActive = true,
        name = "Remax Agente",
        email = "angelita.salazar@remax.com",
        photoUrl = "https://i.pravatar.cc/150?u=66",
        type = UserType.PROVIDER,
        nameLowercase = "remax agente",
        ciOrRuc = "1234566990003",
        moneyPaid = 5000.0,
        referralsConversion = "0.75",
        industry = IndustriesType.REAL_ESTATE,
        companyDescription = "Remax Agente, a real estate company",
        paymentRating = 4.2,
        totalPayouts = 15,
        website = "https://www.remax.com.ec/"
    )
)