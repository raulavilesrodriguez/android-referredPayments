package com.avilesrodriguez.presentation.message

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.avilesrodriguez.domain.model.message.MessageSystemKeys
import com.avilesrodriguez.presentation.R

@Composable
fun formatSystemMessage(rawText: String): String {
    if (!rawText.startsWith(MessageSystemKeys.KEY_PREFIX)) return rawText

    // Quitamos el prefijo y separamos por "|" (por si hay argumentos como el nombre del referido)
    val parts = rawText.removePrefix(MessageSystemKeys.KEY_PREFIX).split("|")
    val key = parts[0]
    val argument = if (parts.size > 1) parts[1] else ""

    return when (key) {
        MessageSystemKeys.REFERRAL_ACCEPTED_SUBJECT -> {
            stringResource(R.string.subject_referral_accepted) + " " + argument
        }
        MessageSystemKeys.REFERRAL_ACCEPTED_CONTENT -> {
            stringResource(R.string.content_referral_accepted)
        }
        MessageSystemKeys.REFERRAL_REJECTED_SUBJECT -> {
            stringResource(R.string.subject_referral_rejected)
        }
        MessageSystemKeys.REFERRAL_REJECTED_CONTENT -> {
            stringResource(R.string.content_referral_rejected)
        }
        MessageSystemKeys.REFERRAL_PAID_SUBJECT -> {
            stringResource(R.string.subject_referral_paid)
        }
        MessageSystemKeys.REFERRAL_PAID_CONTENT -> {
            stringResource(R.string.content_referral_paid) + " " + argument
        }
        else -> rawText
    }
}