package com.avilesrodriguez.presentation.user

import com.avilesrodriguez.domain.model.user.UserType
import com.avilesrodriguez.presentation.R

fun UserType.label() : Int {
    return when(this){
        UserType.CLIENT -> R.string.refer_clients
        UserType.PROVIDER -> R.string.receive_referrals
    }
}

fun UserType.Companion.getById(id: Int): UserType {
    return UserType.entries.find { option ->
        option.label() == id
    } ?: UserType.CLIENT
}

fun UserType.Companion.options(): List<Int> {
    return UserType.entries.map { it.label() }
}