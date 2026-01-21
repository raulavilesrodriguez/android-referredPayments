package com.avilesrodriguez.feature.referrals.ui.addReferral

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.ProfileToolBar

@Composable
fun AddReferralScreen(
    openAndPopUp: (String, String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: AddReferralViewModel = hiltViewModel()
){

}

@Composable
fun AddReferralScreenContent(
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
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
                onNameChange = onNameChange,
                onSaveClick = onSaveClick,
                onCancel = onCancel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}

@Composable
fun FormAddReferral(
    onNameChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancel:() -> Unit,
    modifier: Modifier = Modifier
){

}

@Preview(showBackground = true)
@Composable
fun AddReferralScreenPreview(){
    MaterialTheme {}
}