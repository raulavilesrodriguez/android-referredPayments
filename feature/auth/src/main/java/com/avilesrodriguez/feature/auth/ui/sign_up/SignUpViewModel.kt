package com.avilesrodriguez.feature.auth.ui.sign_up

import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.model.user.UserType
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.IsAuthorizedProvider
import com.avilesrodriguez.domain.usecases.SaveUser
import com.avilesrodriguez.domain.usecases.SignUp
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.ext.isValidEmail
import com.avilesrodriguez.presentation.ext.isValidPassword
import com.avilesrodriguez.presentation.ext.passwordMatches
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.snackbar.SnackbarManager
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val signUp: SignUp,
    private val isAuthorizedProvider: IsAuthorizedProvider,
    private val saveUser: SaveUser
): BaseViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    private val email
        get() = _uiState.value.email
    private val password
        get() = _uiState.value.password

    fun onNameChange(name: String){
        // Solo deja pasar letras y espacios, eliminando lo demás al instante
        val filteredName = name
            .filter { it.isLetter() || it.isWhitespace() }
            .take(30)
        _uiState.value = _uiState.value.copy(name = filteredName)
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onRepeatPasswordChange(repeatPassword: String) {
        _uiState.value = _uiState.value.copy(repeatPassword = repeatPassword)
    }

    fun onSignUpClick(openAndPopUp: (String, String) -> Unit){
        if(!email.isValidEmail()){
            SnackbarManager.showMessage(R.string.email_error)
            return
        }
        if(!password.isValidPassword()){
            SnackbarManager.showMessage(R.string.password_error)
            return
        }
        if(!password.passwordMatches(_uiState.value.repeatPassword)){
            SnackbarManager.showMessage(R.string.password_match_error)
            return
        }

        launchCatching {
            val isProvider = isAuthorizedProvider(email)
            signUp(email, password)
            val userType = if(isProvider) UserType.PROVIDER else UserType.CLIENT

            val newUser = when(userType){
                UserType.CLIENT -> UserData.Client(
                    uid = currentUserIdUseCase(),
                    name = _uiState.value.name,
                    email = email
                )
                UserType.PROVIDER -> UserData.Provider(
                    uid = currentUserIdUseCase(),
                    name = _uiState.value.name,
                    email = email
                )
            }
            saveUser(newUser)
            // navigation
            openAndPopUp(NavRoutes.Home, NavRoutes.SignUp)
        }
    }
}