package com.jlss.smartDairy.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jlss.smartDairy.data.model.Entry
import com.jlss.smartDairy.viewmodel.EntryRowState
import com.jlss.smartDairy.viewmodel.EntryViewModel

import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.jlss.smartDairy.viewmodel.MemberViewModel
import com.jlss.smartDairy.viewmodel.PdfViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton



import androidx.compose.material.icons.filled.Add

import androidx.compose.material.icons.filled.Mic
import androidx.compose.ui.Alignment
import com.jlss.smartDairy.navigation.Screen

import com.jlss.smartDairy.viewmodel.UserViewModel


import com.jlss.smartDairy.viewmodel.*



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(
    vm: EntryViewModel = viewModel(),
    memberVm: MemberViewModel = viewModel(),
    userVm: UserViewModel = viewModel(),
    onSaved: () -> Unit,
    navController: NavController
) {
    // ViewModel state
    val members by memberVm.members.collectAsState()
    val user by userVm.user.collectAsState(initial = null)
    val rows by vm.rows.collectAsState()
    val totalMilk by vm.totalMilk.collectAsState()
    val avgFat by vm.avgFat.collectAsState()
    val totalAmt by vm.totalAmount.collectAsState()
    val wasInitByMembers by vm.wasInitializedByMembers.collectAsState()
    val showMissingRate by vm.showMissingFatRate.collectAsState()

    // UI flags
    var showNoMembersDialog by remember { mutableStateOf(false) }
    var showSaveConfirm by remember { mutableStateOf(false) }

    // Logic
    val hasLoadedMembers = rows.isNotEmpty()
    val shouldShowLoadButton = !wasInitByMembers

    // ---- Dialogs ----
    if (showNoMembersDialog) {
        AlertDialog(
            onDismissRequest = { showNoMembersDialog = false },
            title = { Text("No Members Found") },
            text = { Text("Please add members first.") },
            confirmButton = {
                TextButton(onClick = { showNoMembersDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showSaveConfirm) {
        AlertDialog(
            onDismissRequest = { showSaveConfirm = false },
            title = { Text("Save Entries") },
            text = { Text("Make sure all rows are properly filled before saving.") },
            confirmButton = {
                TextButton(onClick = {
                    showSaveConfirm = false
                    vm.saveAll()
                    onSaved()
                }) {
                    Text("Yes")
                }
            }
        )
    }

    if (showMissingRate) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Missing Fat Rate") },
            text = { Text("Please set the fat rate before proceeding.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.setInitializedByMembersFalse()
                    navController.navigate(Screen.MainScaffold.route)
                }) {
                    Text("OK")
                }
            }
        )
    }

    // ---- UI Scaffold ----
    Scaffold(
        bottomBar = {
            if (hasLoadedMembers) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Milk: $totalMilk", style = MaterialTheme.typography.bodyMedium)
                        Text("Fat: ${"%.2f".format(avgFat)}", style = MaterialTheme.typography.bodyMedium)
                        Text("Pay: ₹${"%.2f".format(totalAmt)}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(4.dp))
                    IconButton(
                        onClick = { vm.addEmptyRow() },
                        Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Row")
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { showSaveConfirm = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("💾 Save Entries")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (shouldShowLoadButton) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(onClick = {
                        if (members.isEmpty()) {
                            showNoMembersDialog = true
                        } else {
                            vm.initRowsFromMembers(members)
                        }
                    }) {
                        Text("📥 Load Members")
                    }
                }
            } else {
                LazyColumn(Modifier.weight(1f)) {
                    item {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            listOf("#", "Name", "Milk", "Fat", "Amt").forEachIndexed { i, header ->
                                Text(
                                    header,
                                    Modifier.weight(if (i == 0) 0.5f else 1f),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                        Divider()
                    }
                    itemsIndexed(rows) { idx, row ->
                        EntryRow(idx, row) { index, changed ->
                            vm.updateRow(index) {
                                copy(
                                    name = changed.name,
                                    milkQty = changed.milkQty,
                                    fatRate = changed.fatRate
                                )
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun EntryRow(
    index: Int,
    row: EntryRowState,
    onRowChange: (Int, EntryRowState) -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text("${row.serialNo}", Modifier.weight(0.25f), style = MaterialTheme.typography.bodySmall)

        OutlinedTextField(
            value       = row.name,
            onValueChange = { onRowChange(index, row.copy(name = it)) },
            Modifier.weight(1.8f),
            singleLine = true
        )

        OutlinedTextField(
            value       = row.milkQty,
            onValueChange = { onRowChange(index, row.copy(milkQty = it)) },
            Modifier.weight(1f),
            singleLine   = true
        )

        OutlinedTextField(
            value       = row.fatRate,
            onValueChange = { onRowChange(index, row.copy(fatRate = it)) },
            Modifier.weight(1f),
            singleLine   = true
        )

        Text("%.2f".format(row.amount), Modifier.weight(1.1f))
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryViewScreen(
    entryList: List<Entry>,
    navController: NavController,
    viewModel: PdfViewModel = viewModel()
) {
    // Calculate summaries
    val totalMilk = remember(entryList) { entryList.sumOf { it.milkQty } }
    val avgFat = remember(entryList) {
        entryList.map { it.fat }
            .let { list -> if (list.isEmpty()) 0.0 else list.average() }
    }
    val totalPay = remember(entryList) { entryList.sumOf { it.amountToPay } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("View Entries") },
                actions = {
                    IconButton(onClick = {
                        viewModel.generateReportPdf(entryList)   // Kick off PDF generation + share
                    }) {
                        Icon(Icons.Default.Send, contentDescription = "Print to PDF")
                    }
                }
            )
        }
    ) {  paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(entryList) { entry ->
                    Card(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .fillMaxWidth()
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Text("Name: ${entry.name}")
                            Text("Milk Qty: ${"%.3f".format(entry.milkQty)}")
                            Text("Fat: ${"%.3f".format(entry.fat)}")
                            Text("Amount: ${"%.3f".format(entry.amountToPay)}")
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Total Milk: $totalMilk", style = MaterialTheme.typography.bodyMedium)
            Text("Avg Fat: ${"%.3f".format(avgFat)}", style = MaterialTheme.typography.bodyMedium)
            Text("Total Pay: ${"%.3f".format(totalPay)}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
