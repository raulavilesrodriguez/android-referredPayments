package com.avilesrodriguez.presentation.ext

import android.util.Patterns
import java.util.regex.Pattern

const val MAX_LENGTH_NAME = 30
const val MAX_LENGTH_INDUSTRY = 20
const val MAX_LENGTH_IDENTITY_CARD = 10
const val MAX_LENGTH_RUC = 13
const val MAX_LENGTH_COUNT_NUMBER_BANK = 20

private const val MIN_PASS_LENGTH = 6
private const val PASS_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{$MIN_PASS_LENGTH,}$"
private const val ONLY_NUMBERS_PATTERN = "^\\d+$"

private const val  NAME_PATTERN = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$"

const val MIN_PASS_LENGTH_PHONE_ECUADOR = 10
private const val ECUADOR_MOBILE_PATTERN = "^09\\d{8}$"

fun String.isValidEmail(): Boolean {
    return this.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPassword(): Boolean {
    return this.isNotBlank() &&
            this.length >= MIN_PASS_LENGTH &&
            Pattern.compile(PASS_PATTERN).matcher(this).matches()
}

fun String.passwordMatches(repeated: String): Boolean {
    return this == repeated
}

fun String.isValidNumber(): Boolean {
    return this.isNotBlank() &&
            this.length == MIN_PASS_LENGTH_PHONE_ECUADOR &&
            Pattern.compile(ECUADOR_MOBILE_PATTERN).matcher(this).matches()
}

fun String.isValidName(): Boolean {
    return this.isNotBlank()
            && this.length <= MAX_LENGTH_NAME &&
            Pattern.compile(NAME_PATTERN).matcher(this).matches()
}


fun String.isOnlyNumbers(): Boolean {
    return Pattern.compile(ONLY_NUMBERS_PATTERN).matcher(this).matches()
}

fun String.truncate(maxLength: Int): String {
    return if (this.length > maxLength) {
        this.substring(0, maxLength) + "..."
    } else {
        this
    }
}