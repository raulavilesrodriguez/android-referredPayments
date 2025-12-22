package com.avilesrodriguez.feature.auth.ui.sign_up

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = ""
)