package com.avilesrodriguez.feature.auth.ui.sign_up

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.user.UserType
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.BasicBottomBar
import com.avilesrodriguez.presentation.composables.BasicButton
import com.avilesrodriguez.presentation.composables.BasicTextButton
import com.avilesrodriguez.presentation.composables.BasicToolbar
import com.avilesrodriguez.presentation.composables.EmailField
import com.avilesrodriguez.presentation.composables.MenuDropdownBox
import com.avilesrodriguez.presentation.composables.NameField
import com.avilesrodriguez.presentation.composables.PasswordField
import com.avilesrodriguez.presentation.composables.RepeatPasswordField
import com.avilesrodriguez.presentation.ext.basicButton
import com.avilesrodriguez.presentation.ext.fieldModifier
import com.avilesrodriguez.presentation.ext.textButton
import com.avilesrodriguez.presentation.user.label
import com.avilesrodriguez.presentation.user.options


@Composable
fun SignUpScreen(
    openAndPopUp: (String, String) -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsState()
    val selectUserType by viewModel.selectUserType.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            BasicToolbar(stringResource(R.string.sign_up))
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
                onNavigateToSignIn = {viewModel.onNavigateToSignIn(openAndPopUp)},
                onUserTypeChange = viewModel::onSelectUserType,
                selectUserType = selectUserType?.label(),
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
    onNavigateToSignIn: () -> Unit,
    onUserTypeChange: (Int) -> Unit,
    selectUserType: Int?,
    modifier: Modifier = Modifier
){
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val userOptions = UserType.options()
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = if(isLandscape) Arrangement.Top else Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
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
        MenuDropdownBox(
            options = userOptions,
            selectedOption = selectUserType?: R.string.choose_account_type,
            title = R.string.account,
            onClick = onUserTypeChange,
            modifier = Modifier.fieldModifier()
        )
        NameField(uiState.name, onNameChange, Modifier.fieldModifier(), R.string.placeholder_name)
        EmailField(uiState.email, onEmailChange, Modifier.fieldModifier())
        PasswordField(uiState.password, onPasswordChange, Modifier.fieldModifier())
        RepeatPasswordField(uiState.repeatPassword, onRepeatPasswordChange, Modifier.fieldModifier())
        BasicButton(text = R.string.sign_up, modifier = Modifier.basicButton()) {
            focusManager.clearFocus()
            keyboardController?.hide()
            onSignUpClick()
        }
        BasicTextButton(R.string.navigate_sign_in, Modifier.textButton()) {
            onNavigateToSignIn()
        }
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
            onSignUpClick = {},
            onNavigateToSignIn = {},
            onUserTypeChange = {},
            selectUserType = null
        )
    }
}