package com.jlss.smartDairy.navigation

import android.app.Application
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jlss.smartDairy.screen.*
import com.jlss.smartDairy.viewmodel.AppLockViewModel
import com.jlss.smartDairy.viewmodel.AppStateViewModel
import com.jlss.smartDairy.viewmodel.SharedViewModel

@RequiresApi(35)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current.applicationContext as Application

    val lockVm: AppLockViewModel = viewModel(factory = AppLockViewModelFactory(context))
    val stateVm: AppStateViewModel = viewModel(factory = AppStateViewModelFactory(context))
    val sharedVm: SharedViewModel = viewModel()

    val isAccountCreated by stateVm.isAccountCreated.collectAsState(initial = false)
    val isAppLocked by stateVm.isAppLocked.collectAsState(initial = true)

    val startDestination = when {
        !isAccountCreated -> Screen.WelcomeScreen.route
        isAppLocked -> Screen.AppLockScreen.route
        else -> Screen.MainScaffold.route
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.WelcomeScreen.route) {
            WelcomeScreen {
                navController.navigate(Screen.AccountCreationScreen.route)
            }
        }

        composable(Screen.EntryViewScreen.route) {
            EntryViewScreen(
                entryList = sharedVm.selectedEntries,
                navController = navController
            )
        }

        composable(Screen.AccountCreationScreen.route) {
            AccountCreationScreen(
                onAccountCreated = {
                    stateVm.markAccountCreated()
                    navController.navigate(Screen.MainScaffold.route) {
                        popUpTo(Screen.WelcomeScreen.route) { inclusive = true }
                    }
                },
                viewModelLock = lockVm
            )
        }

        composable(Screen.AppLockScreen.route) {
            AppLockScreen(
                onUnlocked = {
                    stateVm.unlockApp()
                    navController.navigate(Screen.MainScaffold.route) {
                        popUpTo(Screen.AppLockScreen.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.MemberDetail.route,
            arguments = listOf(navArgument("memberId") { type = NavType.LongType })
        ) { backStack ->
            val memberId = backStack.arguments!!.getLong("memberId")
            MemberDetailScreen(memberId = memberId, navController = navController)
        }

        composable(Screen.MainScaffold.route) {
            MainScaffold(
                navController = navController,
                sharedVm = sharedVm
            )
        }

        composable(Screen.EntryListScreen.route) {
            EntryListScreen(
                navController = navController,
                sharedVm = sharedVm
            )
        }

        composable(Screen.ProfileScreen.route) {
            ProfileScreen(navController)
        }
        composable(Screen.UserGuideScreen.route) {
            UserGuideScreen()
        }

        composable(
            route = Screen.ProductionEntryScreen.route, // <- Check this
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        )
        { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: return@composable
            ProductionEntryScreen(entryId = entryId, navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        composable(Screen.HomeScreen.route) {
            HomeScreen(navController)
        }

        composable(Screen.SetRateScreen.route) {
            SetRatesScreen()
        }

        composable(Screen.EntryScreen.route) {
            EntryScreen(
                navController = navController,
                onSaved = {
                    navController.navigate(Screen.MainScaffold.route)
                }
            )
        }
    }
}
