package com.avilesrodriguez.feature.referrals.ui.referrals

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.SearchField
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
    var showSearchField by remember { mutableStateOf(false) }
    var showToolBar by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()){
        when{
            showSearchField -> {
                SearchField(
                    value = searchText,
                    onValueChange = onValueChange,
                    placeholder = R.string.search,
                    leadingIcon = R.drawable.arrow_back,
                    onLeadingIconClick = {
                        showSearchField = false
                        showToolBar = true
                    }
                )
            }
            showToolBar -> {
                SearchToolBarNoBack(
                    title = R.string.select_referred,
                    iconSearch = R.drawable.search,
                    iconSearchClick = {
                        showSearchField = true
                        showToolBar = false
                    }
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier
                .padding(vertical = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
                            .padding(horizontal = 8.dp, vertical = 16.dp)
                    )
                } else {
                    Text(text = stringResource(R.string.no_have_referreds))
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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