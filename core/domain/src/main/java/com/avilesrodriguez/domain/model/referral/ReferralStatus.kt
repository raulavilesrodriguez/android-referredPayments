package com.avilesrodriguez.domain.model.referral

enum class ReferralStatus {
    PENDING, //registrado pero no contactado
    PROCESSING, //el referido adquirio un producto y el provider debe pagar
    PAID, //el provider ya subio el vaucher
    REJECTED; //no aplico para la promocion

    companion object
}