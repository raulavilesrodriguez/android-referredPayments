package com.avilesrodriguez.feature.referrals.ui.addReferral

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.feature.referrals.ui.model.AddReferralUiState
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.EmailField
import com.avilesrodriguez.presentation.composables.FormButtons
import com.avilesrodriguez.presentation.composables.NameField
import com.avilesrodriguez.presentation.composables.PhoneField
import com.avilesrodriguez.presentation.composables.ProfileToolBar
import com.avilesrodriguez.presentation.ext.fieldModifier

@Composable
fun AddReferralScreen(
    providerId: String,
    openAndPopUp: (String, String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: AddReferralViewModel = hiltViewModel()
){
    val addReferralState by viewModel.addReferralState.collectAsState()
    AddReferralScreenContent(
        onBackClick = onBackClick,
        addReferralState = addReferralState,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onNumberPhoneChange = viewModel::onNumberPhoneChange,
        onSaveClick = { viewModel.onSaveClick(providerId, openAndPopUp) },
        onCancel = { onBackClick() }
    )
}

@Composable
fun AddReferralScreenContent(
    onBackClick: () -> Unit,
    addReferralState: AddReferralUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNumberPhoneChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancel:() -> Unit,
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ProfileToolBar(
                iconBack = R.drawable.arrow_back,
                title = R.string.add_new_referral,
                backClick = { onBackClick() }
            )
        },
        content = { innerPadding ->
            FormAddReferral(
                addReferralState = addReferralState,
                onNameChange = onNameChange,
                onEmailChange = onEmailChange,
                onNumberPhoneChange = onNumberPhoneChange,
                onSaveClick = onSaveClick,
                onCancel = onCancel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}

@Composable
fun FormAddReferral(
    addReferralState: AddReferralUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNumberPhoneChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancel:() -> Unit,
    modifier: Modifier = Modifier
){
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(color = MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        NameField(addReferralState.name, onNameChange, Modifier.fieldModifier(), R.string.name_referred)
        EmailField(addReferralState.email, onEmailChange, Modifier.fieldModifier())
        Spacer(modifier = Modifier.padding(8.dp))
        PhoneField(addReferralState.numberPhone, onNumberPhoneChange, Modifier.fieldModifier())
        FormButtons(R.string.save, R.string.cancel, onSaveClick, onCancel, addReferralState.isSaving, addReferralState.isEntryValid)
        if(!addReferralState.isEntryValid){
            Text(
                text = stringResource(R.string.required_field),
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddReferralScreenPreview(){
    MaterialTheme {
        AddReferralScreenContent(
            onBackClick = {},
            addReferralState = AddReferralUiState(),
            onNameChange = {},
            onEmailChange = {},
            onNumberPhoneChange = {},
            onSaveClick = {},
            onCancel = {}
        )
    }
}