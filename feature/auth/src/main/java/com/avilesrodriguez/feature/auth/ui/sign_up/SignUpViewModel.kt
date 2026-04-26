package com.avilesrodriguez.feature.auth.ui.sign_up

import android.util.Log
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.model.user.UserType
import com.avilesrodriguez.domain.model.validationRules.ValidationRules
import com.avilesrodriguez.domain.usecases.account.CurrentUserId
import com.avilesrodriguez.domain.usecases.user.IsAuthorizedProvider
import com.avilesrodriguez.domain.usecases.user.SaveUser
import com.avilesrodriguez.domain.usecases.authPreferences.SetNotFirstTime
import com.avilesrodriguez.domain.usecases.account.SignUp
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.ext.isValidEmail
import com.avilesrodriguez.presentation.ext.isValidPassword
import com.avilesrodriguez.presentation.ext.passwordMatches
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.snackbar.SnackbarManager
import com.avilesrodriguez.presentation.user.getById
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
    private val saveUser: SaveUser,
    private val setNotFirstTime: SetNotFirstTime
): BaseViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    private val _selectUserType = MutableStateFlow<UserType?>(null)
    val selectUserType: StateFlow<UserType?> = _selectUserType

    private val email
        get() = _uiState.value.email
    private val password
        get() = _uiState.value.password

    private val name
        get() = _uiState.value.name

    fun onNameChange(name: String){
        // Solo deja pasar letras y espacios, eliminando lo demás al instante
        val allowedSymbols = setOf('.', '-', ',', '/')

        val filteredName = name
            .filter { it.isLetter() || it.isDigit() || it.isWhitespace() || allowedSymbols.contains(it) }
            .take(ValidationRules.MAX_LENGTH_NAME)
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

    fun onSelectUserType(userType: Int){
        val userType = UserType.getById(userType)
        _selectUserType.value = userType
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
            signUp(email, password)

            val userType = _selectUserType.value
            val uid = currentUserIdUseCase()

            val newUser = when(userType){
                UserType.CLIENT -> UserData.Client(
                    uid = uid,
                    name = name,
                    email = email
                )
                UserType.PROVIDER -> UserData.Provider(
                    uid = uid,
                    name = name,
                    email = email
                )
                else -> {return@launchCatching}
            }
            saveUser(newUser)
            setNotFirstTime()
            // navigation
            openAndPopUp(NavRoutes.HOME, NavRoutes.SIGN_UP)
        }
    }

    fun onNavigateToSignIn(openAndPopUp: (String, String) -> Unit){
        openAndPopUp(NavRoutes.LOGIN, NavRoutes.SIGN_UP)
    }
}