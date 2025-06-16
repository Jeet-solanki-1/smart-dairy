package com.jlss.smartDairy.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.jlss.smartDairy.data.AppDatabase
import com.jlss.smartDairy.data.model.Entry
import com.jlss.smartDairy.data.model.ListOfEntry

import com.jlss.smartDairy.viewmodel.EntryViewModel
import com.jlss.smartDairy.viewmodel.UserViewModel


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.jlss.smartDairy.data.dao.FatRateDao
import com.jlss.smartDairy.data.model.Rates
import com.jlss.smartDairy.navigation.Screen
import com.jlss.smartDairy.viewmodel.ProductionReport
import kotlinx.coroutines.launch

@Composable
fun ProductionEntryScreen(
    entryId: Long,
    navController: NavHostController,
    vm: EntryViewModel = viewModel(),
    userVm: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val rateDao = db.fatRateDao()

    val user by userVm.user.collectAsState(initial = null)
    val rates by rateDao.get().collectAsState(initial = null)

    val entryGroup = remember { mutableStateOf<ListOfEntry?>(null) }
    val report = remember { mutableStateOf<ProductionReport?>(null) }

    var showFatRateDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // only show _after_ a non-null Rates arrives
    LaunchedEffect(rates) {
        rates?.let {
            if (it.sellingFatRate == 0.0) {
                showFatRateDialog = true
            }
        }
    }

    // 3. Load EntryGroup
    LaunchedEffect(entryId) {
        isLoading = true
        entryGroup.value = vm.getById(entryId)
        isLoading = false
    }

    // 4. Generate report when ready
    LaunchedEffect(entryGroup.value, rates) {
        val group = entryGroup.value
        val currentRates = rates
        if (group?.factoryEntry != null && currentRates != null) {
            report.value = vm.generateReport(group, currentRates)
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val group = entryGroup.value
    when {
        group == null ->  // add top padding so “No entry found…” is lower on screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp),             // ← adjust this value as you like
                contentAlignment = Alignment.TopCenter  // center horizontally, at that vertical offset
            ) {
                Text("No entry found for this ID.", style = MaterialTheme.typography.bodyLarge)
            }

        group.factoryEntry == null -> {
            if (rates != null && user != null) {
                ProductionEntryForm(
                    timestamp = group.timestamp,
                    isNight = group.isNight,
                    userName = user!!.userName,
                    rates = rates!!,
                    onSave = { entry ->
                        coroutineScope.launch {
                        vm.setFactoryEntry(entryId, entry)
                        entryGroup.value = vm.getById(entryId) // ✅ safely called inside coroutine
                    }
                    }
                )
            }
        }

        report.value != null -> {
            ProductionReportScreen(
                factoryEntry = group.factoryEntry!!,
                report = report.value!!,
                onClearFactoryEntry = {
                    vm.clearFactoryEntryFromGroup(entryId)
                    entryGroup.value = null // trigger re-fetch
                }
            )
        }

        else -> {
            // “Generating report...” with similar top padding
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp),             // ← same offset as above
                contentAlignment = Alignment.TopCenter
            ) {
                Text("Generating report... Wait!!", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    if (showFatRateDialog) {
        AlertDialog(
            onDismissRequest = { showFatRateDialog = false },
            title = { Text("Missing Fat Rate") },
            text = { Text("Please set the selling fat rate and Milk selling rates to proceed before proceeding.") },
            confirmButton = {
                Button(onClick = {
                    showFatRateDialog = false
                    navController.navigate(Screen.MainScaffold.route)
                }) {
                    Text("OK")
                }
            }
        )
    }
}


@Composable
fun ProductionEntryForm(
    userName: String,
    timestamp: Long,
    isNight: Boolean,
    onSave: (Entry) -> Unit,
    rates: Rates
) {
    var milkQty by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    val sellingRate = rates.sellingFatRate
    val amount = (milkQty.toDoubleOrNull() ?: 0.0) *
            (fat.toDoubleOrNull() ?: 0.0) *
            sellingRate

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 100.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            "Your Factory / Production Data",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier   = Modifier.fillMaxWidth(),
            elevation  = CardDefaults.cardElevation(8.dp),
            shape      = RoundedCornerShape(8.dp)
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value         = milkQty,
                    onValueChange = { milkQty = it },
                    label         = { Text("Milk Quantity (L)") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value         = fat,
                    onValueChange = { fat = it },
                    label         = { Text("Fat %") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )

                Text(
                    "Estimated Amount: ₹ %.2f".format(amount),
                    style = MaterialTheme.typography.bodyLarge
                )

                Button(
                    onClick = {
                        val entry = Entry(
                            name        = userName,
                            fat         = fat.toDoubleOrNull() ?: 0.0,
                            milkQty     = milkQty.toDoubleOrNull() ?: 0.0,
                            amountToPay = amount,
                            timestamp   = timestamp,
                            isNight     = isNight
                        )
                        onSave(entry)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Factory Entry")
                }
            }
        }
    }
}


@Composable
fun ProductionReportScreen(
    factoryEntry: Entry,
    report: ProductionReport,
    onClearFactoryEntry: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 100.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            "Your Reports of This Day",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Factory Entry Details
        Card(
            shape   = RoundedCornerShape(12.dp),
            colors  = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Factory Entry", style = MaterialTheme.typography.titleMedium)
                Text("Name: ${factoryEntry.name}")
                Text("Milk Qty: %.2f L".format(factoryEntry.milkQty))
                Text("Fat: %.2f %%".format(factoryEntry.fat))
                Text("Amount: ₹ %.2f".format(factoryEntry.amountToPay))
            }
        }

        Spacer(Modifier.height(12.dp))

        // Milk Summary
        Card(
            shape     = RoundedCornerShape(12.dp),
            colors    = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Milk Summary", style = MaterialTheme.typography.titleMedium)
                Text("Total Collected: %.2f L".format(report.totalMilkCollected))
                Text("Sent to Production: %.2f L".format(report.totalMilkSent))
                Text("Sold Locally: %.2f L".format(report.localSoldMilk))
            }
        }

        Spacer(Modifier.height(12.dp))

        // Earnings Summary
        Card(
            shape     = RoundedCornerShape(12.dp),
            colors    = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Earnings Summary", style = MaterialTheme.typography.titleMedium)
                Text("Paid to Members: ₹ %.2f".format(report.totalPaidToMembers))
                Text("Factory Income: ₹ %.2f".format(report.factoryIncome))
                Text("Local Income: ₹ %.2f".format(report.localIncome))
                Text("Final Earning: ₹ %.2f".format(report.finalEarning),
                    style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick  = onClearFactoryEntry,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("🗑️ Clear Factory Entry")
        }
    }
}
