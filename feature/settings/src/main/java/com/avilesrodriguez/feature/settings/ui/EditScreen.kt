package com.avilesrodriguez.feature.settings.ui

import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.avatar.Avatar
import com.avilesrodriguez.presentation.avatar.DEFAULT_AVATAR_USER
import com.avilesrodriguez.presentation.composables.FormButtons
import com.avilesrodriguez.presentation.composables.ProfileToolBar
import com.avilesrodriguez.presentation.composables.TextFieldProfile
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_COUNT_NUMBER_BANK
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_IDENTITY_CARD
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_INDUSTRY
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_NAME
import com.avilesrodriguez.presentation.ext.MAX_LENGTH_RUC
import com.avilesrodriguez.presentation.ext.fieldModifier
import com.avilesrodriguez.presentation.photo.pickImageLauncher

@Composable
fun EditScreen(
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
                onSaveClick = { viewModel.onSaveClick(popUp) },
                onCancel = { viewModel.cancelEditUser(popUp) },
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
    onCancel:() -> Unit,
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
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable { onPickImageClick() }
        ){
            Avatar(
                photoUri = if(userData?.photoUrl.isNullOrBlank()) DEFAULT_AVATAR_USER else userData.photoUrl,
                size = 72.dp,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(8.dp)
                    .size(21.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                    .padding(2.dp)
            )
        }

        TextFieldProfile(
            value = userData?.name?:"",
            onNewValue = onNameChange,
            maxLength = MAX_LENGTH_NAME,
            icon = R.drawable.name,
            title = R.string.settings_name,
            Modifier.fieldModifier()
        )
        when(userData){
            is UserData.Client -> {
                TextFieldProfile(
                    value = userData.identityCard?:"",
                    onNewValue = onIdentityCardChange,
                    maxLength = MAX_LENGTH_IDENTITY_CARD,
                    icon = R.drawable.identity_card,
                    title = R.string.settings_identity_card_client,
                    Modifier.fieldModifier()
                )
                TextFieldProfile(
                    value = userData.countNumberPay?:"",
                    onNewValue = onCountNumberBankChange,
                    maxLength = MAX_LENGTH_COUNT_NUMBER_BANK,
                    icon = R.drawable.bank,
                    title = R.string.settings_count_number_bank_client,
                    modifier = Modifier.fieldModifier()
                )
            }
            is UserData.Provider -> {
                TextFieldProfile(
                    value = userData.ciOrRuc?: "",
                    onNewValue = onIdentityCardChange,
                    maxLength = MAX_LENGTH_RUC,
                    icon = R.drawable.identity_card,
                    title = R.string.settings_identity_card_provider,
                    modifier = Modifier.fieldModifier()
                )
                TextFieldProfile(
                    value = userData.countNumber?: "",
                    onNewValue = onCountNumberBankChange,
                    maxLength = MAX_LENGTH_COUNT_NUMBER_BANK,
                    icon = R.drawable.bank,
                    title = R.string.settings_count_number_bank_provider,
                    modifier = Modifier.fieldModifier()
                )
                TextFieldProfile(
                    value = userData.industry?: "",
                    onNewValue = onIndustryChange,
                    maxLength = MAX_LENGTH_INDUSTRY,
                    icon = R.drawable.industry,
                    title = R.string.settings_industry,
                    modifier = Modifier.fieldModifier()
                )
            }
            else -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        FormButtons(R.string.save, R.string.cancel, onSaveClick, onCancel, isSaving)

    }
}

@Preview(showBackground = true)
@Composable
fun EditScreenContenPreview(){
    MaterialTheme {
        EditScreenContent(
            userData = UserData.Client(
                name = "Raúl Avilés Rodríguez",
                identityCard = "",
                countNumberPay = ""
            ),
            isSaving = false,
            onNameChange = {},
            onIndustryChange = {},
            onIdentityCardChange = {},
            onCountNumberBankChange = {},
            onPickImageClick = {},
            onSaveClick = {},
            onCancel = {}
        )
    }
}