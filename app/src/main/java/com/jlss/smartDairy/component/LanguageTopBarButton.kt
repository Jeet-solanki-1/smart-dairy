package com.jlss.smartDairy.component

import android.app.Activity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable

// com.jlss.smartDairy.ui.LanguageTopBarButton.kt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.jlss.smartDairy.R
import com.jlss.smartDairy.screen.LanguageSelectorDialog
import com.jlss.smartDairy.viewmodel.LanguageViewModel


@Composable
fun LanguageTopBarButton(viewModel: LanguageViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    IconButton(onClick = { showDialog = true }) {
        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.members))
    }




    if (showDialog) {
        LanguageSelectorDialog(
            currentLang = viewModel.selectedLang.collectAsState().value,
            onDismiss = { showDialog = false },
            onLanguageSelected = { lang ->
                viewModel.changeLanguage(lang)
                activity?.recreate()

            }
        )
    }
}
