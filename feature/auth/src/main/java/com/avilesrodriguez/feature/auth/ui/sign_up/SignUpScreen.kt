package com.avilesrodriguez.feature.auth.ui.sign_up

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.BasicBottomBar
import com.avilesrodriguez.presentation.composables.BasicButton
import com.avilesrodriguez.presentation.composables.BasicToolbar
import com.avilesrodriguez.presentation.composables.EmailField
import com.avilesrodriguez.presentation.composables.NameField
import com.avilesrodriguez.presentation.composables.PasswordField
import com.avilesrodriguez.presentation.composables.RepeatPasswordField
import com.avilesrodriguez.presentation.ext.basicButton
import com.avilesrodriguez.presentation.ext.fieldModifier


@Composable
fun SignUpScreen(
    openAndPopUp: (String, String) -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            BasicToolbar(R.string.sign_up)
        },
        bottomBar = {
            BasicBottomBar(R.string.made_by)
        },
        content = { innerPadding ->
            SignUpScreenContent(
                uiState = uiState,
                onNameChange = viewModel::onNameChange,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onRepeatPasswordChange = viewModel::onRepeatPasswordChange,
                onSignUpClick = {viewModel.onSignUpClick(openAndPopUp)},
                modifier = Modifier.padding(innerPadding)
            )
        }
    )

}

@Composable
fun SignUpScreenContent(
    uiState: SignUpUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRepeatPasswordChange: (String) -> Unit,
    onSignUpClick: () -> Unit,
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
        NameField(uiState.name, onNameChange, Modifier.fieldModifier())
        EmailField(uiState.email, onEmailChange, Modifier.fieldModifier())
        PasswordField(uiState.password, onPasswordChange, Modifier.fieldModifier())
        RepeatPasswordField(uiState.repeatPassword, onRepeatPasswordChange, Modifier.fieldModifier())
        BasicButton(R.string.sign_up, Modifier.basicButton()) { onSignUpClick() }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview(){
    MaterialTheme {
        SignUpScreenContent(
            uiState = SignUpUiState(),
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onRepeatPasswordChange = {},
            onSignUpClick = {}
        )
    }
}