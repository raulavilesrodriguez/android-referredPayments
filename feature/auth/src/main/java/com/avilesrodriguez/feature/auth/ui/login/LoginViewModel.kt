package com.avilesrodriguez.feature.auth.ui.login

import com.avilesrodriguez.domain.usecases.SendRecoveryEmail
import com.avilesrodriguez.domain.usecases.SetNotFirstTime
import com.avilesrodriguez.domain.usecases.SignIn
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.ext.isValidEmail
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.snackbar.SnackbarManager
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signIn: SignIn,
    private val sendRecoveryEmail: SendRecoveryEmail,
    private val setNotFirstTime: SetNotFirstTime
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private val email
        get() = _uiState.value.email
    private val password
        get() = _uiState.value.password

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onSignInClick(openAndPopUp: (String, String) -> Unit){
        if(!email.isValidEmail()){
            SnackbarManager.showMessage(R.string.email_error)
            return
        }
        if(password.isBlank()){
            SnackbarManager.showMessage(R.string.empty_password_error)
            return
        }

        launchCatching {
            signIn(email, password)
            setNotFirstTime()
            openAndPopUp(NavRoutes.HOME, NavRoutes.LOGIN)
        }
    }

    fun onForgotPasswordClick(){
        if(!email.isValidEmail()){
            SnackbarManager.showMessage(R.string.email_error)
            return
        }
        launchCatching {
            sendRecoveryEmail(email)
            SnackbarManager.showMessage(R.string.recovery_email_sent)
        }
    }

    fun onNavigateToSignUp(openAndPopUp: (String, String) -> Unit){
        openAndPopUp(NavRoutes.SIGN_UP, NavRoutes.LOGIN)
    }

}