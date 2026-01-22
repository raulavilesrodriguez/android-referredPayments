package com.avilesrodriguez.presentation.navigation

object NavRoutes {
    const val Splash = "splash"

    const val Login = "login"

    const val SignUp = "sign_up"

    const val EditUser = "edit_user"

    const val Policies = "policies"

    const val Home = "home"

    const val NEW_REFERRAL = "new_referral/{id}"

    const val USER_DETAIL = "user_detail/{id}"
    object UserArgs {
        const val ID = "id"
    }

    const val REFERRAL_GRAPH = "referral_graph"
    const val REFERRAL_DETAIL = "referral_detail/{id}"
    object ReferralArgs {
        const val ID = "id"
    }

    const val EDIT_NAME_REFERRAL = "edit_name_referral"
    const val EDIT_EMAIL_REFERRAL = "edit_email_referral"
    const val EDIT_PHONE_REFERRAL = "edit_phone_referral"
    const val PAY_REFERRAL = "pay_referral"

}