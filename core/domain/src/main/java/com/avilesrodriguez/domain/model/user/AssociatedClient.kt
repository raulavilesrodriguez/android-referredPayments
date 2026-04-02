package com.avilesrodriguez.domain.model.user

data class AssociatedClient(
    val uid: String = "",
    val name: String = "",
    val nameLowercase: String = "",
    val referralCount: Int,
    val updateAt: Long = System.currentTimeMillis(),
)
