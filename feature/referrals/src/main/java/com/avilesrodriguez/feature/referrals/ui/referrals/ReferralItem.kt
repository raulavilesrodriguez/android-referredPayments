package com.avilesrodriguez.feature.referrals.ui.referrals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.ext.toColor
import com.avilesrodriguez.presentation.ext.toDisplayName
import com.avilesrodriguez.presentation.ext.truncate
import com.avilesrodriguez.presentation.fakeData.referral
import com.avilesrodriguez.presentation.fakeData.userClient
import com.avilesrodriguez.presentation.fakeData.userProvider
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
            val status = referral.status.toDisplayName()
            val colorBackground = referral.status.toColor()
            Icon(
                imageVector = Icons.Default.Circle,
                contentDescription = null,
                tint = colorBackground,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(status),
                style = MaterialTheme.typography.bodySmall,
                color = colorBackground
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ReferralItemClientPreview(){
    MaterialTheme {
        ReferralItem(
            referral = referral,
            providerThatReceived = userProvider,
            clientWhoReferred = userClient,
            user = userProvider
        )
    }
}