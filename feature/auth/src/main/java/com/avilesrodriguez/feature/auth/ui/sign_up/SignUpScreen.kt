package com.avilesrodriguez.feature.auth.ui.sign_up

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel


@Composable
fun SignUpScreen(
    openAndPopUp: (String, String) -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsState()


}

@Composable
fun SignUpScreenContent(

){

}