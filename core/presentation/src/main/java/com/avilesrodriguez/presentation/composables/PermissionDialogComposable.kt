package com.avilesrodriguez.presentation.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.avilesrodriguez.presentation.R
import com.avilesrodriguez.presentation.ext.alertDialog
import com.avilesrodriguez.presentation.ext.textButton

@Composable
fun PermissionDialog(onRequestPermission: () -> Unit){
    var showWarningDialog by remember { mutableStateOf(true) }

    if (showWarningDialog) {
        AlertDialog(
            modifier = Modifier.alertDialog(),
            title = { Text(stringResource(id = R.string.notification_permission_title))},
            text = {Text(stringResource(id = R.string.notification_permission_settings))},
            confirmButton = {
                TextButton(
                    onClick = {
                        onRequestPermission()
                        showWarningDialog = false
                    },
                    modifier = Modifier.textButton(),
                    colors = ButtonDefaults.textButtonColors()
                ) { Text(text = stringResource(R.string.request_notification_permission)) }
            },
            onDismissRequest = { }
        )
    }
}

@Composable
fun RationaleDialog() {
    var showWarningDialog by remember { mutableStateOf(true) }

    if (showWarningDialog) {
        AlertDialog(
            modifier = Modifier.alertDialog(),
            title = { Text(stringResource(id = R.string.notification_permission_title)) },
            text = { Text(stringResource(id = R.string.notification_permission_settings)) },
            confirmButton = {
                TextButton(
                    onClick = { showWarningDialog = false },
                    modifier = Modifier.textButton(),
                    colors = ButtonDefaults.textButtonColors()
                ) { Text(text = stringResource(R.string.ok)) }
            },
            onDismissRequest = { showWarningDialog = false }
        )
    }
}