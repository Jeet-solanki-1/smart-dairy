package com.jlss.smartDairy.navigation

import com.jlss.smartDairy.data.model.ListOfEntry

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
 */sealed class Screen(val route: String) {
    object ProductionEntryScreen : Screen("production_entry_screen/{entryId}") {
        fun createRoute(entryId: Long) = "production_entry_screen/$entryId"
    }

    object Settings : Screen("settings")
    object SetRateScreen : Screen("set_rate_screen")
    object WelcomeScreen : Screen("welcome_screen")
    object ProfileScreen : Screen("profile_screen")
    object AccountCreationScreen : Screen("account_creation_screen")
    object AppLockScreen : Screen("app_lock_screen")

    object MainScaffold : Screen("main_scaffold")
    object EntryViewScreen : Screen("entry_view_screen")
    object EntryScreen : Screen("entry_screen")
    object EntryListScreen : Screen("entries")
    object MemberScreen : Screen("members")
    object HomeScreen : Screen("home")
    object MemberDetail : Screen("member_detail/{memberId}") {
        fun createRoute(id: Long) = "member_detail/$id"
    }
    object UserGuideScreen : Screen("user_guide")
}
