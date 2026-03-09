package com.example.feature.home.ui.paymentsMovement

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.referral.ReferralWithNames
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.AdvancedSearchSection
import com.avilesrodriguez.presentation.composables.ToolBarWithIcon
import com.avilesrodriguez.presentation.composables.ToolbarPlaceholder
import com.avilesrodriguez.presentation.ext.truncate
import com.avilesrodriguez.presentation.time.formatTime
import com.avilesrodriguez.presentation.time.formatTimeBasic


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreenProvider(
    provider: UserData.Provider,
    onBackClick: () -> Unit,
    referrals: List<ReferralWithNames>,
    isLoading: Boolean,
    dateFrom: Long?,
    dateTo: Long?,
    onDateFromChange: (Long?) -> Unit,
    onDateToChange: (Long?) -> Unit,
    showTopBar: Boolean = true // Añadido para soporte adaptativo
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            if (showTopBar) {
                ToolBarWithIcon(
                    iconBack = R.drawable.arrow_back,
                    title = stringResource(R.string.payments),
                    backClick = { onBackClick() }
                )
            } else {
                ToolbarPlaceholder()
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
                PaymentsProvider(
                    provider = provider,
                    referrals = referrals,
                    dateFrom = dateFrom,
                    dateTo = dateTo,
                    onDateFromChange = onDateFromChange,
                    onDateToChange = onDateToChange,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    )
}

@Composable
private fun PaymentsProvider(
    provider: UserData.Provider,
    referrals: List<ReferralWithNames>,
    dateFrom: Long?,
    dateTo: Long?,
    onDateFromChange: (Long?) -> Unit,
    onDateToChange: (Long?) -> Unit,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ReferralsList(
            provider = provider,
            referrals = referrals,
            dateFrom = dateFrom,
            dateTo = dateTo,
            onDateFromChange = onDateFromChange,
            onDateToChange = onDateToChange
        )
    }
}

@Composable
private fun ReferralsList(
    provider: UserData.Provider,
    referrals: List<ReferralWithNames>,
    dateFrom: Long?,
    dateTo: Long?,
    onDateFromChange: (Long?) -> Unit,
    onDateToChange: (Long?) -> Unit
){
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item{
            AdvancedSearchSection(
                dateFrom = dateFrom,
                dateTo = dateTo,
                onDateFromChange = onDateFromChange,
                onDateToChange = onDateToChange
            )
        }
        items(referrals){ item ->
            if(!referrals.isEmpty()){
                ReferralPaidItem(referral = item, provider = provider)
            }else{
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center){
                    Text(text = stringResource(R.string.no_payments))
                }
            }

        }
    }
}

@Composable
private fun ReferralPaidItem(
    referral: ReferralWithNames,
    provider: UserData.Provider,
){
    val paidAt = formatTimeBasic(referral.referral.paidAt)
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = referral.referral.name.truncate(40),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "USD ${referral.referral.amountPaid}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = paidAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
        if(expanded){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                    text = stringResource(R.string.payments_for, provider.name?:""),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = stringResource(R.string.paid_to, referral.otherPartyName),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                IconButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}