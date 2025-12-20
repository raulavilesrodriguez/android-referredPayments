package com.avilesrodriguez.presentation.ext

import java.text.Normalizer
import java.util.regex.Pattern

private const val MIN_PASS_LENGTH = 10
private const val ECUADOR_MOBILE_PATTERN = "^09\\d{8}$"
private const val ONLY_NUMBERS_PATTERN = "^\\d+$"

fun String.isValidNumber(): Boolean {
    return this.isNotBlank() &&
            this.length == MIN_PASS_LENGTH &&
            Pattern.compile(ECUADOR_MOBILE_PATTERN).matcher(this).matches()
}

fun String.isOnlyNumbers(): Boolean {
    return Pattern.compile(ONLY_NUMBERS_PATTERN).matcher(this).matches()
}

fun String.normalizeName(): String {
    // 1. Pasar a minúsculas
    var result = this.lowercase()
    // 2. Eliminar tildes/acentos
    result = Normalizer.normalize(result, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    // 3. Reemplazar múltiples espacios por uno solo
    result = result.replace("\\s+".toRegex(), " ")
    // 4. Eliminar espacios al inicio y fin
    return result.trim()
}

fun String.truncate(maxLength: Int): String {
    return if (this.length > maxLength) {
        this.substring(0, maxLength) + "..."
    } else {
        this
    }
}