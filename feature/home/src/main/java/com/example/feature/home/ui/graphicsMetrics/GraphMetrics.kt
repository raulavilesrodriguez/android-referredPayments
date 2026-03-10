package com.example.feature.home.ui.graphicsMetrics

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
fun GraphMetrics(
    popUp: () -> Unit,
    showTopBar: Boolean = true,
    viewModel: GraphViewModel = hiltViewModel()
){
    val user by viewModel.userDataStore.collectAsState()
    val metrics by viewModel.uiStateReferralsMetrics.collectAsState()
    val referralConversionProvider by viewModel.referralsConversionProvider.collectAsState()
    val costByReferralProvider by viewModel.costByReferralProvider.collectAsState()
    val winByReferralClient by viewModel.winByReferralClient.collectAsState()
    val percentageMetrics by viewModel.percentageMetrics.collectAsState()
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
                GraphMetricsProvider(
                    user = user as UserData.Provider,
                    metrics = metrics,
                    referralConversion = referralConversionProvider,
                    costByReferral = costByReferralProvider,
                    percentageMetrics = percentageMetrics,
                    isLoading = isLoading,
                    onBackClick = popUp,
                    showTopBar = showTopBar
                )
            }
            is UserData.Client -> {
                GraphMetricsClient(
                    user = user as UserData.Client,
                    metrics = metrics,
                    winByReferral = winByReferralClient,
                    percentageMetrics = percentageMetrics,
                    isLoading = isLoading,
                    onBackClick = popUp,
                    showTopBar = showTopBar
                )
            }
            else -> {
                LaunchedEffect(Unit) { popUp() }
            }
        }
    }
}