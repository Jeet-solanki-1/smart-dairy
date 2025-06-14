package  com.jlss.smartDairy.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jlss.smartDairy.component.ConfirmDeleteDialog
import com.jlss.smartDairy.data.model.Members
import com.jlss.smartDairy.navigation.Screen
import com.jlss.smartDairy.viewmodel.MemberViewModel
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberScreen(
    navController: NavController,
    vm: MemberViewModel = viewModel()
) {
    val members by vm.members.collectAsState()
    var name by remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }
    val memberToDelete = remember { mutableStateOf<Members?>(null) }
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Members") },
                actions = {
                    Icon(Icons.Default.People, contentDescription = null)
                }
            )
        }

    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Add New Member", style = MaterialTheme.typography.titleMedium)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Member Name") },
                    placeholder = { Text("e.g., Ramesh") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                IconButton(onClick = {
                    if (name.trim().isNotEmpty()) {
                        vm.add(name.trim())
                        name = ""
                    }
                }) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Member")
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Your Members", style = MaterialTheme.typography.titleMedium)
            if (members.isEmpty()) {
                Text("No members yet. Start by adding someone.", style = MaterialTheme.typography.bodyMedium)
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(members) { m ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Screen.MemberDetail.createRoute(m.id))
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(m.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Joined on ${dateFmt.format(Date(m.dateOfJoin))}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            IconButton(onClick = {
                                memberToDelete.value = m
                                showDialog.value = true
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Member")
                            }
                        }
                    }
                }
            }

            memberToDelete.value?.let { memberId ->
                if (showDialog.value) {
                    ConfirmDeleteDialog(
                        message = "Do you really want to delete this member?",
                        onConfirm = {
                            vm.remove(memberId)
                            showDialog.value = false
                            memberToDelete.value = null
                        },
                        onDismiss = {
                            showDialog.value = false
                            memberToDelete.value = null
                        }
                    )
                }
            }
        }
    }
}
