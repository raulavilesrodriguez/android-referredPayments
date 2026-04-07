package com.avilesrodriguez.presentation.navigation

object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val POLICIES = "policies"
    const val REFERRALS = "referrals"
    const val REFERRALS_ROUTE = "referrals?id={id}" //id del referral
    object ReferralsArgs {
        const val ID = "id"
    }
    const val SETTINGS = "settings"
    const val HOME = "home"
    const val NEW_REFERRAL = "new_referral/{id}?product_id={product_id}"  //id del user provider
    object UserArgs {
        const val ID = "id"
    }
    object ProductArgs {
        const val ID = "product_id"
    }
    const val REFERRAL_DETAIL = "referral_detail/{id}" //id del referral
    const val EDIT_NAME_REFERRAL = "edit_name_referral/{id}" //id del referral
    const val EDIT_EMAIL_REFERRAL = "edit_email_referral/{id}" //id del referral
    const val EDIT_PHONE_REFERRAL = "edit_phone_referral/{id}" //id del referral
    object ReferralArgs {
        const val ID = "id"
    }
    const val MESSAGES_SCREEN = "messages_screen/{id}" //id del referral
}