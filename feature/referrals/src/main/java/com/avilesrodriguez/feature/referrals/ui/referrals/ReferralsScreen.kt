package com.avilesrodriguez.feature.referrals.ui.referrals

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.SearchField
import com.avilesrodriguez.presentation.composables.SearchFieldBasic
import com.avilesrodriguez.presentation.composables.SearchToolBarNoBack
import com.avilesrodriguez.presentation.fakeData.generateFakeReferrals
import com.avilesrodriguez.presentation.fakeData.userClient
import com.avilesrodriguez.presentation.fakeData.userProvider

@Composable
fun ReferralsScreen(
    openScreen: (String) -> Unit,
    viewModel: ReferralsViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val user by viewModel.userDataStore.collectAsState()
    val clientWhoReferred = viewModel.clientWhoReferred
    val providerThatReceived = viewModel.providerThatReceived

    ReferralsScreenContent(
        searchText = searchText,
        onValueChange = viewModel::updateSearchText,
        onReferralClick = { referral ->
            viewModel.onReferralClick(referral, openScreen) },
        referrals = uiState,
        user = user,
        isLoading = isLoading,
        clientWhoReferred = clientWhoReferred,
        providerThatReceived = providerThatReceived
    )
}

@Composable
private fun ReferralsScreenContent(
    searchText: String,
    onValueChange: (String) -> Unit,
    onReferralClick: (Referral) -> Unit,
    referrals: List<Referral>,
    user: UserData?,
    isLoading: Boolean,
    clientWhoReferred: UserData?,
    providerThatReceived: UserData?
){
    Column(modifier = Modifier.fillMaxSize()){
        Text(
            text= stringResource(R.string.search_your_referred),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp)
        )
        SearchFieldBasic(
            value = searchText,
            onValueChange = onValueChange,
            placeholder = R.string.select_referred,
            trailingIcon = R.drawable.search,
            modifier = Modifier
                .padding(16.dp)
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()){
            if(!isLoading){
                if(referrals.isNotEmpty()){
                    ReferralsList(
                        onReferralClick = onReferralClick,
                        referrals = referrals,
                        user = user,
                        clientWhoReferred = clientWhoReferred,
                        providerThatReceived = providerThatReceived,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(R.string.no_have_referreds))
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.no_have_referreds))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReferralsScreenPreview(){
    MaterialTheme {
        ReferralsScreenContent(
            searchText = "",
            onValueChange = {},
            onReferralClick = {},
            referrals = generateFakeReferrals(),
            user = userClient,
            isLoading = false,
            clientWhoReferred = userClient,
            providerThatReceived = userProvider
        )
    }
}