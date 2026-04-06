package com.fanyiadrien.ictu_ex.core.navigation

sealed class Screen(val route: String) {

    object Onboarding    : Screen("onboarding")
    object CheckStatus   : Screen("check_status")

    object SignUp : Screen("sign_up/{userType}") {
        fun createRoute(userType: String) = "sign_up/$userType"
    }
    object SignIn : Screen("sign_in")

    object Home          : Screen("home")
    object Search        : Screen("search")
    object Wishlist      : Screen("wishlist")
    object PostItem      : Screen("post_item")
    object Profile       : Screen("profile")
    object EditProfile   : Screen("edit_profile")
    object Settings      : Screen("settings")
    object Camera        : Screen("camera")
    object Cart          : Screen("cart")
    object Notifications : Screen("notifications")

    // Chat list — shows all conversations the current user is part of
    object ChatList : Screen("chat_list")

    // Individual chat — opened directly with a threadId
    object Chat : Screen("chat/{threadId}") {
        fun createRoute(threadId: String) = "chat/$threadId"
    }

    // Legacy deep-link entry: open/create a thread from ItemDetail then go to Chat
    object Messages : Screen("messages") {
        const val sellerIdArg  = "sellerId"
        const val listingIdArg = "listingId"
        const val routeWithArgs = "messages?$sellerIdArg={$sellerIdArg}&$listingIdArg={$listingIdArg}"
        fun createRoute(sellerId: String? = null, listingId: String? = null): String {
            val params = buildList {
                if (!sellerId.isNullOrBlank())  add("$sellerIdArg=$sellerId")
                if (!listingId.isNullOrBlank()) add("$listingIdArg=$listingId")
            }
            return if (params.isEmpty()) route else "$route?${params.joinToString("&")}"
        }
    }
    object MyActivity    : Screen("my_activity")

    object ItemDetail : Screen("item_detail/{listingId}") {
        fun createRoute(listingId: String) = "item_detail/$listingId"
    }
}
