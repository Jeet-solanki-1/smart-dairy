package com.jlss.smartDairy.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jlss.smartDairy.viewmodel.HomeViewModel


@Composable
fun HomeScreen(vm: HomeViewModel = viewModel()) {
    // collect latest fat-rate from your ViewModel’s StateFlow
    val rate by vm.rate.collectAsState()

    // remember an editable input string, initialized from current rate
    var input by remember { mutableStateOf(rate?.toString() ?: "") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Current rate: ${rate ?: "--"} ₹/fat",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("New Rate") },
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        )

        Button(onClick = {
            input.toDoubleOrNull()?.let { vm.setRate(it) }
        }) {
            Text("Set Rate")
        }
    }
}
