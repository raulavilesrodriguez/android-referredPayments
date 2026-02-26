package com.example.feature.home.ui.paymentsMovement

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.user.UserData

@Composable
fun PaymentsMovement(
    popUp: () -> Unit,
    viewModel: PaymentsMovementViewModel = hiltViewModel()
) {
    val user by viewModel.userDataStore.collectAsState()
    val referralsProvider by viewModel.referralsProvider.collectAsState()
    val referralsClient by viewModel.referralsClient.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if(user == null){
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        when(user){
            is UserData.Provider -> {
                PaymentsScreenProvider(
                    provider = user as UserData.Provider,
                    onBackClick = popUp,
                    referrals = referralsProvider,
                    isLoading = isLoading
                )
            }
            is UserData.Client -> {
                PaymentsScreenClient(
                    client = user as UserData.Client,
                    onBackClick = popUp,
                    referrals = referralsClient,
                    isLoading = isLoading
                )
            }
            else -> {
                LaunchedEffect(Unit) { popUp() }
            }
        }
    }
}