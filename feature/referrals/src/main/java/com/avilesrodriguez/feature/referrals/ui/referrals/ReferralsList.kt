package com.avilesrodriguez.feature.referrals.ui.referrals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.referral.ReferralWithNames
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.SearchFieldBasic

@Composable
fun ReferralsList(
    searchText: String,
    onValueChange: (String) -> Unit,
    onReferralClick: (Referral) -> Unit,
    referrals: List<ReferralWithNames>,
    user: UserData?,
    modifier: Modifier = Modifier
){
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item{
            //To suppress focus
            Box(
                Modifier
                    .size(0.dp)
                    .focusable()
            )
        }
        item{
            SearchFieldBasic(
                value = searchText,
                onValueChange = onValueChange,
                placeholder = R.string.search_your_referred,
                trailingIcon = R.drawable.search,
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 4.dp)
            )
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
        if(referrals.isEmpty()){
            item{
                Box(
                    modifier = Modifier
                        .fillParentMaxHeight(0.7f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_have_referreds),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }else{
            items(referrals){ item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onReferralClick(item.referral) }
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
}