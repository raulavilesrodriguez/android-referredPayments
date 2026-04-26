package com.avilesrodriguez.feature.auth.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.avilesrodriguez.presentation.ext.textButton2

@Composable
fun LoginScreen(
    openAndPopUp: (String, String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            BasicToolbar(stringResource(R.string.login))
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
                onNavigateToSignUp = { viewModel.onNavigateToSignUp(openAndPopUp) },
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
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier
){
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = if(isLandscape) Arrangement.Top else Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(!isLandscape){
            Image(
                painter = painterResource(id = R.drawable.logo_app),
                contentDescription = "Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFF0C2B4E), shape = CircleShape)
                    .clip(CircleShape)
            )
        }
        Spacer(Modifier.height(4.dp))
        EmailField(uiState.email, onEmailChange, Modifier.fieldModifier())
        PasswordField(uiState.password, onPasswordChange, Modifier.fieldModifier())
        BasicButton(text = R.string.login, modifier = Modifier.basicButton()) {
            focusManager.clearFocus()
            keyboardController?.hide()
            onSignInClick()
        }
        BasicTextButton(R.string.forgot_password, Modifier.textButton2()) {
            onForgotPasswordClick()
        }
        BasicTextButton(R.string.navigate_sign_up, Modifier.textButton2()) {
            onNavigateToSignUp()
        }
    }
}