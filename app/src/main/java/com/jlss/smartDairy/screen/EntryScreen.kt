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
import androidx.compose.ui.platform.LocalContext
import com.jlss.smartDairy.data.AppDatabase
import com.jlss.smartDairy.navigation.Screen


@Composable
fun EntryScreen(
    vm: EntryViewModel = viewModel(),
    memberVm: MemberViewModel = viewModel(),
    onSaved: () -> Unit,
    navController: NavController
) {
    val members by memberVm.members.collectAsState()
    val rows by vm.rows.collectAsState()
    val totalMilk by vm.totalMilk.collectAsState()
    val avgFat by vm.avgFat.collectAsState()
    val totalAmt by vm.totalAmount.collectAsState()
    var showFatRateDialog by remember { mutableStateOf(false) }
    var showSaveConfirm by remember { mutableStateOf(false) }
    var showNoMembersDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val rate = mutableStateOf(0.0)


    LaunchedEffect(Unit) {
          vm.refreshFatRate()
     if (vm.ratePerFat.value ==null){
         showFatRateDialog = true
     }

    }
    if (showFatRateDialog) {
        // Show dialog or directly navigate to HomeScreen
        AlertDialog(
            onDismissRequest = { showFatRateDialog = false },
            title = { Text("Fat Rate not Set!!") },
            text = { Text("Set fat rate first from home screen to calculate amounts.") },
            confirmButton = { TextButton(onClick = {
                navController.navigate(Screen.HomeScreen.route) // replace with actual route
            }
            ) { Text("Go") }
            })
    }

    if (showNoMembersDialog) {
        AlertDialog(
            onDismissRequest = { showNoMembersDialog = false },
            title = { Text("No Members Found") },
            text = { Text("Please add members first. The members section is needed to map entries correctly.") },
            confirmButton = { TextButton(onClick = {
                showNoMembersDialog = false

            }
            ) { Text("OK") }
            }
        )
    }
    if (showSaveConfirm) {
        AlertDialog(
            onDismissRequest = { showSaveConfirm = false },
            title = { Text("Save") },
            text = { Text("Always make sure you filled all entries of the shift to prevent data loss.") },
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


    if (showFatRateDialog) {
        AlertDialog(
            onDismissRequest = { showFatRateDialog = false },
            title = { Text("Fat Rate Not Set") },
            text = { Text("Go to home screen and set current fat rate!") },
            confirmButton = { TextButton(onClick = { navController.navigate(Screen.HomeScreen.route) }) { Text("Ok") } }
        )
    }

    Scaffold(
        bottomBar = {
            Column(Modifier.fillMaxWidth().padding(8.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Milk: $totalMilk", style = MaterialTheme.typography.bodyMedium)
                    Text("Fat: ${"%.2f".format(avgFat)}", style = MaterialTheme.typography.bodyMedium)
                    Text("Pay: ₹${"%.2f".format(totalAmt)}", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = { vm.addEmptyRow() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Row")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        showSaveConfirm = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("💾 Save Entries")
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
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
                    EntryRow(index = idx, row = row, onRowChange = { index, changedRow ->
                        vm.updateRow(index) {
                            copy(
                                name = changedRow.name,
                                milkQty = changedRow.milkQty,
                                fatRate = changedRow.fatRate
                            )
                        }
                    })
                    Divider()
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
