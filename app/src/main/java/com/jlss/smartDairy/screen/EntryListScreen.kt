package com.jlss.smartDairy.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jlss.smartDairy.navigation.Screen
import com.jlss.smartDairy.viewmodel.EntryListViewModel
import com.jlss.smartDairy.viewmodel.SharedViewModel
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
/**
 * 📄 EntryListScreen.kt — Smart Dairy App
 * ----------------------------------------------------------
 * 🧠 Purpose:
 * Displays a scrollable list of all saved entry sessions, grouped by timestamp and shift (Morning/Night).
 * Provides:
 * 1. 🔍 Search functionality
 * 2. 🎚 Filter by shift type (All / Morning / Night)
 * 3. 🧾 View entries of a session
 * 4. ❌ Delete individual session (with confirmation dialog)

 * 🧩 ViewModels Used:
 * - EntryListViewModel:
 *   ◦ Exposes `filteredEntries`, `searchQuery`, and `filterType`
 *   ◦ Provides methods: `setSearch()`, `setFilter()`, and `deleteEntry()`
 * - SharedViewModel:
 *   ◦ Stores `selectedEntries` for viewing in `EntryViewScreen`

 * 📚 Concepts Covered:
 * ----------------------------------------------------------
 * ▸ State Hoisting (search/filter/showDialog)
 * ▸ collectAsState() for observing Flow from ViewModel
 * ▸ LazyColumn for list rendering
 * ▸ Card + Button + IconButton for interactivity
 * ▸ AlertDialog for filtering and confirming deletion

 * 🖼️ UI Layout:
 * - TextField + Filter Icon → Header row
 * - LazyColumn of Cards → Each represents a saved session
 * - Inside each Card: Label with date/time + shift, Delete icon, Open button

 * 🧠 Important Local Variables:
 * - `entries` = session list from ViewModel based on filter/search
 * - `showFilterDialog` = toggles the shift filter popup
 * - `showDialog` = toggles delete confirmation for current item
 * - `displayFmt` = formatter for displaying timestamp in human-friendly format

 * 🔍 Search Explanation:
 * - Binds TextField to `vm.searchQuery` and uses `setSearch()` to update ViewModel
 * - Internally ViewModel filters `allEntries` to produce `filteredEntries`

 * 🎚 Filter Explanation:
 * - Tapping filter icon opens an AlertDialog listing `FilterType` enum values
 * - Radio buttons change ViewModel's filter state

 * 📤 Open Entry Flow:
 * - Pressing "Open" sets the selected item’s list to `sharedVm.selectedEntries`
 * - Navigates to `EntryViewScreen`

 * ❌ Deletion Flow:
 * - Clicking Delete icon shows `ConfirmDeleteDialog`
 * - If confirmed → `vm.deleteEntry(item)` is called
 *
 * 📦 Dependencies:
 * - `ConfirmDeleteDialog`: Custom composable for delete confirmation
 * - `FilterType`: Enum representing shift filters

 * 🧪 Test Cases (Conceptual):
 * ----------------------------------------------------------
 * ▸ Searching a partial date (e.g. “24 Jun”) shows only matches
 * ▸ Deleting an entry removes it and updates UI
 * ▸ Filter "Morning" only shows morning entries
 * ▸ Tapping "Open" passes correct entry to `SharedViewModel`

 * 🔄 Data Flow Summary:
 * ┌──────────────┐        ┌───────────────┐        ┌──────────────┐
 * │ User Inputs  ├──────▶ │ EntryListVM   ├──────▶ │ Room Entries │
 * └──────────────┘        └───────────────┘        └──────────────┘
 *       ▲                        ▼                          ▲
 *       └──── ViewModel Flows ◀── UI renders ◀─────────────┘

 * ⚠️ Key Notes:
 * ----------------------------------------------------------
 * ▸ `showDialog` is not entry-specific, so delete confirmation could misbehave with many entries.
 *   ✅ Suggestion: Use a `deleteCandidate` state variable instead of simple `Boolean`.

 * ▸ `SimpleDateFormat` in `remember {}` ensures performance and avoids recreating every recomposition.

 * ▸ `FilterType.values()` is a smart usage of enums for future scalability (e.g., add “Evening” filter later).

 * 🚀 Future Enhancements:
 * ----------------------------------------------------------
 * ▸ ✅ Fix dialog bug with non-specific `showDialog` (see above)
 * ▸ 📆 Add date picker for specific day selection
 * ▸ 🔄 Use Swipe-to-Delete pattern with LazyColumn
 * ▸ 📊 Show milk/fat summary in each card
 * ▸ ☁️ Add backup/upload options for each session

 * 💡 Pro Tip:
 * - You can extract filter and list rendering into their own composables like:
 *   `@Composable fun EntryFilterRow(...)` and `@Composable fun EntryCard(...)`
 *   for cleaner file and better modularization.

 */

