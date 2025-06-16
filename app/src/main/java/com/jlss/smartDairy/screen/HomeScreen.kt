package com.jlss.smartDairy.screen


import androidx.compose.foundation.layout.*

import androidx.compose.material.icons.Icons

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.dp

import androidx.lifecycle.viewmodel.compose.viewModel
import com.jlss.smartDairy.component.MenuCard
import com.jlss.smartDairy.viewmodel.UserViewModel
import androidx.compose.foundation.background

import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NoteAdd

import androidx.compose.ui.graphics.Brush

import androidx.navigation.NavController
import com.jlss.smartDairy.R

import com.google.accompanist.pager.*
import com.jlss.smartDairy.navigation.Screen

import java.time.LocalTime

import kotlinx.coroutines.delay

fun getTimeGreeting(): String {
    val hour = LocalTime.now().hour
    return when (hour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..20 -> "Good Evening"
        else -> "Good Night"
    }
}

/**
 * 📄 HomeScreen.kt — Smart Dairy App
 * ----------------------------------------------------------
 * 🧠 Purpose:
 * This screen acts as the **Dashboard** of the Smart Dairy app.
 * It enables you to:
 * 1. View and update the current milk fat rate.
 * 2. Export app data (entries + members or only members).
 * 3. Import data using:
 *    - File Picker (user selects file via storage UI)
 *    - Auto-Scan from known locations
 *
 * 🔍 ViewModel Used:
 * - HomeViewModel:
 *   Manages business logic and data for rate, export, and import functionalities.
 *   Automatically retained across recompositions by `viewModel()`.

 * 📦 Dependencies:
 * - Jetpack Compose Material3
 * - ActivityResultContracts for file operations
 * - Android permissions
 * - LocalContext for Toasts and access to application resources

 * 📚 Concepts Covered:
 * ----------------------------------------------------------
 * ▸ remember / mutableStateOf → To hold UI state
 * ▸ collectAsState() → Convert Flow from ViewModel to Composable state
 * ▸ AlertDialog → UI popup for export file naming
 * ▸ ActivityResultContracts → Manage permission and file picking
 * ▸ Dynamic file list loading & selection using ExposedDropdownMenuBox
 * ▸ Toasts → For user feedback

 * 🧩 UI Layout:
 * - Column (Vertical scrolling area)
 * - Multiple `Card`s used to visually segment features
 * - Intuitive icons and spacing to improve UX

 * 🔁 State Variables:
 * - input: Text input for rate
 * - showExportDialog: Boolean to control dialog visibility
 * - exportFileName: Name for export file
 * - selectedFile: Currently picked file from device storage
 * - fileOptions: List of discovered `.txt` files on the device

 * 🛡️ Permissions:
 * - For API < 33 (TIRAMISU), asks `READ_EXTERNAL_STORAGE`
 * - Uses launcher to request and handle result

 * 🛠️ Composables & Contracts Used:
 * - rememberLauncherForActivityResult → File picker and permission requests
 * - AlertDialog → Custom dialog for entering export file name
 * - ExposedDropdownMenuBox → Modern dropdown UI
 * - Material3 Components → Text, Buttons, Cards, etc.

 * 🧪 Test Cases (Conceptual):
 * - Enter a valid rate → Button updates ViewModel and shows toast
 * - Pick file using picker → ViewModel imports from URI
 * - Scan files → Device files populate dropdown
 * - Select file and click import → JSON parsed and applied

 * 🔄 Data Flow Summary:
 * ┌──────────────┐        ┌─────────────┐        ┌──────────────┐
 * │ UI Actions   ├──────▶ │ ViewModel   ├──────▶ │ Room + Files │
 * └──────────────┘        └─────────────┘        └──────────────┘
 *      ▲                       ▼                        ▲
 *      └──────── collectAsState() ◀──── observe State ──┘

 * 🧠 Why This Structure?
 * - Uses modern Compose best practices
 * - Highly modular — easy to replace individual cards with composables later
 * - Clean architecture separation between UI, ViewModel, and data access

 * 🧠 Common Confusions Addressed:
 * 1. ❓ Why is `remember` used?
 *    🔹 It keeps the variable alive across recompositions. Needed for `input`, `selectedFile`, etc.
 *
 * 2. ❓ How does the ViewModel get initialized?
 *    🔹 Using `viewModel()` from `androidx.lifecycle.viewmodel.compose`, which automatically scopes it to the NavGraph/Activity.
 *
 * 3. ❓ Why is `.collectAsState()` needed for `rate`?
 *    🔹 Because `rate` is a `StateFlow` in the ViewModel. We must convert it to Compose state to observe.

 * 4. ❓ Why use `AlertDialog` instead of another screen?
 *    🔹 Because we just need a temporary popup with an input — not a full screen.

 * 5. ❓ Can I export as JSON instead of TXT?
 *    🔹 Yes, just change the extension and write logic in `exportAllData()` and `exportOnlyMembers()` in the ViewModel.

 * 🚀 What Else Can I Do Here?
 * ----------------------------------------------------------
 * ▸ 🔁 Auto-backup: Schedule daily auto-backup using WorkManager
 * ▸ ☁️ Cloud sync: Allow upload/download to Google Drive using SAF or Drive API
 * ▸ 🗂 Categorize entries: Export separate files for morning/evening shifts
 * ▸ 🔍 File preview: Show preview of file content before importing
 * ▸ 🧠 AI-enhanced import: Validate data using AI OCR or intelligent parsing

 * 💡 Pro Tip for Future:
 * - Create a separate `ExportImportSection()` composable to keep code more modular.
 * - Use `LazyColumn` if cards become scrollable later.
 * - Replace static toasts with `SnackbarHost` scoped to a `Scaffold` for better UX.

 */


