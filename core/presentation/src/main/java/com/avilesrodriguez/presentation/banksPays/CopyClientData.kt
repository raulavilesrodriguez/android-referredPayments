package com.avilesrodriguez.presentation.banksPays

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.avilesrodriguez.domain.model.user.UserData
import com.avilesrodriguez.presentation.R

@Composable
fun CopyClientData(
    context: Context,
    userData: UserData
){
    val infoPay = if(userData is UserData.Client) { """
        ${stringResource(R.string.beneficiary)} ${userData.name}
        ${stringResource(R.string.identity_card)} ${userData.identityCard}
        ${stringResource(R.string.bank_name)} ${userData.bankName}
        ${stringResource(R.string.account_type)} ${userData.accountType}
        ${stringResource(R.string.count_number_pay)} ${userData.countNumberPay}
    """.trimIndent()
    } else {""}

    // para copiar en Android
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("infoPay", infoPay)
    clipboard.setPrimaryClip(clip)
}