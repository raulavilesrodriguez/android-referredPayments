package com.example.feature.home.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.user.UserData

@Composable
fun DetailScreenUser(
    uId: String?,
    popUp: () -> Unit,
    openScreen: (String) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
){
    val userData by viewModel.userDataStore.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserInformation(uId)
    }
    when(userData){
        is UserData.Provider -> {
            DetailScreenProvider(
                provider = userData as UserData.Provider,
                onBackClick = popUp,
                onAddReferClick = {uid -> viewModel.onAddReferClick(uid, openScreen)}
            )
        }
        is UserData.Client -> {
            DetailScreenClient(
                client = userData as UserData.Client,
                onBackClick = popUp,
                onReferClick = {id -> viewModel.onReferClick(id, openScreen)}
            )
        }
        else -> {
            popUp()
        }
    }
}

