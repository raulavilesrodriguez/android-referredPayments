package com.avilesrodriguez.presentation.composables

import androidx.annotation.StringRes
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun DialogConfirmButton(@StringRes text: Int, action: () -> Unit) {
    Button(
        onClick = action,
    ) {
        Text(text = stringResource(text))
    }
}

@Composable
fun DialogCancelButton(@StringRes text: Int, action: () -> Unit) {
    Button(
        onClick = action,
    ) {
        Text(text = stringResource(text))
    }
}