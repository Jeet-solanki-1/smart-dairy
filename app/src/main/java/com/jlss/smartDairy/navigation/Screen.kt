package com.jlss.smartDairy.navigation

/**
 * **Screen** - Represents the navigation destinations in the Mukesh dairy app.
 *
 * ## **Core Concept**
 * - Defines a **sealed class** for type-safe navigation within the app.
 * - Uses **object declarations** for navigation routes.
 * - Supports both **static and dynamic routes**.
 *
 * ## **Essence & Logic**
 * - Prevents **hardcoded strings** in navigation.
 * - Ensures **type safety** when passing navigation arguments.
 * - Simplifies navigation logic with **predefined routes**.
 *
 * ## **Technology Stack**
 * - **Jetpack Compose Navigation**: Enables smooth, declarative navigation.
 * - **Sealed Classes**: Restricts subclassing and ensures all screens are defined in one place.
 * - **Parameterized Routes**: Allows screens to receive dynamic data.
 *
 * ## **Goal**
 * - Provide a **structured and maintainable** navigation system.
 * - Ensure **scalability** by allowing easy addition of new screens.
 * - Avoid **string-based navigation errors** by using type-safe routes.
 */
sealed class Screen(val route: String, val title: String? = null) {



    object WelcomeScreen : Screen("welcome_screen", "Welcome")
    object ProfileScreen : Screen("profile_screen", "profile")
    object UserGuideScreen : Screen("user_guide_screen", "User Guide")
    object AccountCreationScreen : Screen("account_creation_screen","Create Account")
    object AppLockScreen : Screen("app_lock_screen","AppLockScreen")

    object MainScaffold  : Screen("main_scaffold","main Screen")
    object EntryViewScreen : Screen("entry_view_screen", "View Entry")
    object MemberDetail  : Screen("member_detail/{memberId}") {
        fun createRoute(id: Long) = "member_detail/$id"
    }

}