@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    userVm: UserViewModel = viewModel()
) {
    val user by userVm.user.collectAsState(initial = null)
    val userName = user?.name ?: "Guest"
    val greeting = getTimeGreeting()
    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

// Auto-scroll every 3 seconds


    val pages = listOf(
        // 1️⃣ Initial setup: members & fat rate
        WelcomePage(
            title       = "Start by Adding Members & Fat Rates",
            description = """
            1. Tap “Load Members” to pull in—or manually Add—your dairy members.
            2. Go to HomeScreen  → Set Rates, and enter your Buying‑Fat and Factory‑Fat rates and milk selling rates.
            3. Only after both members and rates are in place you can record milk entries accurately.
        """.trimIndent(),
            imageRes    = null
        ),

        // 2️⃣ Daily usage: record & backup
        WelcomePage(
            title       = "Record Daily Collections & Print Reports",
            description = """
            • Every day, enter each member’s milk & fat % in the table.
            • Tap 💾 Save to persist data offline.
            • Use the “Print Report” button to generate & share today’s collection—so even if the app is deleted, you have a PDF backup.
        """.trimIndent(),
            imageRes    = null
        ),

        // 3️⃣ Data portability: export / import
        WelcomePage(
            title       = "Export / Import to Secure & Share Data",
            description = """
            • Tap Export to dump all your members + entries into a Text or Json file.
            • Store it safely or share with colleagues.
            • Import that file on any device to restore everything—no manual re‑entry needed.
        """.trimIndent(),
            imageRes    = null
        ),

        // 4️⃣ Upcoming features preview
        WelcomePage(
            title       = "New Features Coming Soon!",
            description = """
            • Voice‑based data entry: speak names, milks & fats in one go.
            • Scheduled weekly/monthly summary reports.
            • Optional server sync: back up your data in the cloud.
            • And many more AI‑powered insights for your dairy business.
        """.trimIndent(),
            imageRes    = null
        ),

        // 5️⃣ Community & follow
        WelcomePage(
            title       = "Follow JLSS—Join the Uniqueness",
            description = """
            💖 Stay updated: Follow JLSS .
            🌐 Join our community of digital transforming India’s capital .
        """.trimIndent(),
            imageRes    = null
        )
    )

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(3000)
            val nextPage = (pagerState.currentPage + 1) % pages.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFE8F5E9), Color(0xFFB2DFDB), Color(0xFF80CBC4))
                    )
                ),
            verticalArrangement = Arrangement.Top
        ) {
            /** Greeting + Horizontal Notice Board **/
            Text(
                text = "👋 $greeting, $userName!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            HorizontalPager(
                state = pagerState,
                count = pages.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) { index ->
                WelcomeCard(pages[index])
            }


            HorizontalPagerIndicator(
                pagerState = pagerState,
                activeColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp)
            )

            Spacer(Modifier.height(20.dp))

            /** Action Menu Cards **/
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuCard(
                    title = "Collect Milk 🐄",
                    description = "Tap here to enter milk & fat entries. Quick entry with voice & camera support!",
                    icon = Icons.Default.NoteAdd,
                    onClick = { navController.navigate(Screen.EntryScreen.route) }
                )

                MenuCard(
                    title = "Set Rates 💰",
                    description = "Set your fat rate, factory selling rate & milk price for payment accuracy.",
                    icon = Icons.Default.MonetizationOn,
                    onClick = { navController.navigate(Screen.SetRateScreen.route) }
                )
            }


        }
    }
}