//@Composable
//fun EntryListScreen(
//    vm: EntryListViewModel = viewModel(),
//    sharedVm: SharedViewModel,
//    navController: NavController
//) {
//    val entries by vm.allEntries.collectAsState()
//
//    // Pre-create your formatter once
//    val formatter = remember {
//        SimpleDateFormat("d MMM yyyy | h:mm a", Locale.getDefault())
//    }
//
//    Column(Modifier.fillMaxSize().padding(16.dp)) {
//        Text("All Entries", style = MaterialTheme.typography.headlineSmall)
//        Spacer(Modifier.height(8.dp))
//
//        if (entries.isEmpty()) {
//            Text("No saved entry-lists yet.")
//        }
//
//        LazyColumn {
//            items(entries) { item ->
//                val color = if (item.isNight)
//                    MaterialTheme.colorScheme.primary
//                else
//                    MaterialTheme.colorScheme.secondary
//
//                // Format the timestamp + append night/day tag
//                val formatted = formatter.format(Date(item.timestamp)) +
//                        " | ${if (item.isNight) "Night" else "Morning"}"
//
//                Button(
//                    onClick = {
//                        sharedVm.selectedEntries = item.listOfEntry
//                        navController.navigate(Screen.EntryViewScreen.route)
//                    },
//                    colors = ButtonDefaults.buttonColors(containerColor = color),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp)
//                ) {
//                    Text(formatted)
//                }
//            }
//        }
//    }
//}

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.jlss.smartDairy.component.ConfirmDeleteDialog

import com.jlss.smartDairy.viewmodel.FilterType

import java.util.*



import androidx.compose.material.icons.filled.FilterList
import com.jlss.smartDairy.data.model.ListOfEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryListScreen(
    navController: NavController,
    sharedVm: SharedViewModel,
    vm: EntryListViewModel = viewModel()
) {
    val entries by vm.filteredEntries.collectAsState()
    val searchQuery by vm.searchQuery.collectAsState()
    val selectedFilter by vm.filterType.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<ListOfEntry?>(null) }

    val formatter = remember {
        SimpleDateFormat("EEE, d MMM yyyy | h:mm a", Locale.getDefault())
    }

    Column(Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // 🔍 Search and Filter Row
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = vm::setSearch,
                label = { Text("Search by Date") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showFilterDialog = true }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter by Shift")
            }
        }

        Spacer(Modifier.height(12.dp))

        if (entries.isEmpty()) {
            Text(
                "No entries found for the current filter.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(entries) { entry ->
                    EntryCard(
                        entryGroup = entry,
                        formatter = formatter,
                        onOpen = {
                            sharedVm.selectedEntries = entry.listOfEntry
                            navController.navigate(Screen.EntryViewScreen.route)
                        },
                        onDelete = { deleteCandidate = entry }
                    )
                }
            }
        }
    }

    // 🧾 Filter Dialog
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Close")
                }
            },
            title = { Text("Filter Entries") },
            text = {
                Column {
                    FilterType.values().forEach { filter ->
                        Row(Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)) {
                            RadioButton(
                                selected = selectedFilter == filter,
                                onClick = { vm.setFilter(filter) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(filter.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }
        )
    }

    // ❌ Delete Dialog
    deleteCandidate?.let { entryToDelete ->
        ConfirmDeleteDialog(
            message = "Delete this entry session from ${formatter.format(Date(entryToDelete.timestamp))}?",
            onConfirm = {
                vm.deleteEntry(entryToDelete)
                deleteCandidate = null
            },
            onDismiss = { deleteCandidate = null }
        )
    }
}

@Composable
private fun EntryCard(
    entryGroup: ListOfEntry,
    formatter: SimpleDateFormat,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val shiftText = if (entryGroup.isNight) "Night" else "Morning"
    val label = formatter.format(Date(entryGroup.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (entryGroup.isNight)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "$label | $shiftText",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Entry List")
            }
        }
    }
}
