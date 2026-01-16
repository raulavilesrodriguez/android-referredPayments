package com.avilesrodriguez.presentation.navigation

object NavRoutes {
    const val Splash = "splash"

    const val Login = "login"

    const val SignUp = "sign_up"

    const val EditUser = "edit_user"

    const val Policies = "policies"

    const val NewReferral = "new_referral"
    const val ReferralScreenClient = "referral_screen_client"
    const val ReferralScreenProvider = "referral_screen_provider"

    const val Home = "home"

    const val User = "user/{uId}"
    object UserArgs {
        const val uId = "uId"
    }

}