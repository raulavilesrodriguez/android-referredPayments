package com.example.feature.home.ui.graphicsMetrics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.referral.ReferralMetrics
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.BasicToolbar
import com.avilesrodriguez.presentation.composables.ToolBarWithIcon
import com.avilesrodriguez.presentation.composables.ToolbarPlaceholder
import com.avilesrodriguez.presentation.ext.referralMetricsColors
import com.avilesrodriguez.presentation.ext.referralMetricsLabels
import com.avilesrodriguez.presentation.ext.toList
import com.avilesrodriguez.presentation.fakeData.userProvider
import com.avilesrodriguez.presentation.graphs.BulletsGraphPercentages
import com.avilesrodriguez.presentation.graphs.BulletsGraphProvider
import com.avilesrodriguez.presentation.graphs.DonutGraph
import com.example.feature.home.models.ReferralPercentageMetrics
import com.example.feature.home.models.toList

@Composable
fun GraphMetricsProvider(
    user: UserData.Provider,
    metrics: ReferralMetrics,
    referralConversion: Double,
    costByReferral: Double,
    percentageMetrics: ReferralPercentageMetrics,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    showTopBar: Boolean = true
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            if (showTopBar) {
                ToolBarWithIcon(
                    iconBack = R.drawable.arrow_back,
                    title = stringResource(R.string.statistics),
                    backClick = { onBackClick() }
                )
            } else {
                BasicToolbar(stringResource(R.string.statistics))
            }
        },
        content = {innerPadding ->
            if(isLoading){
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
                }
            } else {
                MetricsProvider(
                    user = user,
                    metrics = metrics,
                    referralConversion = referralConversion,
                    costByReferral = costByReferral,
                    percentageMetrics = percentageMetrics,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    )
}

@Composable
private fun MetricsProvider(
    user: UserData.Provider,
    metrics: ReferralMetrics,
    referralConversion: Double,
    costByReferral: Double,
    percentageMetrics: ReferralPercentageMetrics,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize().padding(8.dp)
            .verticalScroll(rememberScrollState())
    ){
        DonutGraph(
            values = metrics.toList().map { it.toFloat() },
            labels = referralMetricsLabels(),
            colors = referralMetricsColors(),
            modifier = Modifier.padding(8.dp)
        )
        BulletsGraphProvider(
            moneyPaid = user.moneyPaid.toFloat(),
            referralConversion = referralConversion.toFloat(),
            costByReferral = costByReferral.toFloat(),
            modifier = Modifier.padding(8.dp)
        )
        val percentagesMetricsFloat = percentageMetrics.toList().map { it.first.toFloat() to it.second  }
        BulletsGraphPercentages(
            percentagesMetrics = percentagesMetricsFloat,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GraphMetricsProvidePreview(){
    MaterialTheme {
        GraphMetricsProvider(
            user = userProvider,
            metrics = ReferralMetrics(
                totalReferrals = 10,
                pendingReferrals = 3,
                processingReferrals = 2,
                rejectedReferrals = 1,
                paidReferrals = 4
            ),
            referralConversion = 0.4,
            costByReferral = 15.0,
            percentageMetrics = ReferralPercentageMetrics(
                percentagePaid = 0.4,
                percentagePending = 0.2,
                percentageProcessing = 0.2,
                percentageRejected = 0.2
            ),
            isLoading = false,
            onBackClick = {}
        )
    }
}