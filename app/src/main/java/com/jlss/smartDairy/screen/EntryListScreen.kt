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

import com.jlss.smartDairy.viewmodel.FilterType

import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryListScreen(
    navController: NavController,
    sharedVm: SharedViewModel,
    vm: EntryListViewModel = viewModel()
) {
    val entries by vm.filteredEntries.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }

    // date formatter
    val displayFmt = remember { SimpleDateFormat("d MMM yyyy | h:mm a", Locale.getDefault()) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(
                value = vm.searchQuery.collectAsState().value,
                onValueChange = vm::setSearch,
                placeholder = { Text("Search by date…") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showFilterDialog = true }) {
                Icon(Icons.Default.List, contentDescription = "Filter")
            }
        }

        Spacer(Modifier.height(8.dp))

        if (entries.isEmpty()) {
            Text("No saved entry-lists match your criteria.", style = MaterialTheme.typography.bodyMedium)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(entries) { item ->
                val color = if (item.isNight)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = color)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val label = displayFmt.format(Date(item.timestamp))
                        Text(
                            "$label | ${if (item.isNight) "Night" else "Morning"}",
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )

                        IconButton(onClick = {
                            vm.deleteEntry(item)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Entry")
                        }
                    }

                    Button(
                        onClick = {
                            sharedVm.selectedEntries = item.listOfEntry
                            navController.navigate(Screen.EntryViewScreen.route)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Open")
                    }
                }
            }
        }

    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("Filter by Shift") },
            text = {
                Column {
                    FilterType.values().forEach { ft ->
                        Row(Modifier.fillMaxWidth().padding(4.dp)) {
                            RadioButton(
                                selected = vm.filterType.collectAsState().value == ft,
                                onClick = { vm.setFilter(ft) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(ft.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }
        )
    }
}
