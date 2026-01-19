package com.avilesrodriguez.feature.referrals.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avilesrodriguez.domain.model.industries.IndustriesType
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.model.user.UserType
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.ext.truncate
import com.avilesrodriguez.presentation.time.formatTimestamp

@Composable
fun ReferralItem(
    referral: Referral,
    providerThatReceived: UserData?,
    clientWhoReferred: UserData?,
    user: UserData?,
){
    val nameUserToShow = when(user){
        is UserData.Client -> {
            val nameProvider = providerThatReceived?.name?.truncate(30)?:""
            stringResource(R.string.referring_to, nameProvider)
        }
        is UserData.Provider -> {
            val nameClient = clientWhoReferred?.name?.truncate(30)?:""
            stringResource(R.string.referred_from, nameClient)
        }
        else -> ""
    }
    val createdAt = formatTimestamp(referral.createdAt)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ){
        Column {
            Text(
                text = referral.name.truncate(20),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 18.sp
            )
            Text(
                text = nameUserToShow,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = stringResource(R.string.created, createdAt),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            val status = when(referral.status){
                ReferralStatus.PENDING -> referral.status.name
                ReferralStatus.COMPLETED -> referral.status.name
                ReferralStatus.REJECTED -> referral.status.name
                ReferralStatus.PAID -> referral.status.name
            }
            val colorBackground = when(referral.status){
                ReferralStatus.PENDING -> Color(0xFFF5AD18)
                ReferralStatus.COMPLETED -> Color(0xFF6594B1)
                ReferralStatus.REJECTED -> Color(0XFFDC0E0E)
                ReferralStatus.PAID -> Color(0xFF08CB00)
            }
            Box(
                modifier = Modifier
                    .background(colorBackground, shape = RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = status,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ReferralItemClientPreview(){
    MaterialTheme {
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
        val providerThatReceived = UserData.Provider(
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

        ReferralItem(
            referral = referral,
            providerThatReceived = providerThatReceived,
            clientWhoReferred = userClient,
            user = userProvider
        )
    }
}