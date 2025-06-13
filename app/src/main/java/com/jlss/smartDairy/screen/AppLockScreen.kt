package com.jlss.smartDairy.screen

import android.app.Application
import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jlss.smartDairy.navigation.AppLockViewModelFactory
import com.jlss.smartDairy.viewmodel.AppLockViewModel
import java.util.concurrent.Executor

@Composable
fun AppLockScreen(
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current.applicationContext as Application
    val factory = AppLockViewModelFactory(context)
    val viewModel: AppLockViewModel = viewModel(factory = factory)


    val activity = context as? FragmentActivity

    val savedPin by viewModel.pin.collectAsState()
    val isUnlocked by viewModel.isUnlocked.collectAsState()

    var enteredPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Load PIN from Room on first launch
    LaunchedEffect(Unit) {
        viewModel.loadPin()

        // Show biometric prompt
        if (activity != null && canAuthenticate(context)) {
            showBiometricPrompt(
                activity,
                onSuccess = { viewModel.setUnlocked() },
                onError = { errorMsg = it }
            )
        }
    }

    // Navigate if unlocked
    LaunchedEffect(isUnlocked) {
        if (isUnlocked) {
            onUnlocked()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Enter PIN to unlock", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = enteredPin,
                onValueChange = { enteredPin = it },
                label = { Text("PIN") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                viewModel.verifyPin(enteredPin)
                if (!viewModel.isUnlocked.value) {
                    errorMsg = "Incorrect PIN"
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Unlock")
            }

            errorMsg?.let {
                Spacer(Modifier.height(12.dp))
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// Check if biometric auth is available
fun canAuthenticate(context: Context): Boolean {
    val biometricManager = BiometricManager.from(context)
    return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS
}

fun showBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val executor: Executor = ContextCompat.getMainExecutor(activity)
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock Mukesh Dairy")
        .setSubtitle("Use your fingerprint or face")
        .setNegativeButtonText("Use PIN")
        .build()

    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError("Biometric error: $errString")
            }

            override fun onAuthenticationFailed() {
                onError("Biometric authentication failed")
            }
        })

    biometricPrompt.authenticate(promptInfo)
}
