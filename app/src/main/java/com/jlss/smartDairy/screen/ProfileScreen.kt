package com.jlss.smartDairy.screen


import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jlss.smartDairy.navigation.AppLockViewModelFactory
import com.jlss.smartDairy.viewmodel.AppLockViewModel
import com.jlss.smartDairy.viewmodel.UserViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    vm: UserViewModel = viewModel()
) {
    val factory = AppLockViewModelFactory(LocalContext.current.applicationContext as Application)
    val pinVm: AppLockViewModel = viewModel(factory = factory)
    val user by vm.user.collectAsState(initial = null)
    val pin by pinVm.pin.collectAsState(initial = null)
    var yourName by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var pinVar by remember {
        mutableStateOf("")
    }

    LaunchedEffect(user) {
        user?.let {
            yourName = it.userName
            name = it.name
            mobile = it.mobile
            village = it.village

        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = yourName,
                onValueChange = { yourName = it },
                label = { Text("Your Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Dairy Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = mobile,
                onValueChange = { mobile = it },
                label = { Text("Mobile") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = village,
                onValueChange = { village = it },
                label = { Text("Village") },
                modifier = Modifier.fillMaxWidth()
            )
            pin?.let {
                OutlinedTextField(
                    value = it,
                    onValueChange = { pinVar = it },
                    label = { Text("pin") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    vm.saveUser(yourName,name, mobile, village)
                    pin?.let { pinVm.setNewPin(pinVar) }
                    navController.popBackStack()
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save")
            }
        }
    }
}
