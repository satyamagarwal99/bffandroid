package com.gobff.getfriends.navigation

sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object UpdateApp : AppRoute("update_app")
    data object Login : AppRoute("login")
    data object Gender : AppRoute("gender")
    data object Audio : AppRoute("audio")
    data object Home : AppRoute("home")
    data object Home2 : AppRoute("home2")
    data object Profile : AppRoute("profile")
    data object Settings : AppRoute("settings")
    data object GiftVibe : AppRoute("gift_vibe")
    data object Wallet : AppRoute("wallet")
    data object Chat : AppRoute("chat")
    data object History : AppRoute("history")
    data object Games : AppRoute("games")
    data object TruthDare : AppRoute("truth_dare")
    data object PersonalChat : AppRoute("personal_chat")
    data object Friends : AppRoute("friends")
    data object Recharge : AppRoute("recharge")
    data object IncomingCall : AppRoute("incoming_call")
    data object Call : AppRoute("call")
}
