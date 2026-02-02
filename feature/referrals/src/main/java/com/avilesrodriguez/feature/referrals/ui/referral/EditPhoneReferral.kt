package com.avilesrodriguez.feature.referrals.ui.referral

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.referral.Referral
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.PhoneFieldCursor
import com.avilesrodriguez.presentation.composables.SaveButton
import com.avilesrodriguez.presentation.composables.ToolBarWithIcon
import com.avilesrodriguez.presentation.fakeData.referral

@Composable
fun EditPhoneReferral(
    onBackClick: () -> Unit,
    viewModel: ReferralViewModel = hiltViewModel()
){
    val referral by viewModel.referralState.collectAsState()
    EditPhoneReferralContent(
        referral = referral,
        onBackClick = onBackClick,
        updateNumberPhone = viewModel::updateNumberPhone,
        onSaveClick = { viewModel.onSaveNumberPhone { onBackClick() }}
    )
}

@Composable
private fun EditPhoneReferralContent(
    referral: Referral,
    onBackClick: () -> Unit,
    updateNumberPhone: (String) -> Unit,
    onSaveClick: () -> Unit
){
    Scaffold(
        topBar = {
            ToolBarWithIcon(
                iconBack = R.drawable.arrow_back,
                title = R.string.phone_number_referred,
                backClick = { onBackClick() }
            )
        },
        content = { innerPadding ->
            PhoneContent(
                referral = referral,
                updateNumberPhone = updateNumberPhone,
                onSaveClick = onSaveClick,
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}

@Composable
private fun PhoneContent(
    referral: Referral,
    updateNumberPhone: (String) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
){
    //local configuration of mobile device
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    if(isLandscape){
        LandscapePhoneContent(
            referral = referral,
            updateNumberPhone = updateNumberPhone,
            onSaveClick = onSaveClick,
            focusRequester = focusRequester,
            modifier = modifier
        )
    } else {
        PortraitPhoneContent(
            referral = referral,
            updateNumberPhone = updateNumberPhone,
            onSaveClick = onSaveClick,
            focusRequester = focusRequester,
            modifier = modifier
        )
    }
}

@Composable
private fun PortraitPhoneContent(
    referral: Referral,
    updateNumberPhone: (String) -> Unit,
    onSaveClick: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
){
    Box(modifier = modifier
        .fillMaxSize()
        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp)
    ){
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.End
        ) {
            PhoneFieldCursor(
                value = referral.numberPhone,
                onNewValue = updateNumberPhone,
                focusRequester = focusRequester,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(R.string.comment_save_phone_number_referred),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                textAlign = TextAlign.Start
            )
        }
        SaveButton(
            onClick = onSaveClick,
            isFieldValid = referral.numberPhone.isNotBlank(),
            text = R.string.save,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun LandscapePhoneContent(
    referral: Referral,
    updateNumberPhone: (String) -> Unit,
    onSaveClick: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            PhoneFieldCursor(
                value = referral.numberPhone,
                onNewValue = updateNumberPhone,
                focusRequester = focusRequester,
                modifier = Modifier
                    .weight(0.7f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            SaveButton(
                onClick = onSaveClick,
                isFieldValid = referral.numberPhone.isNotBlank(),
                text = R.string.save,
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 16.dp)
                    .weight(0.3f)
            )
        }
        Text(
            text = stringResource(R.string.comment_save_phone_number_referred),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            textAlign = TextAlign.Start
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditPhoneReferralPreview(){
    MaterialTheme {
        EditPhoneReferralContent(
            referral = referral,
            onBackClick = {},
            updateNumberPhone = {},
            onSaveClick = {}
        )
    }
}