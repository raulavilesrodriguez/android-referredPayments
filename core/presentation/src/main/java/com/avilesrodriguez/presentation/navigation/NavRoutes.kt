package com.avilesrodriguez.presentation.navigation

object NavRoutes {
    const val Splash = "splash"

    const val Login = "login"

    const val SignUp = "sign_up"

    const val EditUser = "edit_user"

    const val Policies = "policies"

    const val Home = "home"

    const val NEW_REFERRAL = "new_referral/{id}"  //id del provider

    const val USER_DETAIL = "user_detail/{id}" //id del user client o provider
    object UserArgs {
        const val ID = "id"
    }

    const val REFERRAL_GRAPH = "referral_graph"
    const val REFERRAL_DETAIL = "referral_detail/{id}" //id del referral
    object ReferralArgs {
        const val ID = "id"
    }

    const val EDIT_NAME_REFERRAL = "edit_name_referral"
    const val EDIT_EMAIL_REFERRAL = "edit_email_referral"
    const val EDIT_PHONE_REFERRAL = "edit_phone_referral"
    const val MESSAGES_GRAPH = "messages_graph"
    const val MESSAGES_SCREEN = "messages_screen/{id}" //id del referral
    const val NEW_MESSAGE = "new_message/{id}" //id del referral
    const val MESSAGE_SCREEN = "message_screen/{id}" //id del message
    object MessageArgs {
        const val ID = "id"
    }

    const val PAY_REFERRAL = "pay_referral"

}