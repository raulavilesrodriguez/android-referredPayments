package com.avilesrodriguez.feature.settings.ui

import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.CurrentUserId
import com.avilesrodriguez.domain.usecases.DownloadUrlPhoto
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.domain.usecases.SaveUser
import com.avilesrodriguez.domain.usecases.SecureDeleteAccount
import com.avilesrodriguez.domain.usecases.UploadPhoto
import com.avilesrodriguez.presentation.avatar.DEFAULT_AVATAR_USER
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val currentUserIdUseCase: CurrentUserId,
    private val hasUser: HasUser,
    private val getUser: GetUser,
    private val uploadPhoto: UploadPhoto,
    private val downloadPhoto: DownloadUrlPhoto,
    private val saveUser: SaveUser,
    private val secureDeleteAccount: SecureDeleteAccount
) : BaseViewModel() {
    private val _uiState = MutableStateFlow<UserData?>(null)
    val uiState: StateFlow<UserData?> = _uiState
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    val currentUserId
        get() = currentUserIdUseCase()

    init {
        launchCatching {
            if (hasUser()){
                _uiState.value = getUser(currentUserId)
            }
        }
    }

    fun reloadUserData() {
        launchCatching {
            if (hasUser()) {
                _uiState.value = getUser(currentUserId)
            }
        }
    }

    fun secureDeleteAccount(restartApp: (String) -> Unit){
        launchCatching {
            secureDeleteAccount(currentUserId)
            restartApp(NavRoutes.Splash)
        }
    }

    fun updateName(newName: String){
        val currentState = _uiState.value
        if(currentState != null){
            _uiState.value = when(currentState){
                is UserData.Client -> currentState.copy(name = newName)
                is UserData.Provider -> currentState.copy(name = newName)
            }
        }
    }

    fun updatePhoto(newPhotoUri: String){
        val currentState = _uiState.value
        if(currentState != null){
            _uiState.value = when(currentState){
                is UserData.Client -> currentState.copy(photoUrl = newPhotoUri)
                is UserData.Provider -> currentState.copy(photoUrl = newPhotoUri)
            }
        }
    }

    fun updateIndustry(industry: String){
        val currentState = _uiState.value
        if(currentState is UserData.Provider){
            _uiState.value = currentState.copy(industry = industry)
        }
    }

    fun updateIdentityCard(identityCard: String){
        val currentState = _uiState.value
        if(currentState != null){
            _uiState.value = when(currentState){
                is UserData.Client -> currentState.copy(identityCard = identityCard)
                is UserData.Provider -> currentState.copy(ciOrRuc = identityCard)
            }
        }
    }

    fun updateCountNumberBank(countNumberBank: String){
        val currentState = _uiState.value
        if(currentState != null){
            _uiState.value = when(currentState){
                is UserData.Client -> currentState.copy(countNumberPay = countNumberBank)
                is UserData.Provider -> currentState.copy(countNumber = countNumberBank)
            }
        }
    }

    fun onSaveClick(openScreen: (String) -> Unit){
        launchCatching {
            _isSaving.value = true
            val remotePath = "profile_images/${currentUserId}.jpg"

            val newPhotoUri = _uiState.value?.photoUrl ?: DEFAULT_AVATAR_USER

            // Upload photo to Firebase Storage
            uploadPhoto(newPhotoUri, remotePath)

            // Download URL of the uploaded photo
            val finalPhotoUrl = downloadPhoto(remotePath)
            val currentState = _uiState.value
            if (currentState != null) {
                _uiState.value = when (currentState) {
                    is UserData.Client -> currentState.copy(photoUrl = finalPhotoUrl)
                    is UserData.Provider -> currentState.copy(photoUrl = finalPhotoUrl)
                }
            }
            _uiState.value?.let{
                saveUser(it)
            }
            // navigate to settings screen
            openScreen(NavRoutes.Settings)
        }.invokeOnCompletion { _isSaving.value = false }
    }

    fun editUser(openScreen: (String) -> Unit){
        launchCatching {
            openScreen(NavRoutes.EditUser)
        }
    }

}