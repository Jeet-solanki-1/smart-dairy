package com.jlss.smartDairy.screen

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


@Composable
fun EntryScreen(
    vm: EntryViewModel = viewModel(),
    memberVm: MemberViewModel = viewModel(),
    onSaved: () -> Unit
) {
    val members by memberVm.members.collectAsState()
    var showFatRateDialog by remember { mutableStateOf(false) }
    var showNoMembersDialog by remember { mutableStateOf(false) }
    val rows by vm.rows.collectAsState()
    // Call only once when rows are empty
    LaunchedEffect(members) {
        // ONLY initialize when rows is still empty and members have arrived
        if (members.isNotEmpty() && rows.isEmpty()) {
            vm.initRowsFromMembers(members)
        }
    }

    LaunchedEffect(members) {
        if (members.isEmpty()) {
            showNoMembersDialog = true
        }
    }
    val totalMilk by vm.totalMilk.collectAsState()
    val avgFat by vm.avgFat.collectAsState()
    val totalAmt by vm.totalAmount.collectAsState()

    if (showNoMembersDialog) {
        AlertDialog(
            onDismissRequest = { showNoMembersDialog = false },
            title = { Text("No Members Found") },
            text = { Text("Please add members first. OtherWise the diary will not creates for the entries you are gonna add. The members section is for you to add all your milk providers , so that their daily data will store there ") },
            confirmButton = {
                TextButton(onClick = { showNoMembersDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    if (showFatRateDialog) {
        AlertDialog(
            onDismissRequest = { showFatRateDialog = false },
            title = { Text("Fat Rate Not Set") },
            text = { Text("Fat rate is not set. Amount calculation will not work until it's set in Home.") },
            confirmButton = {
                TextButton(onClick = { showFatRateDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        VoiceInputButton(vm)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f)) {
            item {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    listOf("#", "Name", "Milk", "Fat", "Amt").forEachIndexed { i, header ->
                        val weight = if (i == 0) 0.5f else 1f
                        Text(header, Modifier.weight(weight), style = MaterialTheme.typography.labelLarge)
                    }

                }
                Divider()
            }
            itemsIndexed(rows) { idx, row ->
                EntryRow(
                    index = idx,
                    row = row,
                    onRowChange = { index, changedRow ->
                        vm.updateRow(index) {
                            copy(
                                name = changedRow.name,
                                milkQty = changedRow.milkQty,
                                fatRate = changedRow.fatRate
                            )
                        }
                    }


                )
                Divider()
            }
        }



        Spacer(Modifier.height(8.dp))

        Button(onClick = vm::addEmptyRow) { Text("Add More") }

        Spacer(Modifier.height(16.dp))

        Text("Total Milk: $totalMilk", style = MaterialTheme.typography.bodyMedium)
        Text("Avg Fat: ${"%.3f".format(avgFat)}", style = MaterialTheme.typography.bodyMedium)
        Text("Total Pay: ${"%.3f".format(totalAmt)}", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            vm.saveAll()
            onSaved()
        }, Modifier.fillMaxWidth()) {
            Text("Save Entries")
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
        // Serial #
        Text("${row.serialNo}", Modifier.weight(0.25f), style = MaterialTheme.typography.bodySmall)

        // Name
        // Name — increased width
        OutlinedTextField(
            value = row.name,
            onValueChange = { newName ->
                onRowChange(index, row.copy(name = newName))
            },
            modifier = Modifier.weight(1.8f),
            singleLine = true
        )


        // Milk Qty
        OutlinedTextField(
            value = row.milkQty,
            onValueChange = { newQty ->
                onRowChange(index, row.copy(milkQty = newQty))
            },
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        // Fat Rate
        OutlinedTextField(
            value = row.fatRate,
            onValueChange = { newFat ->
                onRowChange(index, row.copy(fatRate = newFat))
            },
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        Text("%.3f".format(row.amount), Modifier.weight(1.1f)) // In 5th column
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
