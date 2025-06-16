package com.jlss.smartDairy.screen


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jlss.smartDairy.viewmodel.HomeViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    var exportFileName by remember { mutableStateOf("") }
    val fileOptions = remember { mutableStateListOf<File>() }
    var selectedFile by remember { mutableStateOf<File?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fileOptions.clear()
            fileOptions.addAll(vm.getAvailableJsonFilesFromPublicFolders(context))
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    val pickTextLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { vm.importEntriesFromUri(context, it) } }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("⚙️ Settings", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            ExportCard(
                showExportDialog = showExportDialog,
                exportFileName = exportFileName,
                onExportFileNameChange = { exportFileName = it },
                onConfirmExport = {
                    vm.exportAllData(exportFileName)
                    showExportDialog = false
                },
                onExportOnlyMembers = {
                    vm.exportOnlyMembers(exportFileName)
                    showExportDialog = false
                },
                onDismiss = { showExportDialog = false },
                onOpenDialog = { showExportDialog = true }
            )
        }

        item {
            ImportFilePickerCard(pickTextLauncher)
        }

        item {
            ImportFromDeviceCard(
                context = context,
                vm = vm,
                fileOptions = fileOptions,
                selectedFile = selectedFile,
                onSelectFile = { selectedFile = it },
                onClickImport = {
                    vm.importSmartJson(selectedFile!!) {
                        Toast.makeText(context, "Invalid file!", Toast.LENGTH_SHORT).show()
                    }
                },
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE) }
            )
        }
    }
}


@Composable
fun ExportCard(
    showExportDialog: Boolean,
    exportFileName: String,
    onExportFileNameChange: (String) -> Unit,
    onConfirmExport: () -> Unit,
    onExportOnlyMembers: () -> Unit,
    onDismiss: () -> Unit,
    onOpenDialog: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📤 Export Data", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onOpenDialog) {
                Icon(Icons.Default.Save, contentDescription = "Export")
                Spacer(Modifier.width(8.dp))
                Text("Export to JSON File")
            }
        }

        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = onDismiss,
                confirmButton = {
                    TextButton(onClick = onConfirmExport) { Text("Export All Entries") }
                },
                dismissButton = {
                    Column {
                        TextButton(onClick = onExportOnlyMembers) { Text("Only Members") }
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                    }
                },
                title = { Text("File Name") },
                text = {
                    OutlinedTextField(
                        value = exportFileName,
                        onValueChange = onExportFileNameChange,
                        label = { Text("Enter filename") },
                        singleLine = true
                    )
                }
            )
        }
    }
}

@Composable
fun ImportFilePickerCard(
    pickTextLauncher: ManagedActivityResultLauncher<Array<String>, Uri?>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📥 Import via File Picker", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                pickTextLauncher.launch(arrayOf("application/json", "text/plain"))
            }) {
                Icon(Icons.Default.UploadFile, contentDescription = "Import")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import from File Picker")
            }
        }
    }
}


@Composable
fun ImportFromDeviceCard(
    context: Context,
    vm: HomeViewModel,
    fileOptions: List<File>,
    selectedFile: File?,
    onSelectFile: (File) -> Unit,
    onClickImport: () -> Unit,
    onRequestPermission: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📂 Import from Device Storage", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(8.dp))

            Button(onClick = onRequestPermission) {
                Icon(Icons.Default.Folder, contentDescription = "Browse Files")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Browse Device Files")
            }

            Spacer(Modifier.height(8.dp))

            if (fileOptions.isNotEmpty()) {
                DropdownMenuBox(
                    fileOptions = fileOptions,
                    selectedFile = selectedFile,
                    onSelectFile = onSelectFile
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onClickImport,
                    enabled = selectedFile != null
                ) {
                    Text("📥 Import Selected File")
                }
            }
        }
    }
}

@Composable
private fun DropdownMenuBox(
    fileOptions: List<File>,
    selectedFile: File?,
    onSelectFile: (File) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedFile?.name ?: "Select a file")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            fileOptions.forEach { file ->
                DropdownMenuItem(
                    text = { Text(file.name) },
                    onClick = {
                        expanded = false
                        onSelectFile(file)
                    }
                )
            }
        }
    }
}
