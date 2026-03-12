package com.fanyiadrien.ictu_ex.core.navigation

/**
 * Single source of truth for all navigation routes in ICTU-Ex.
 *
 * Usage from any Composable:
 *   navController.navigate(Screen.SignUp.createRoute("SELLER"))
 *   navController.navigate(Screen.Home.route)
 */
sealed class Screen(val route: String) {

    // ─── Onboarding ───────────────────────────────────────────────────────────
    /** Welcome / splash screen. Shown only on first install. */
    object Onboarding : Screen("onboarding")

    /** "Are you a Buyer or Seller?" picker screen. */
    object CheckStatus : Screen("check_status")

    // ─── Auth ─────────────────────────────────────────────────────────────────
    /**
     * Unified sign-up screen.
     * Receives [userType] as a nav argument: "SELLER" or "BUYER"
     *
     * Navigate to it with:  Screen.SignUp.createRoute("SELLER")
     * Receive arg with:      navBackStackEntry.arguments?.getString("userType")
     */
    object SignUp : Screen("sign_up/{userType}") {
        fun createRoute(userType: String) = "sign_up/$userType"
    }

    /** Sign-in screen for returning users. */
    object SignIn : Screen("sign_in")

    // ─── Main App ─────────────────────────────────────────────────────────────
    /** Main listings feed — root destination after login. */
    object Home : Screen("home")

    /**
     * Item detail screen.
     * Receives [listingId] as a nav argument.
     *
     * Navigate with: Screen.ItemDetail.createRoute("abc123")
     */
    object ItemDetail : Screen("item_detail/{listingId}") {
        fun createRoute(listingId: String) = "item_detail/$listingId"
    }

    /** Post a new item for sale/swap. Seller-only screen. */
    object PostItem : Screen("post_item")

    /** User profile screen. */
    object Profile : Screen("profile")

    /* Settings Screen */
    object Settings: Screen("settings")
}