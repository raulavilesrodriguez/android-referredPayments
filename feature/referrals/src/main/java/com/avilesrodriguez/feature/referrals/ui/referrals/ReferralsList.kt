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
import com.avilesrodriguez.domain.model.referral.ReferralWithNames
import com.avilesrodriguez.domain.model.user.UserData

@Composable
fun ReferralsList(
    onReferralClick: (Referral) -> Unit,
    referrals: List<ReferralWithNames>,
    user: UserData?,
    modifier: Modifier = Modifier
){
    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(referrals){ item ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp)
                    .clickable{
                        onReferralClick(item.referral)
                    }
            ){
                ReferralItem(
                    referral = item.referral,
                    otherPartyName = item.otherPartyName,
                    user = user
                )
            }
        }
    }
}