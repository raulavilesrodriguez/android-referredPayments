package com.avilesrodriguez.domain.ext

import java.text.Normalizer

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