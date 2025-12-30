package com.avilesrodriguez.feature.settings.ui

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.avatar.Avatar
import com.avilesrodriguez.presentation.avatar.DEFAULT_AVATAR_USER
import com.avilesrodriguez.presentation.composables.ProfileToolBar
import com.avilesrodriguez.presentation.photo.pickImageLauncher

@Composable
fun EditScreen(
    openScreen: (String) -> Unit,
    popUp: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
){
    val isSaving by viewModel.isSaving.collectAsState()
    val userData by viewModel.uiState.collectAsState()

    val imagePicker = pickImageLauncher(
        context = LocalContext.current,
        updatePhotoUri = { newUri ->
            // Cuando la imagen se recorta con éxito, actualizamos el ViewModel
            viewModel.updatePhoto(newUri)
        },
        errorCropping = R.string.error_cropping,
        errorSaving = R.string.error_saving
    )

    Scaffold(
        topBar = {
            ProfileToolBar(
                iconBack = R.drawable.arrow_back,
                title = R.string.edit_profile,
                backClick = popUp
            )
        },
        content = { innerPadding ->
            EditScreenContent(
                userData = userData,
                isSaving = isSaving,
                onNameChange = viewModel::updateName,
                onIndustryChange = viewModel::updateIndustry,
                onIdentityCardChange = viewModel::updateIdentityCard,
                onCountNumberBankChange = viewModel::updateCountNumberBank,
                onPickImageClick = {
                    imagePicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onSaveClick = { viewModel.onSaveClick(openScreen) },
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}

@Composable
fun EditScreenContent(
    userData: UserData?,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onIndustryChange: (String) -> Unit,
    onIdentityCardChange: (String) -> Unit,
    onCountNumberBankChange: (String) -> Unit,
    onPickImageClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(color = MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Box(
            modifier = Modifier
                .size(72.dp)
                .clickable { onPickImageClick() }
        ){
            Avatar(
                photoUri = userData?.photoUrl?: DEFAULT_AVATAR_USER,
                size = 64.dp,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(3.dp)
                    .size(32.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                    .padding(3.dp)
            )
        }


    }
}