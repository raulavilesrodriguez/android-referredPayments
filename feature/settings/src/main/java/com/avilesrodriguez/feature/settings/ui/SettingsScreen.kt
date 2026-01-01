package com.avilesrodriguez.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.avatar.Avatar
import com.avilesrodriguez.presentation.avatar.DEFAULT_AVATAR_USER
import com.avilesrodriguez.presentation.profile.ItemEditProfile
import com.avilesrodriguez.presentation.profile.ItemProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    openScreen: (String) -> Unit,
    restartApp: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
){
    val userData by viewModel.uiState.collectAsState()
    var showDialogDeleteAccount by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.reloadUserData()
    }

    Box(modifier = Modifier.fillMaxSize()){
        Profile(
            userData = userData,
            onDeleteAccountClick = { showDialogDeleteAccount = true }
        )
    }

    if(showDialogDeleteAccount){
        AlertDialog(
            onDismissRequest = { showDialogDeleteAccount = false },
            title = {Text(stringResource(R.string.delete_account))},
            text = {Text(stringResource(R.string.warning_delete_account))},
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialogDeleteAccount = false
                        viewModel.secureDeleteAccount(restartApp)
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialogDeleteAccount = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun Profile(
    userData: UserData?,
    onDeleteAccountClick: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.profile),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable {},
            contentAlignment = Alignment.Center // Center the content
        ) {
            Avatar(
                photoUri = if(userData?.photoUrl.isNullOrBlank()) DEFAULT_AVATAR_USER else userData.photoUrl,
                size = 64.dp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ){
            ItemProfile(icon = R.drawable.name, title = R.string.settings_name, data = userData?.name?:stringResource(R.string.no_information))
            when(userData){
                is UserData.Client -> {
                    ItemProfile(
                        icon = R.drawable.identity_card,
                        title = R.string.settings_identity_card_client,
                        data = userData.identityCard?:stringResource(R.string.no_information))
                    ItemProfile(
                        icon = R.drawable.bank,
                        title = R.string.settings_count_number_bank_client,
                        data = userData.countNumberPay?:stringResource(R.string.no_information)
                    )
                }
                is UserData.Provider -> {
                    ItemProfile(
                        icon = R.drawable.identity_card,
                        title = R.string.settings_identity_card_provider,
                        data = userData.ciOrRuc?: stringResource(R.string.no_information))
                    ItemProfile(
                        icon = R.drawable.bank,
                        title = R.string.settings_count_number_bank_provider,
                        data = userData.countNumber?:stringResource(R.string.no_information)
                    )
                    ItemProfile(
                        icon = R.drawable.industry,
                        title = R.string.settings_industry,
                        data = userData.industry?:stringResource(R.string.no_information)
                    )
                }
                else -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            ItemEditProfile(icon = R.drawable.delete_user, title = R.string.delete_account, data = "") { onDeleteAccountClick() }
        }
    }
}