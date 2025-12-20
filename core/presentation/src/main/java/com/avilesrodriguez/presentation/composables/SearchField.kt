package com.avilesrodriguez.presentation.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes placeholder: Int,
    @DrawableRes trailingIcon: Int,
    onTrailingIconClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Box(modifier = modifier
        .padding(16.dp)
        .fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it) },
            placeholder = { Text(text = stringResource(id = placeholder)) },
            leadingIcon = {
                IconButton(onClick = onTrailingIconClick) {
                    Icon(
                        painter = painterResource(id = trailingIcon),
                        contentDescription = null
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.outline,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )
    }
}