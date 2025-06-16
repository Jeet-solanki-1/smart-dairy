package com.jlss.smartDairy.screen


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jlss.smartDairy.viewmodel.HomeViewModel

import kotlinx.coroutines.launch

@Composable
fun SetRatesScreen(
    vm: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // — 1. Collect the raw DB entity instead of allRates —
    val entity by vm.rateState.collectAsState(initial = null)

    // — 2. Your local UI state holders —
    val buyingRateState   = rememberSaveable { mutableStateOf("") }
    val factoryRateState  = rememberSaveable { mutableStateOf("") }
    val milkRateState     = rememberSaveable { mutableStateOf("") }

    // — 3. Sync UI state when the DB entity changes —
    LaunchedEffect(entity) {
        entity?.let {
            buyingRateState.value  = it.ratePerFat.toString()
            factoryRateState.value = it.sellingFatRate.toString()
            milkRateState.value    = it.yourMilkRates.toString()
        }
    }

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("🧮 Set Your Rates", style = MaterialTheme.typography.headlineMedium)

        RateCard(
            title = "Your Buying Fat Rate (₹/fat)",
            value = buyingRateState.value,
            onValueChange = { buyingRateState.value = it }
        )

        RateCard(
            title = "Selling Fat Rate at Factory (₹/fat)",
            value = factoryRateState.value,
            onValueChange = { factoryRateState.value = it }
        )

        RateCard(
            title = "Milk Rate (₹/litre)",
            value = milkRateState.value,
            onValueChange = { milkRateState.value = it }
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            val data = HomeViewModel.RateData(
                buyingRate  = buyingRateState.value.toDoubleOrNull() ?: 0.0,
                factoryRate = factoryRateState.value.toDoubleOrNull() ?: 0.0,
                milkRate    = milkRateState.value.toDoubleOrNull() ?: 0.0
            )

            coroutineScope.launch {
                vm.setAllRates(data)  // → writes into Room via fatDao.set(...)
                Toast.makeText(context, "Rates saved", Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("💾 Save All Rates")
        }
    }
}


@Composable
fun RateCard(title: String, value: String, onValueChange: (String) -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text("Enter rate") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
