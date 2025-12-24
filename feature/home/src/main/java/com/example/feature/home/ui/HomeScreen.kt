package com.example.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.user.UserType
import com.avilesrodriguez.presentation.R

@Composable
fun HomeScreen3(
    openScreen: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) { Text("holaaa")}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    openScreen: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
){
    LaunchedEffect(Unit) {
        viewModel.getUserData()
    }

    val userData by viewModel.userDataStore.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing, // para que no se ponga encima de la parte superior del movil
        topBar = {
            TopAppBar(
                title = {
                    val titleRes = if(userData?.type == UserType.CLIENT)
                        R.string.home_client
                     else
                       R.string.home_provider

                    Text(stringResource(titleRes, userData?.name ?: ""))
                        },
                actions = {}
            )
        },
        content = { innerPadding ->
            val user = userData
            if(user != null){
                when(user.type){
                    UserType.CLIENT -> HomeScreenClient(
                        user = user,
                        modifier = Modifier.padding(innerPadding)
                    )
                    UserType.PROVIDER -> HomeScreenProvider(
                        user = user,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}