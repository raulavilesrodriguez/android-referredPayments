package com.avilesrodriguez.feature.auth.ui.login

import com.avilesrodriguez.domain.model.user.UserType

data class LoginUiState(
    val email: String = "",
    val password: String = ""
)
