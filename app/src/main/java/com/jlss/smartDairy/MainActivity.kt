package com.jlss.smartDairy

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jlss.smartDairy.ui.theme.MukeshDairyTheme
import com.jlss.smartDairy.screen.MukeshDairyApp

class MainActivity : ComponentActivity() {

    @RequiresApi(35)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MukeshDairyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val activity = context as Activity

                    // Request permission on launch
                    RequestAudioPermissionIfNeeded(context, activity)

                    MukeshDairyApp()
                }
            }
        }
    }
}


@Composable
fun RequestAudioPermissionIfNeeded(context: Context, activity: Activity) {
    LaunchedEffect(Unit) {
        val permission = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), 1001)
        }
    }
}
