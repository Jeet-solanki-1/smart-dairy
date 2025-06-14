package com.jlss.smartDairy.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jlss.smartDairy.component.ConfirmDeleteDialog
import com.jlss.smartDairy.navigation.Screen
import com.jlss.smartDairy.viewmodel.MemberViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MemberScreen(
    navController: NavController,
    vm: MemberViewModel = viewModel()
) {
    val list by vm.members.collectAsState()
    var name by remember { mutableStateOf("") }
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var showDialog by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Add Member") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { vm.add(name); name = "" },
            modifier = Modifier.align(Alignment.End)
        ) { Text("Add") }

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list) { m ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate(Screen.MemberDetail.createRoute(m.id))
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(m.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Joined ${dateFmt.format(Date(m.dateOfJoin))}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { showDialog=true }) {

                                Icon(Icons.Default.Delete, contentDescription = "remove")

                        }
                    }
                }
                if (showDialog) {
                    ConfirmDeleteDialog(
                        message = "Do you really want to delete this entry?",
                        onConfirm = {
                            vm.remove(m)// your delete logic
                            showDialog = false
                        },
                        onDismiss = { showDialog = false }
                    )
                }
            }


        }

    }
}
