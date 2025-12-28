package com.avilesrodriguez.presentation.navigation

object NavRoutes {
    const val Splash = "splash"

    const val Login = "login"

    const val SignUp = "sign_up"

    const val Settings = "settings"

    const val Policies = "policies"

    const val NewReferral = "new_referral"

    const val Home = "home"

    const val User = "user/{uId}"
    object UserArgs {
        const val uId = "uId"
    }

    const val EditUser = "edit_user"
    const val EditName = "edit_name"
}