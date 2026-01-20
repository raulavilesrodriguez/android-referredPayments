package com.avilesrodriguez.feature.referrals.ui.referrals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData

@Composable
fun ReferralsList(
    onReferralClick: (Referral) -> Unit,
    referrals: List<Referral>,
    user: UserData?,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?,
    modifier: Modifier = Modifier
){
    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(referrals){ referral ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp)
                    .clickable{
                        onReferralClick(referral)
                    }
            ){
                ReferralItem(
                    referral = referral,
                    providerThatReceived = providerThatReceived,
                    clientWhoReferred = clientWhoReferred,
                    user = user
                )
            }
        }
    }
}