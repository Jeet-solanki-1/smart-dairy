package com.jlss.smartDairy.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.*
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun WelcomeScreen(navigateToLoginScreen: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Smart Dairy Management",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            )
        },
        bottomBar = {

            // Footer Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Powered by JLSS",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Jeet Laxman Sitaram Solanki",
                    fontSize = 12.sp,
                    color = Color.Gray.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "The world of uniqueness",
                    fontSize = 10.sp,
                    color = Color.Gray.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        }
    ) { padding ->
        val pages = listOf("Features", "How to Use", "About Developer")
        val descriptions = listOf(
            "- Real-time calculations: Total milk, avg fat, total payment.\n" +
                    "- Voice recognition: Parse key-value speech, fuzzy name match, multi-language support.\n" +
                    "- Persistent local storage: Room DB stores everything.\n" +
                    "- Search & filter entries by date or shift.\n" +
                    "- One-tap PDF export and share.\n" +
                    "- Fully offline capable.\n" +
                    "- Secure: PIN and biometric lock + auto draft save.",
            "- Add members from the Members screen.\n" +
                    "- Go to Add tab to enter milk data.\n" +
                    "- Tap Save Entries to save.\n" +
                    "- Use All tab to browse history.\n" +
                    "- Tap a member to view their history.\n" +
                    "- Unlock app using PIN or biometrics.",
            "Developed by `JLSS` : Jeet Laxman Sitaram Solanki  with ❤️ for local dairies to make a step towards digital india."
        )

        val pagerState = rememberPagerState()

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(pages[page], style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(descriptions[page], style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Page indicator (optional)
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = navigateToLoginScreen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Get Started")
            }
        }
    }
}
