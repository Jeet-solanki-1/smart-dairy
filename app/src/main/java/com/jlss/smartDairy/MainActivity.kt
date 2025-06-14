package com.jlss.smartDairy

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

import com.jlss.smartDairy.data.UserPreferences
import com.jlss.smartDairy.language.LocaleHelper
import com.jlss.smartDairy.screen.MukeshDairyApp
import com.jlss.smartDairy.ui.theme.MukeshDairyTheme

class MainActivity : ComponentActivity() {

    @RequiresApi(35)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MukeshDairyTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Request permissions on launch
                    RequestAudioPermissionIfNeeded()
                    RequestStoragePermissionsIfNeeded()

                    // Main app
                    MukeshDairyApp()
                }
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        val langCode = UserPreferences(base).getAppLanguageSync()
        val context = LocaleHelper.setLocale(base, langCode)
        super.attachBaseContext(context)
    }
}

@Composable
fun RequestAudioPermissionIfNeeded() {
    val context = LocalContext.current
    val activity = context as Activity

    val permission = Manifest.permission.RECORD_AUDIO

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("Permission", "RECORD_AUDIO granted: $isGranted")
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(permission)
        }
    }
}
@Composable
fun RequestStoragePermissionsIfNeeded() {
    val context = LocalContext.current
    val activity = context as Activity

    val permissions = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        )
        else -> listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        permissionsMap.forEach { (perm, granted) ->
            Log.d("Permission", "$perm granted: $granted")
        }
    }

    LaunchedEffect(Unit) {
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            launcher.launch(notGranted.toTypedArray())
        }
    }
}
