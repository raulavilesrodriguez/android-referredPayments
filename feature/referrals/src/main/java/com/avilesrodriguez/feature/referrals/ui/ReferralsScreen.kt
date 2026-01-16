package com.avilesrodriguez.feature.referrals.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.avilesrodriguez.domain.model.referral.ReferralStatus
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.SearchField
import com.avilesrodriguez.presentation.composables.SearchToolBarNoBack

@Composable
fun ReferralsScreen(
    openScreen: (String) -> Unit,
    user: UserData?,
    viewModel: ReferralViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    ReferralsScreenContent(
        searchText = searchText,
        onValueChange = viewModel::updateSearchText,
        onReferralClick = { referral ->
            viewModel.onReferralClick(referral, openScreen) },
        referrals = uiState,
        user = user,
        isLoading = isLoading
    )
}

@Composable
private fun ReferralsScreenContent(
    searchText: String,
    onValueChange: (String) -> Unit,
    onReferralClick: (Referral) -> Unit,
    referrals: List<Referral>,
    user: UserData?,
    isLoading: Boolean
){
    var showSearchField by remember { mutableStateOf(false) }
    var showToolBar by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()){
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
                    },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
            showToolBar -> {
                SearchToolBarNoBack(
                    title = R.string.select_referred,
                    iconSearch = R.drawable.search,
                    iconSearchClick = {
                        showSearchField = true
                        showToolBar = false
                    },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .align(Alignment.TopCenter),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        if(!isLoading){
            if(referrals.isNotEmpty()){
                ReferralsList(
                    onReferralClick = onReferralClick,
                    referrals = referrals,
                    user = user,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.Center)
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

@Preview(showBackground = true)
@Composable
fun ReferralsScreenPreview(){
    MaterialTheme {

    }
}

private fun generateFakeReferrals(): List<Referral> = listOf(
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
        email = "juana.liceo@examplepetstore.com",
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
        status = ReferralStatus.COMPLETED,
        createdAt = System.currentTimeMillis(),
        voucherUrl = ""
    )
)