package com.avilesrodriguez.feature.auth.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.BasicBottomBar
import com.avilesrodriguez.presentation.composables.BasicButton
import com.avilesrodriguez.presentation.composables.BasicTextButton
import com.avilesrodriguez.presentation.composables.BasicToolbar
import com.avilesrodriguez.presentation.composables.EmailField
import com.avilesrodriguez.presentation.composables.PasswordField
import com.avilesrodriguez.presentation.ext.basicButton
import com.avilesrodriguez.presentation.ext.fieldModifier
import com.avilesrodriguez.presentation.ext.textButton

@Composable
fun LoginScreen(
    openAndPopUp: (String, String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            BasicToolbar(R.string.login)
        },
        bottomBar = {
            BasicBottomBar(R.string.made_by)
        },
        content = { innerPadding ->
            LoginScreenContent(
                uiState = uiState,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onSignInClick = { viewModel.onSignInClick(openAndPopUp) },
                onForgotPasswordClick = { viewModel.onForgotPasswordClick() },
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}

@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignInClick: () -> Unit,
    onForgotPasswordClick:() -> Unit,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailField(uiState.email, onEmailChange, Modifier.fieldModifier())
        PasswordField(uiState.password, onPasswordChange, Modifier.fieldModifier())
        BasicButton(R.string.login, Modifier.basicButton()) { onSignInClick() }
        BasicTextButton(R.string.forgot_password, Modifier.textButton()) {
            onForgotPasswordClick()
        }
    }
}