package com.example.feature.home.ui.products.editProduct

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
import com.avilesrodriguez.domain.model.productsProvider.ProductProvider
import com.avilesrodriguez.domain.model.validationRules.ProductRules
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.composables.BasicToolbar
import com.avilesrodriguez.presentation.composables.FieldForm
import com.avilesrodriguez.presentation.composables.FormButtons
import com.avilesrodriguez.presentation.composables.ToolBarWithIcon
import com.avilesrodriguez.presentation.ext.fieldModifier
import com.avilesrodriguez.presentation.ext.fieldModifierHeight
import com.avilesrodriguez.presentation.profile.Item


@Composable
fun EditProductScreen(
    productId: String?,
    onBackClick: () -> Unit,
    showTopBar: Boolean = true,
    viewModel: EditProductViewModel = hiltViewModel()
){
    LaunchedEffect(productId){
        viewModel.loadProductInformation(productId.orEmpty())
    }

    val product by viewModel.productState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    EditProductScreenContent(
        product = product,
        onNameChange = viewModel::onNameChange,
        onDescriptionChange = viewModel::updateDescription,
        onPayByReferralChange = viewModel::updatePayByReferral,
        onSaveClick = { viewModel.onUpdateClick(onBackClick) },
        onBackClick = onBackClick,
        showTopBar = showTopBar,
        isLoading = isLoading
    )
}

@Composable
private fun EditProductScreenContent(
    product: ProductProvider,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPayByReferralChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    showTopBar: Boolean,
    isLoading: Boolean
){
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            if(showTopBar){
                ToolBarWithIcon(
                    iconBack = R.drawable.arrow_back,
                    title = stringResource(R.string.edit_product),
                    backClick = { onBackClick() }
                )
            }else {
                BasicToolbar(title = stringResource(R.string.edit_product))
            }
        },
        content = {innerPadding ->
            EditFormProduct(
                product = product,
                onNameChange = onNameChange,
                onDescriptionChange = onDescriptionChange,
                onPayByReferralChange = onPayByReferralChange,
                onSaveClick = onSaveClick,
                onBackClick = onBackClick,
                isLoading = isLoading,
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}

@Composable
private fun EditFormProduct(
    product: ProductProvider,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPayByReferralChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    isLoading: Boolean,
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
            data = product.industry.name,
            modifier = Modifier.fieldModifier()
        )
        Spacer(modifier = Modifier.padding(8.dp))
        FieldForm(
            value = product.name,
            onNewValue = onNameChange,
            namePlaceholder = stringResource(R.string.product_name),
            icon = Icons.Default.CardGiftcard,
            modifier = Modifier.fieldModifier(),
            ruleField = ProductRules.MAX_LENGTH_NAME
        )
        FieldForm(
            value = product.payByReferral,
            onNewValue = onPayByReferralChange,
            namePlaceholder = stringResource(R.string.pay_by_referral),
            icon = Icons.Default.Paid,
            modifier = Modifier.fieldModifier(),
        )
        Spacer(modifier = Modifier.padding(4.dp))
        FieldForm(
            value = product.description,
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
            enabled = product.name.isNotBlank() && product.description.isNotBlank() && product.payByReferral.isNotBlank()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditProductScreenPreview(){
    MaterialTheme {
        EditProductScreenContent(
            product = ProductProvider(),
            onNameChange = {},
            onDescriptionChange = {},
            onPayByReferralChange = {},
            onSaveClick = {},
            onBackClick = {},
            showTopBar = true,
            isLoading = false
        )
    }
}
