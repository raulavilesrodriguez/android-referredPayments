package com.example.feature.home.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.avatar.Avatar
import com.avilesrodriguez.presentation.composables.StatItem
import com.avilesrodriguez.presentation.composables.ToolBarWithIcon
import com.avilesrodriguez.presentation.details.DetailMetricItem
import com.avilesrodriguez.presentation.fakeData.userProvider
import com.avilesrodriguez.presentation.industries.label
import java.util.Locale

@Composable
fun DetailScreenProvider(
    provider: UserData.Provider,
    onBackClick: () -> Unit,
    onAddReferClick: (String) -> Unit,
    isSaturated: Boolean,
    canReferUserClient: Boolean,
    showTopBar: Boolean = true
){
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            if (showTopBar) {
                ToolBarWithIcon(
                    iconBack = R.drawable.arrow_back,
                    title = stringResource(R.string.information_provider),
                    backClick = { onBackClick() }
                )
            }
        },
        bottomBar = {
            ButtonToRefer(
                onReferClick = onAddReferClick,
                provider = provider,
                isSaturated = isSaturated,
                canReferUserClient = canReferUserClient
            )},
        content = { innerPadding ->
            ProfileProvider(
                provider = provider,
                isSaturated = isSaturated,
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}

@Composable
fun ButtonToRefer(
    onReferClick: (String) -> Unit,
    provider: UserData.Provider,
    isSaturated: Boolean,
    canReferUserClient: Boolean
){
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 12.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = { onReferClick(provider.uid) },
            enabled = !isSaturated && canReferUserClient,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.refer_now),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProfileProvider(
    provider: UserData.Provider,
    isSaturated: Boolean,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp)
    ){
        ElevatedCard(
            modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Avatar(photoUri = provider.photoUrl, size = 90.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = provider.name ?: stringResource(R.string.unnamed),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = stringResource(provider.industry.label()),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ){
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ){
                DetailMetricItem(
                    icon = Icons.Default.Star,
                    value = String.format(Locale.US, "%.1f", provider.paymentRating),
                    label = stringResource(R.string.rating),
                    tint = Color(0xFF0F2854)
                )
                VerticalDivider(modifier = Modifier.height(40.dp), thickness = 1.dp)
                DetailMetricItem(
                    icon = Icons.Default.Payments,
                    value = "${provider.totalPayouts}",
                    label = stringResource(R.string.payouts),
                    tint = Color(0xFF1C4D8D)
                )
                VerticalDivider(modifier = Modifier.height(40.dp))
                if(provider.isActive){
                    DetailMetricItem(
                        icon = Icons.Default.BrightnessHigh,
                        value ="",
                        label = stringResource(R.string.active_user),
                        tint = Color(0xFF08CB00)
                    )
                } else {
                    DetailMetricItem(
                        icon = Icons.Default.Brightness2,
                        value ="",
                        label = stringResource(R.string.inactive_user),
                        tint = Color(0xFFC40C0C)
                    )
                }
            }
        }
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.company_description),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = provider.companyDescription ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
        }
        if (!provider.website.isNullOrBlank()) {
            InfoCard(
                icon = Icons.Default.Language,
                title = stringResource(R.string.website),
                value = provider.website!!
            )
        }
        /**
        InfoCard(
            icon = Icons.Default.Email,
            title = stringResource(R.string.email),
            value = provider.email
        ) */
        InfoCard(
            icon = Icons.Default.Business,
            title = stringResource(R.string.settings_industry),
            value = stringResource(provider.industry.label())
        )
        if(isSaturated){
            StatItem(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 16.dp),
                title = stringResource(R.string.provider_saturated),
                value = stringResource(R.string.warning),
                icon = Icons.Default.WarningAmber,
                color = MaterialTheme.colorScheme.errorContainer
            )
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun InfoCard(icon: ImageVector, title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenProviderPreview(){
    MaterialTheme {
        DetailScreenProvider(
            provider = userProvider,
            onBackClick = {},
            onAddReferClick = {},
            isSaturated = true,
            canReferUserClient = true
        )
    }
}
