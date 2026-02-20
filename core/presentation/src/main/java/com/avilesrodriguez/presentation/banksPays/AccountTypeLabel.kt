package com.avilesrodriguez.presentation.banksPays

import com.avilesrodriguez.domain.model.banks.AccountType
import com.avilesrodriguez.presentation.R

fun AccountType.label(): Int {
    return when (this) {
        AccountType.SAVINGS -> R.string.savings
        AccountType.CHECKING -> R.string.checking
    }
}

fun AccountType.Companion.getById(id:Int): AccountType{
    return AccountType.entries.find { option ->
        option.label() == id
    } ?: AccountType.SAVINGS
}

fun AccountType.Companion.options(): List<Int>{
    return AccountType.entries.map { it.label() }
}