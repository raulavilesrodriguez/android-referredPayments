package com.avilesrodriguez.presentation.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BasicTextButton(@StringRes text: Int, modifier: Modifier, action: () -> Unit) {
    TextButton(onClick = action, modifier = modifier) { Text(text = stringResource(text)) }
}

@Composable
fun BasicButton(@StringRes text: Int, modifier: Modifier, argument: String? = null, action: () -> Unit) {
    Button(
        onClick = action,
        modifier = modifier,
    ) {
        Text(text = stringResource(text, argument?:""), fontSize = 16.sp)
    }
}

@Composable
fun ButtonWithIcon(@StringRes text: Int, @DrawableRes icon: Int, modifier: Modifier, argument: String? = null, action: () -> Unit) {
    Button(
        onClick = action,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(painter = painterResource(id = icon), contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(text = stringResource(text, argument?:""), fontSize = 16.sp)
    }
}

@Composable
fun FormButtons(
    @StringRes confirmText: Int,
    @StringRes cancelText: Int,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    isSaving: Boolean = false,
    enabled: Boolean = true
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
        ) {
            Text(
                text = stringResource(cancelText),
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(Modifier.width(16.dp))
        Button(
            onClick = onConfirm,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
        ) {
            if(isSaving){
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            }else{
                Text(
                    text = stringResource(confirmText),
                    style = MaterialTheme.typography.titleMedium
                    )
            }
        }
    }
}

@Composable
fun SaveButton(
    onClick: () -> Unit,
    isFieldValid: Boolean,
    @StringRes text: Int,
    modifier: Modifier = Modifier
){
    Button(
        onClick = onClick,
        enabled = isFieldValid,
        modifier = modifier,
        shape = RoundedCornerShape(50.dp),
    ) {
        Text(
            text = stringResource(text),
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }
}

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