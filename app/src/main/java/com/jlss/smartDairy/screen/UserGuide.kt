package com.jlss.smartDairy.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Smart Dairy",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Smart Dairy is an Android application designed to streamline daily milk collection and payment calculations for dairy farmers and cooperative societies. With an intuitive table-based data entry system, voice-assisted input, persistent storage, and secure access, Smart Dairy makes recording, managing, and reviewing milk entries simple and efficient.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )

            Text("Description", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "- Record daily milk entries: Pre-populate rows for each registered member, then fill in fat percentage and milk quantity. Calculations for amount to pay are automatic based on a configurable fat rate.\n" +
                        "- Voice-assisted data entry: Use natural speech like 'name Jeet solanki fat 5.6 milk 8' to fill fields. Supports English and Hindi.\n" +
                        "- Historical records: Save each day's entries as timestamped reports. Browse and filter by date or shift.\n" +
                        "- PDF reporting: Generate and share PDFs of entries with totals and headers.\n" +
                        "- Member management: Add/edit/delete members. View individual histories.\n" +
                        "- Security: PIN or biometric unlock.\n" +
                        "- Auto-save drafts: Prevent data loss on accidental closure.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text("Features", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "- Real-time calculations: Total milk, avg fat, total payment.\n" +
                        "- Voice recognition: Parse key-value speech, fuzzy name match, multi-language support.\n" +
                        "- Persistent local storage: Room DB stores everything.\n" +
                        "- Search & filter entries by date or shift.\n" +
                        "- One-tap PDF export and share.\n" +
                        "- Fully offline capable.\n" +
                        "- Secure: PIN and biometric lock + auto draft save.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )


            Text("Usage", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "- Add members from the Members screen.\n" +
                        "- Go to Add tab to enter milk data.\n" +
                        "- Tap Save Entries to save.\n" +
                        "- Use All tab to browse history.\n" +
                        "- Tap a member to view their history.\n" +
                        "- Unlock app using PIN or biometrics.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Powered by JLSS",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
