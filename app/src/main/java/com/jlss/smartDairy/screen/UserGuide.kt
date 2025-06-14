package com.jlss.smartDairy.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserGuideScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Guide") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // App Title
            Text(
                text = "📋 Smart Dairy",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Smart Dairy is a modern, offline-first Android application designed to streamline daily milk collection and payment workflows for farmers and cooperatives. With voice control, rich table entry, and robust storage, it makes managing dairy operations elegant and effortless.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(24.dp))
            SectionTitle("🔍 Description")
            SectionBody(
                "- Record daily milk entries with automatic amount calculation using fat rate.\n" +
                        "- Voice-assisted input like: 'Jeet Solanki fat 6.5 milk 10'. Supports English & Hindi.\n" +
                        "- Save entries as reports with timestamps and view history by date or shift.\n" +
                        "- Auto PDF generation and secure sharing.\n" +
                        "- Member management with individual record histories.\n" +
                        "- Security via PIN or biometric lock.\n" +
                        "- Auto draft-save prevents data loss."
            )

            Spacer(Modifier.height(16.dp))
            SectionTitle("✨ Features")
            SectionBody(
                "- Real-time milk/fat/pay calculations.\n" +
                        "- AI-enhanced fuzzy name recognition in voice input.\n" +
                        "- Fully offline with Room DB.\n" +
                        "- Entry search, filtering (morning/night).\n" +
                        "- Elegant Compose UI.\n" +
                        "- Multi-language support.\n" +
                        "- Export & import entries easily."
            )

            Spacer(Modifier.height(16.dp))
            SectionTitle("🚀 Getting Started")
            SectionBody(
                "- Add your milk providers in the Member tab first.\n" +
                        "- Use the 'Add' tab daily to fill entries.\n" +
                        "- Tap 'Save Entries' to store securely.\n" +
                        "- View past records in the 'All' tab.\n" +
                        "- Tap member name to view their full history.\n" +
                        "- Enable app lock from Settings for protection."
            )

            Spacer(Modifier.height(32.dp))
            Text(
                text = "Built with 💛 by JLSS",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

// Helper composables for consistent styling
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SectionBody(content: String) {
    Text(
        text = content,
        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
        modifier = Modifier.padding(start = 8.dp)
    )
}
