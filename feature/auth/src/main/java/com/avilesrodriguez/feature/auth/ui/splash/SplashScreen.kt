package com.avilesrodriguez.feature.auth.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R

@Composable
fun SplashScreen(
    openAndPopUp: (String, String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
){
    // Escuchar eventos de navegación
    LaunchedEffect(viewModel.navigationEvent) {
        viewModel.navigationEvent.collect { (route, popUp) ->
            openAndPopUp(route, popUp)
        }
    }

    // Iniciar lógica de login solo una vez
    LaunchedEffect(Unit) {
        viewModel.alreadyLoggedIn()
    }

    val userData by viewModel.userDataStore.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    SplashScreenContent()

    if (isLoading) {
        LoadBottomSheet(
            userData = userData
        )
    }
}

@Composable
fun SplashScreenContent(){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textToShow = stringResource(R.string.welcome)
            Text(
                text = textToShow,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            
            Image(
                painter = painterResource(id = R.drawable.logo_app),
                contentDescription = "Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFF0C2B4E), shape=CircleShape)
                    .clip(CircleShape)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadBottomSheet(
    userData: UserData?
){
    ModalBottomSheet(
        onDismissRequest = { },
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Transparent, // Evita que el fondo se ponga gris
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 640.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textToShow = userData?.name?.let {
                stringResource(R.string.welcome_name, it)
            } ?: ""
            
            if (textToShow.isNotEmpty()) {
                Text(
                    text = textToShow,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
            
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 6.dp,
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp)
            )
        }
    }
}
