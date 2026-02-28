package com.example.feature.home.ui.paymentsMovement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.ToolBarWithIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreenClient(
    client: UserData.Client,
    onBackClick: () -> Unit,
    referrals: List<Referral>,
    isLoading: Boolean,
    showTopBar: Boolean = true // Añadido para soporte adaptativo
){
    Scaffold(
        topBar = {
            if (showTopBar) {
                ToolBarWithIcon(
                    iconBack = R.drawable.arrow_back,
                    title = stringResource(R.string.payments),
                    backClick = { onBackClick() }
                )
            }
        },
        content = { innerPadding ->
            if(isLoading){
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                PaymentsClient(
                    client = client,
                    referrals = referrals,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    )
}

@Composable
private fun PaymentsClient(
    client: UserData.Client,
    referrals: List<Referral>,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hola soy PaymentsClient")
    }
}
