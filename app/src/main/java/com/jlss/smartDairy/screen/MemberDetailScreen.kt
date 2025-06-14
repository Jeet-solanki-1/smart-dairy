package com.jlss.smartDairy.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jlss.smartDairy.component.ConfirmDeleteDialog
import com.jlss.smartDairy.data.model.Entry
import com.jlss.smartDairy.viewmodel.MemberViewModel
import com.jlss.smartDairy.viewmodel.PdfViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    memberId: Long,
    navController: NavController,
    vm: MemberViewModel = viewModel(),
    viewModel: PdfViewModel = viewModel()
) {
    val member by vm.selected.collectAsState()
    LaunchedEffect(memberId) { vm.findById(memberId) }
    var showDialog by remember { mutableStateOf(false) }
    member?.let { m ->
        // 1) Convert nullable history to a non-null list
        val entries: List<Entry> = m.history

        // 2) Now sumOf will work, and items(...) accepts List<Entry>
        val totalMilk = entries.sumOf { it.milkQty }
        val avgFat     = entries.takeIf { it.isNotEmpty() }?.map { it.fat }?.average() ?: 0.0
        val totalPay   = entries.sumOf { it.amountToPay }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(m.name) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },

                    actions = {
                        // Print/share PDF button
                        IconButton(onClick = {
                            viewModel.generateReportPdf(entries)
                        }) {
                            Icon(Icons.Default.Send, contentDescription = "Print to PDF")
                        }
                        IconButton(onClick = { showDialog=true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear History")
                        }
                    }
                )
            }

        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                LazyColumn(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // pass the non-null list
                    items(entries) { e ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    SimpleDateFormat(
                                        "dd MMM yyyy | h:mm a",
                                        Locale.getDefault()
                                    ).format(Date(e.timestamp))
                                )
                                Spacer(Modifier.height(4.dp))
                                Divider()
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Fat: %.2f".format(e.fat))
                                    Text("Qty: %.2f".format(e.milkQty))
                                    Text("Amt: %.2f".format(e.amountToPay))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Total Milk: %.2f".format(totalMilk))
                Text("Avg Fat:   %.2f".format(avgFat))
                Text("Total Pay: %.2f".format(totalPay))
                // dialog to delet confirm
                if (showDialog) {
                    ConfirmDeleteDialog(
                        message = "Do you really want to delete this entry?",
                        onConfirm = {
                           vm.clearHistory(memberId)// your delete logic
                            showDialog = false
                        },
                        onDismiss = { showDialog = false }
                    )
                }
            }
        }

    } ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
