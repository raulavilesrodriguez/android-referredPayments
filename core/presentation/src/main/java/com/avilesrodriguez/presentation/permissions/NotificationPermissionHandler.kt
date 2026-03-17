package com.avilesrodriguez.presentation.permissions

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.avilesrodriguez.presentation.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionHandler(onDismiss: () -> Unit) {
    val context = LocalContext.current

    @Composable
    fun EnabledToDisableNotificationsDialog(title:String, text:String){
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                    onDismiss()
                }) { Text(stringResource(R.string.go_to_settings)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState = rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS
        )

        // Indica si acabamos de intentar pedir el permiso en esta sesión
        var hasAttemptedRequest by rememberSaveable { mutableStateOf(false) }

        when {
            // CASO 1: Ya tiene el permiso
            permissionState.status.isGranted -> {
                if (hasAttemptedRequest) {
                    // Si lo acaba de aceptar hace un segundo tras el diálogo de Android, cerramos silenciosamente
                    LaunchedEffect(Unit) { onDismiss() }
                } else {
                    // Si ya lo tenía de antes y pulsó la campana, le damos opción de ir a ajustes
                    EnabledToDisableNotificationsDialog(
                        title=stringResource(R.string.notifications_enabled_title),
                        text=stringResource(R.string.notifications_enabled_desc)
                    )
                }
            }

            // CASO 2: El sistema recomienda explicar por qué lo necesitamos (Usuario denegó antes)
            permissionState.status.shouldShowRationale -> {
                AlertDialog(
                    onDismissRequest = onDismiss,
                    title = { Text(stringResource(R.string.notifications_needed_title)) },
                    text = { Text(stringResource(R.string.notifications_needed_desc)) },
                    confirmButton = {
                        TextButton(onClick = {
                            permissionState.launchPermissionRequest()
                            hasAttemptedRequest = true
                        }) { Text(stringResource(R.string.ok)) }
                    },
                    dismissButton = {
                        TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                    }
                )
            }

            // CASO 3: Primera vez O bloqueado permanentemente
            else -> {
                if (!hasAttemptedRequest) {
                    // Lanzamos la petición de Android por primera vez
                    LaunchedEffect(Unit) {
                        permissionState.launchPermissionRequest()
                        hasAttemptedRequest = true
                    }
                } else {
                    // Si ya se lanzó y seguimos aquí, es que el usuario lo denegó permanentemente
                    AlertDialog(
                        onDismissRequest = onDismiss,
                        title = { Text(stringResource(R.string.notifications_blocked_title)) },
                        text = { Text(stringResource(R.string.notifications_blocked_desc)) },
                        confirmButton = {
                            TextButton(onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                                onDismiss()
                            }) { Text(stringResource(R.string.go_to_settings)) }
                        },
                        dismissButton = {
                            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                        }
                    )
                }
            }
        }
    } else {
        EnabledToDisableNotificationsDialog(
            title = stringResource(R.string.notifications),
            text = stringResource(R.string.turn_on_or_turn_off)
        )
    }
}
