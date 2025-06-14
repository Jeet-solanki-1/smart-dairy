package com.jlss.smartDairy.screen

// com.jlss.smartDairy.ui.LanguageSelectorDialog.kt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import com.jlss.smartDairy.language.AppLanguage

@Composable
fun LanguageSelectorDialog(
    currentLang: AppLanguage,
    onDismiss: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("भाषा चुनें / Select Language") },
        confirmButton = {},
        text = {
            Column {
                AppLanguage.values().forEach { lang ->
                    Row {
                        RadioButton(
                            selected = currentLang == lang,
                            onClick = {
                                onLanguageSelected(lang)
                                onDismiss()
                            }
                        )
                        Text(text = lang.displayName)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
