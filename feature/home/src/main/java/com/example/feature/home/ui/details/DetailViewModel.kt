package com.example.feature.home.ui.details

import android.util.Log
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.usecases.GetUser
import com.avilesrodriguez.domain.usecases.HasUser
import com.avilesrodriguez.presentation.navigation.NavRoutes
import com.avilesrodriguez.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getUser: GetUser,
) : BaseViewModel() {
    private val _userDataStore = MutableStateFlow<UserData?>(null)
    val userDataStore: StateFlow<UserData?> = _userDataStore

    fun loadUserInformation(uid: String){
        try {
            launchCatching {
                _userDataStore.value = getUser(uid)
            }
        } catch (ie: Throwable){
            Log.e("DetailViewModel", "Error loading user information", ie)
        }
    }

    fun onAddReferClick(uid: String, openScreen: (String) -> Unit){
        openScreen(NavRoutes.NEW_REFERRAL.replace("{id}", uid))
    }
}