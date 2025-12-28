package com.example.feature.home.ui

import androidx.annotation.StringRes
import com.avilesrodriguez.presentation.R

enum class ActionOptionsHome(@param:StringRes val id: Int) {
    POLICIES(R.string.usage_policies),
    SIGN_OUT(R.string.sign_out);

    companion object{
        fun getOptions(): List<Int> {
            return entries.map { it.id }
        }

        fun getById(id: Int): ActionOptionsHome {
            entries.forEach { option ->
                if (option.id == id) return option
            }
            return POLICIES
        }
    }
}