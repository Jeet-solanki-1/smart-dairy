package com.jlss.smartDairy.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jlss.smartDairy.viewmodel.HomeViewModel
import com.jlss.smartDairy.viewmodel.UserViewModel
import java.io.File

/**
 * 📄 HomeScreen.kt — Smart Dairy App
 * ----------------------------------------------------------
 * 🧠 Purpose:
 * This screen acts as the **Dashboard** of the Smart Dairy app.
 * It enables you to:
 * 1. View and update the current milk fat rate.
 * 2. Export app data (entries + members or only members).
 * 3. Import data using:
 *    - File Picker (user selects file via storage UI)
 *    - Auto-Scan from known locations
 *
 * 🔍 ViewModel Used:
 * - HomeViewModel:
 *   Manages business logic and data for rate, export, and import functionalities.
 *   Automatically retained across recompositions by `viewModel()`.

 * 📦 Dependencies:
 * - Jetpack Compose Material3
 * - ActivityResultContracts for file operations
 * - Android permissions
 * - LocalContext for Toasts and access to application resources

 * 📚 Concepts Covered:
 * ----------------------------------------------------------
 * ▸ remember / mutableStateOf → To hold UI state
 * ▸ collectAsState() → Convert Flow from ViewModel to Composable state
 * ▸ AlertDialog → UI popup for export file naming
 * ▸ ActivityResultContracts → Manage permission and file picking
 * ▸ Dynamic file list loading & selection using ExposedDropdownMenuBox
 * ▸ Toasts → For user feedback

 * 🧩 UI Layout:
 * - Column (Vertical scrolling area)
 * - Multiple `Card`s used to visually segment features
 * - Intuitive icons and spacing to improve UX

 * 🔁 State Variables:
 * - input: Text input for rate
 * - showExportDialog: Boolean to control dialog visibility
 * - exportFileName: Name for export file
 * - selectedFile: Currently picked file from device storage
 * - fileOptions: List of discovered `.txt` files on the device

 * 🛡️ Permissions:
 * - For API < 33 (TIRAMISU), asks `READ_EXTERNAL_STORAGE`
 * - Uses launcher to request and handle result

 * 🛠️ Composables & Contracts Used:
 * - rememberLauncherForActivityResult → File picker and permission requests
 * - AlertDialog → Custom dialog for entering export file name
 * - ExposedDropdownMenuBox → Modern dropdown UI
 * - Material3 Components → Text, Buttons, Cards, etc.

 * 🧪 Test Cases (Conceptual):
 * - Enter a valid rate → Button updates ViewModel and shows toast
 * - Pick file using picker → ViewModel imports from URI
 * - Scan files → Device files populate dropdown
 * - Select file and click import → JSON parsed and applied

 * 🔄 Data Flow Summary:
 * ┌──────────────┐        ┌─────────────┐        ┌──────────────┐
 * │ UI Actions   ├──────▶ │ ViewModel   ├──────▶ │ Room + Files │
 * └──────────────┘        └─────────────┘        └──────────────┘
 *      ▲                       ▼                        ▲
 *      └──────── collectAsState() ◀──── observe State ──┘

 * 🧠 Why This Structure?
 * - Uses modern Compose best practices
 * - Highly modular — easy to replace individual cards with composables later
 * - Clean architecture separation between UI, ViewModel, and data access

 * 🧠 Common Confusions Addressed:
 * 1. ❓ Why is `remember` used?
 *    🔹 It keeps the variable alive across recompositions. Needed for `input`, `selectedFile`, etc.
 *
 * 2. ❓ How does the ViewModel get initialized?
 *    🔹 Using `viewModel()` from `androidx.lifecycle.viewmodel.compose`, which automatically scopes it to the NavGraph/Activity.
 *
 * 3. ❓ Why is `.collectAsState()` needed for `rate`?
 *    🔹 Because `rate` is a `StateFlow` in the ViewModel. We must convert it to Compose state to observe.

 * 4. ❓ Why use `AlertDialog` instead of another screen?
 *    🔹 Because we just need a temporary popup with an input — not a full screen.

 * 5. ❓ Can I export as JSON instead of TXT?
 *    🔹 Yes, just change the extension and write logic in `exportAllData()` and `exportOnlyMembers()` in the ViewModel.

 * 🚀 What Else Can I Do Here?
 * ----------------------------------------------------------
 * ▸ 🔁 Auto-backup: Schedule daily auto-backup using WorkManager
 * ▸ ☁️ Cloud sync: Allow upload/download to Google Drive using SAF or Drive API
 * ▸ 🗂 Categorize entries: Export separate files for morning/evening shifts
 * ▸ 🔍 File preview: Show preview of file content before importing
 * ▸ 🧠 AI-enhanced import: Validate data using AI OCR or intelligent parsing

 * 💡 Pro Tip for Future:
 * - Create a separate `ExportImportSection()` composable to keep code more modular.
 * - Use `LazyColumn` if cards become scrollable later.
 * - Replace static toasts with `SnackbarHost` scoped to a `Scaffold` for better UX.

 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: HomeViewModel = viewModel(), userVm: UserViewModel = viewModel()) {
    val context = LocalContext.current
    val rate by vm.rate.collectAsState()
    val user by userVm.user.collectAsState(initial = null)
    val userName = user?.name
    var input by remember { mutableStateOf(rate?.toString() ?: "") }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportFileName by remember { mutableStateOf("") }

    val fileOptions = remember { mutableStateListOf<File>() }
    var selectedFile by remember { mutableStateOf<File?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fileOptions.clear()
            fileOptions.addAll(vm.getAvailableJsonFilesFromPublicFolders(context))
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    val pickTextLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { vm.importEntriesFromUri(context, it) } }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                Text("📋$userName Dairy ", style = MaterialTheme.typography.headlineMedium)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("💰 Current Rate", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "${rate ?: "--"} ₹/fat",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            label = { Text("Update Rate (₹/fat)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        )

                        Button(
                            onClick = {
                                input.toDoubleOrNull()?.let {
                                    vm.setRate(it)
                                    Toast.makeText(
                                        context,
                                        "Rate updated to ₹$it/fat",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 8.dp)
                        ) {
                            Text("Save Rate")
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("📤 Export Data", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Create a backup of all your entries and members.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )

                        Button(
                            onClick = { showExportDialog = true },
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Export")
                        }
                    }
                }

                if (showExportDialog) {
                    AlertDialog(
                        onDismissRequest = { showExportDialog = false },
                        confirmButton = {
                            Column {
                                Button(onClick = {
                                    if (exportFileName.isNotBlank()) {
                                        vm.exportAllData(exportFileName)
                                        Toast.makeText(
                                            context,
                                            "Exported to $exportFileName.txt",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        showExportDialog = false
                                    }
                                }) { Text("Export All Data") }

                                Spacer(Modifier.height(8.dp))

                                Button(onClick = {
                                    if (exportFileName.isNotBlank()) {
                                        vm.exportOnlyMembers(exportFileName)
                                        Toast.makeText(
                                            context,
                                            "Exported only members",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        showExportDialog = false
                                    }
                                }) { Text("Export Only Members") }

                                TextButton(onClick = { showExportDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        },
                        title = { Text("Export as .txt file") },
                        text = {
                            OutlinedTextField(
                                value = exportFileName,
                                onValueChange = { exportFileName = it },
                                label = { Text("Enter File Name") },
                                singleLine = true
                            )
                        }
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "📥 Import from File Picker",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "Load previous entries from exported .txt files.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Button(
                            onClick = { pickTextLauncher.launch(arrayOf("text/plain")) },
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Pick TXT File")
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("📂 Import from Device", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Auto-scan and select saved .txt files from your device.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                } else {
                                    fileOptions.clear()
                                    fileOptions.addAll(
                                        vm.getAvailableJsonFilesFromPublicFolders(
                                            context
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Text("Scan for Files")
                        }

                        if (fileOptions.isNotEmpty()) {
                            var expanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                            ) {
                                OutlinedTextField(
                                    readOnly = true,
                                    value = selectedFile?.name ?: "Choose file to import",
                                    onValueChange = {},
                                    label = { Text("Saved Files") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                                    },
                                    modifier = Modifier.menuAnchor()
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    fileOptions.forEach { file ->
                                        DropdownMenuItem(
                                            text = { Text(file.name) },
                                            onClick = {
                                                selectedFile = file
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            selectedFile?.let { file ->
                                Button(
                                    onClick = {
                                        vm.importSmartJson(file) {
                                            Toast.makeText(
                                                context,
                                                "File is empty or invalid",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        Toast.makeText(
                                            context,
                                            "Imported from ${file.name}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text("Import This File")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}