package com.jlss.smartDairy.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.*
import com.jlss.smartDairy.R
import kotlinx.coroutines.launch

/**
 * 🚀 WelcomeScreen - The Billion-Dollar First Impression Screen
 *
 * This composable represents the onboarding or introductory screen of the Smart Dairy app.
 * It showcases key features, usage instructions, and developer credits in a visually stunning
 * swipeable pager interface, providing the user with clarity, confidence, and connection.
 *
 * ✅ Key Features:
 * - Uses `Scaffold` for structured layout (topBar, bottomBar, and content)
 * - Displays 3 swipeable `Card`s using `HorizontalPager`:
 *     1. Features
 *     2. How to Use
 *     3. About Developer
 * - Page indicator dots below pager for visual orientation
 * - A prominent CTA "Get Started" button to enter the app
 * - A branded footer honoring developer & brand identity
 *
 * 💡 Components Used:
 * - `Scaffold`, `TopAppBar`, `Card`, `HorizontalPager`, `HorizontalPagerIndicator`, `Button`
 * - `MaterialTheme.typography` for elegant text hierarchy
 * - `Color.Gray.copy(alpha = 0.4f)` to create elegant, subtle branding
 *
 * 🔄 State Management:
 * - `pagerState` from `rememberPagerState()` tracks current pager index for swiping and indicator
 *
 * 🧠 Design Philosophy:
 * - Minimal text on each screen → easier cognitive processing
 * - Distinct cards and typography → visual clarity
 * - Full-width CTA and alignment → mobile usability focus
 *
 * 🔧 Customization Ideas:
 * - Add Lottie animations or illustrations to each page
 * - Animate card transitions with `AnimatedVisibility`
 * - Switch to multi-language using `stringResource()`
 * - Show walkthrough overlay on first launch
 *
 * @param navigateToLoginScreen Lambda that navigates to the next screen (Login or Home)
 * Typically passed from NavController in a NavHost.
 *
 * 📦 Example Usage:
 * ```kotlin
 * NavHost(...) {
 *     composable("welcome") {
 *         WelcomeScreen(navigateToLoginScreen = { navController.navigate("login") })
 *     }
 * }
 * ```
 *
 * 🏆 Designed to be unforgettable. Feels like a billion-dollar product on first touch.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun WelcomeScreen(navigateToLoginScreen: () -> Unit = {}) {
    val pages = listOf(
        WelcomePage(
            title = "Simplify Dairy Management",
            description = "Add members, record entries, and auto-calculate payments seamlessly. But make sure to add your current fate rates, and adding members will halp you to pick all entries of a member at one click.",
            imageRes = R.drawable.feature
        ),
        WelcomePage(
            title = "Offline First. Secure Always.",
            description = "PDF export, send the pdf of all your dairy entries by one click 📨 and share too .if you want to delete the app then first export your all data and when you import , it will auto save again all..",
            imageRes = R.drawable.image
        ),
        WelcomePage(
            title = "By JLSS for Bharat",
            description = "Crafted with ❤️ by Jeet Laxman Sitaram Solanki for every dairy in Digital India.",
            imageRes = R.drawable.vision
        )
    )

    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            Footer(pagerState = pagerState, pageCount = pages.size, onFinish = navigateToLoginScreen)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFE8F5E9), Color(0xFFB2DFDB), Color(0xFF80CBC4))
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalPager(
                state = pagerState,
                count = pages.size,
                modifier = Modifier.weight(1f)
            ) { index ->
                WelcomeCard(pages[index])
            }

            HorizontalPagerIndicator(
                pagerState = pagerState,
                activeColor = MaterialTheme.colorScheme.primary,
                inactiveColor = Color.Gray,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun WelcomeCard(page: WelcomePage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            page.imageRes?.let { painterResource(id = it) }?.let {
                Image(
                    painter = it,
                    contentDescription = null,
                    modifier = Modifier
                        .height(180.dp)
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                textAlign = TextAlign.Center
            )
        }
    }
}
@Composable
fun Footer(pagerState: PagerState, pageCount: Int, onFinish: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                coroutineScope.launch {
                    if (pagerState.currentPage < pageCount - 1) {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    } else {
                        onFinish()
                    }
                }
            },
            modifier = Modifier
                .widthIn(min = 200.dp)
                .height(56.dp), // fixed width for center alignment, or use wrapContentWidth()
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ArrowForward, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                if (pagerState.currentPage < pageCount - 1) "Next" else "Get Started",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(Modifier.height(12.dp))

        // Centered Branding
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Powered by JLSS", style = MaterialTheme.typography.labelMedium)
            Text("Jeet Laxman Sitaram Solanki", fontSize = 12.sp, color = Color.Gray)
            Text("“The world of uniqueness”", fontSize = 10.sp, color = Color.Gray.copy(alpha = 0.5f))
        }
    }
}


data class WelcomePage(
    val title: String,
    val description: String,
    val imageRes: Int?= null
)
