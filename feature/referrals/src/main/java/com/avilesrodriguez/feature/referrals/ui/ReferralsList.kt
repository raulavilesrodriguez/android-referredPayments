package com.avilesrodriguez.feature.referrals.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(referrals){ referral ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable{
                        onReferralClick(referral)
                    }
            ){

            }
        }
    }
}