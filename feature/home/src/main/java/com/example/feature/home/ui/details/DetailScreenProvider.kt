package com.example.feature.home.ui.details

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.avatar.Avatar
import com.avilesrodriguez.presentation.composables.ToolBarDetails
import com.avilesrodriguez.presentation.fakeData.userProvider
import com.avilesrodriguez.presentation.industries.label
import java.util.Locale

@Composable
fun DetailScreenProvider(
    provider: UserData.Provider,
    onBackClick: () -> Unit,
    onAddReferClick: (String) -> Unit,
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ToolBarDetails(
                title = R.string.information_provider,
                backClick = { onBackClick() },
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.secondary
                )
            )
        },
        bottomBar = {ButtonToRefer(onReferClick = onAddReferClick, provider = provider)},
        content = { innerPadding ->
            ProfileProvider(
                provider = provider,
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}

@Composable
fun ButtonToRefer(
    onReferClick: (String) -> Unit,
    provider: UserData.Provider,
){
    Surface(
        //tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = { onReferClick(provider.uid) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ){
        Box(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                MaterialTheme.colorScheme.secondary,
            )
        ){
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(provider.industry.label()),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = provider.name ?: stringResource(R.string.unnamed),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Avatar(
                    photoUri = provider.photoUrl,
                    size = 80.dp
                )
            }
        }
        Column(modifier = Modifier.padding(16.dp)){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ){
                DetailMetricItem(
                    icon = Icons.Default.Star,
                    value = String.format(Locale.US, "%.1f", provider.paymentRating),
                    label = stringResource(R.string.rating),
                    tint = Color(0xFFFFC107)
                )
                VerticalDivider(modifier = Modifier.height(40.dp))
                DetailMetricItem(
                    icon = Icons.Default.Payments,
                    value = "${provider.totalPayouts}",
                    label = stringResource(R.string.payouts),
                    tint = Color(0xFFFFC107)
                )
                VerticalDivider(modifier = Modifier.height(40.dp))
                if(provider.isActive){
                    DetailMetricItem(
                        icon = Icons.Default.BrightnessHigh,
                        value ="",
                        label = stringResource(R.string.active_user),
                        tint = Color(0xFFFFC107)
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
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.company_description),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = provider.companyDescription ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            if (!provider.website.isNullOrBlank()) {
                InfoCard(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.website),
                    value = provider.website!!
                )
            }

            InfoCard(
                icon = Icons.Default.Email,
                title = stringResource(R.string.email),
                value = provider.email
            )
            InfoCard(
                icon = Icons.Default.Business,
                title = stringResource(R.string.settings_industry),
                value = stringResource(provider.industry.label())
            )
        }
    }
}

@Composable
fun DetailMetricItem(icon: ImageVector, value: String, label: String, tint: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(4.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun InfoCard(icon: ImageVector, title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
            onAddReferClick = {}
        )
    }
}