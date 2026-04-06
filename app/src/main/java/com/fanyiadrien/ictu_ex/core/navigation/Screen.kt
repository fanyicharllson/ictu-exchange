package com.fanyiadrien.ictu_ex.core.navigation

/**
 * Single source of truth for all navigation routes in ICTU-Ex.
 */
sealed class Screen(val route: String) {

    // ─── Onboarding ───────────────────────────────────────────────────────────
    object Onboarding   : Screen("onboarding")
    object CheckStatus  : Screen("check_status")

    // ─── Auth ─────────────────────────────────────────────────────────────────
    object SignUp : Screen("sign_up/{userType}") {
        fun createRoute(userType: String) = "sign_up/$userType"
    }
    object SignIn : Screen("sign_in")

    // ─── Main App ─────────────────────────────────────────────────────────────
    object Home         : Screen("home")
    object Search       : Screen("search")
    object Wishlist     : Screen("wishlist")
    object PostItem     : Screen("post_item")
    object Profile      : Screen("profile")
    object EditProfile  : Screen("edit_profile")
    object Settings     : Screen("settings")
    object Camera       : Screen("camera")
    object Cart         : Screen("cart")
    object Notifications: Screen("notifications")

    /** Management Dashboard for Sellers (My Listings) and Buyers (My Orders) */
    object MyActivity : Screen("my_activity")

    object ItemDetail : Screen("item_detail/{listingId}") {
        fun createRoute(listingId: String) = "item_detail/$listingId"
    }
}
