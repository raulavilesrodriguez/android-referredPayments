package com.example.feature.home.ui.products.addProduct

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.domain.model.validationRules.ProductRules
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.BasicToolbar
import com.avilesrodriguez.presentation.composables.FieldForm
import com.avilesrodriguez.presentation.composables.FormButtons
import com.avilesrodriguez.presentation.composables.ToolBarWithIcon
import com.avilesrodriguez.presentation.ext.fieldModifier
import com.avilesrodriguez.presentation.ext.fieldModifierHeight
import com.avilesrodriguez.presentation.fakeData.userProvider
import com.avilesrodriguez.presentation.profile.Item
import com.example.feature.home.ui.products.model.AddProduct

@Composable
fun AddProductScreen(
    providerId: String?,
    onBackClick: () -> Unit,
    showTopBar: Boolean = true,
    viewModel: AddProductViewModel = hiltViewModel()
){
    LaunchedEffect(providerId) {
        viewModel.loadInformation(providerId.orEmpty())
    }
    val addProduct by viewModel.addProduct.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val providerUser by viewModel.providerUser.collectAsState()

    AddProductScreenContent(
        onNameChange = viewModel::onNameChange,
        onDescriptionChange = viewModel::updateDescription,
        onPayByReferralChange = viewModel::updatePayByReferral,
        onSaveClick = { viewModel.onSaveClick( onBackClick) },
        onBackClick = onBackClick,
        addProduct = addProduct,
        isLoading = isLoading,
        showTopBar = showTopBar,
        providerUser = providerUser as UserData.Provider?
    )
}

@Composable
fun AddProductScreenContent(
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPayByReferralChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    addProduct: AddProduct,
    isLoading: Boolean,
    showTopBar: Boolean,
    providerUser: UserData.Provider?
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            if(showTopBar){
                ToolBarWithIcon(
                    iconBack = R.drawable.arrow_back,
                    title = stringResource(R.string.add_new_product),
                    backClick = { onBackClick() }
                )
            }else {
                BasicToolbar(title = stringResource(R.string.add_new_product))
            }
        },
        content = { innerPadding ->
            FormAddProduct(
                onNameChange = onNameChange,
                onDescriptionChange = onDescriptionChange,
                onPayByReferralChange = onPayByReferralChange,
                onSaveClick = onSaveClick,
                onBackClick = onBackClick,
                addProduct = addProduct,
                isLoading = isLoading,
                providerUser = providerUser,
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}


@Composable
private fun FormAddProduct(
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPayByReferralChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    addProduct: AddProduct,
    isLoading: Boolean,
    providerUser: UserData.Provider?,
    modifier: Modifier = Modifier
){
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Item(
            icon = R.drawable.industry,
            title = R.string.settings_industry,
            data = providerUser?.industry?.name ?:"",
            modifier = Modifier.fieldModifier()
        )
        Spacer(modifier = Modifier.padding(8.dp))
        FieldForm(
            value = addProduct.name,
            onNewValue = onNameChange,
            namePlaceholder = stringResource(R.string.product_name),
            icon = Icons.Default.CardGiftcard,
            modifier = Modifier.fieldModifier(),
            ruleField = ProductRules.MAX_LENGTH_NAME
        )
        FieldForm(
            value = addProduct.payByReferral,
            onNewValue = onPayByReferralChange,
            namePlaceholder = stringResource(R.string.pay_by_referral),
            icon = Icons.Default.Paid,
            modifier = Modifier.fieldModifier()
        )
        Spacer(modifier = Modifier.padding(4.dp))
        FieldForm(
            value = addProduct.description,
            onNewValue = onDescriptionChange,
            namePlaceholder = stringResource(R.string.product_description),
            icon = Icons.Default.Description,
            modifier = Modifier.fieldModifier(),
            modifierHeight = Modifier.fieldModifierHeight(),
            ruleField = ProductRules.MAX_LENGTH_PRODUCT_DESCRIPTION
        )
        Spacer(modifier = Modifier.padding(8.dp))
        FormButtons(
            confirmText = R.string.save,
            cancelText = R.string.cancel,
            onConfirm = onSaveClick,
            onCancel = onBackClick,
            isSaving = isLoading,
            enabled = addProduct.name.isNotBlank() && addProduct.description.isNotBlank() && addProduct.payByReferral.isNotBlank()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddProductScreenPreview(){
    MaterialTheme {
        AddProductScreenContent(
            onNameChange = {},
            onDescriptionChange = {},
            onPayByReferralChange = {},
            onSaveClick = {},
            onBackClick = {},
            addProduct = AddProduct(),
            isLoading = false,
            showTopBar = true,
            providerUser = userProvider
        )
    }
}