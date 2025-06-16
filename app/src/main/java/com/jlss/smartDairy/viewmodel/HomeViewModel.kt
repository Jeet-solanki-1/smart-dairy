package com.jlss.smartDairy.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jlss.smartDairy.data.AppDatabase
import com.jlss.smartDairy.data.model.DataExportModel
import com.jlss.smartDairy.data.model.Rates
import com.jlss.smartDairy.data.model.ListOfEntry
import com.jlss.smartDairy.data.model.Members
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader







class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx = app.applicationContext
    private val db = AppDatabase.getDatabase(ctx)
    private val fatDao = db.fatRateDao()
    private val listOfEntryDao = db.listEntryDao()
    private val memberDao = db.memberDao()

    val rate: StateFlow<Double?> = fatDao.get()
        .map { it?.ratePerFat }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    data class RateData(
        val buyingRate: Double = 0.0,
        val factoryRate: Double = 0.0,
        val milkRate: Double = 0.0
    )

    private val _rateState: MutableStateFlow<Rates?> = MutableStateFlow(null)
    val rateState: StateFlow<Rates?> = _rateState

    private val _allRates = MutableStateFlow(RateData())
    val allRates: StateFlow<RateData> = _allRates

    init {
        viewModelScope.launch {
            fatDao.get().collect { entity ->
                // keep your existing rateState
                _rateState.value = entity
                // now update allRates so UI fields show persisted DB values
                entity?.let {
                    _allRates.value = RateData(
                        buyingRate  = it.ratePerFat,
                        factoryRate = it.sellingFatRate,
                        milkRate    = it.yourMilkRates
                    )
                }
            }
        }
    }


    suspend fun setAllRates(data: RateData) {
        _allRates.value = data
        fatDao.set(Rates(0, data.buyingRate, data.factoryRate, data.milkRate))
    }

    fun exportAllData(fileName: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val entries = listOfEntryDao.getAllOnce()
            val members = memberDao.getAllOnce()
            val exportModel = DataExportModel(entries = entries, members = members)
            val json = Gson().toJson(exportModel)
            val file = File(ctx.getExternalFilesDir(null), "$fileName.txt")
            file.writeText(json)

            val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Share Smart Dairy Data")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("ExportAllData", "Export failed", e)
        }
    }

    fun exportOnlyMembers(fileName: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val members = memberDao.getAllOnce()
            val json = Gson().toJson(members)
            val file = File(ctx.getExternalFilesDir(null), "$fileName.txt")
            file.writeText(json)

            val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Export Members Only")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(chooser)
        } catch (e: Exception) {
            Log.e("ExportMembers", "Export failed", e)
        }
    }

    fun importSmartJson(file: File, onEmpty: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val json = FileReader(file).use { it.readText().trim() }

            when {
                json.startsWith("[") -> {
                    importOnlyMembersFromJson(json, onEmpty)
                }

                json.startsWith("{") -> {
                    importAllDataFromJson(json, onEmpty)
                }

                else -> {
                    Log.e("SmartImport", "Unrecognized JSON format")
                }
            }

        } catch (e: Exception) {
            Log.e("SmartImport", "Failed to detect format", e)
        }
    }

    private suspend fun importAllDataFromJson(json: String, onEmpty: () -> Unit) {
        try {
            val type = object : TypeToken<DataExportModel>() {}.type
            val exportModel: DataExportModel = Gson().fromJson(json, type)

            if (exportModel.entries.isEmpty() && exportModel.members.isEmpty()) {
                onEmpty()
                return
            }

            exportModel.members.forEach { importedMember ->
                val existing = memberDao.findByName(importedMember.name)
                if (existing == null) {
                    memberDao.insert(importedMember)
                }
            }

            saveImportedEntries(exportModel.entries)

        } catch (e: Exception) {
            Log.e("ImportAllData", "Failed to import full data", e)
        }
    }

    private suspend fun importOnlyMembersFromJson(json: String, onEmpty: () -> Unit) {
        try {
            val type = object : TypeToken<List<Members>>() {}.type
            val members: List<Members> = Gson().fromJson(json, type)

            if (members.isEmpty()) {
                onEmpty()
                return
            }

            members.forEach { importedMember ->
                val existing = memberDao.findByName(importedMember.name)
                if (existing == null) {
                    memberDao.insert(importedMember)
                }
            }

        } catch (e: Exception) {
            Log.e("ImportOnlyMembers", "Failed to import members", e)
        }
    }

    fun importEntriesFromUri(context: Context, uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@launch
            val json = InputStreamReader(inputStream).readText().trim()

            when {
                json.startsWith("[") -> {
                    val type = object : TypeToken<List<ListOfEntry>>() {}.type
                    val entries: List<ListOfEntry> = Gson().fromJson(json, type)
                    saveImportedEntries(entries)
                }

                json.startsWith("{") -> {
                    importAllDataFromJson(json) {
                        Toast.makeText(context, "Imported file is empty", Toast.LENGTH_SHORT).show()
                    }
                }

                else -> {
                    Log.e("ImportUri", "Unrecognized JSON format")
                }
            }
        } catch (e: Exception) {
            Log.e("ImportUri", "Failed to import from URI", e)
        }
    }

    fun getAvailableJsonFilesFromPublicFolders(context: Context): List<File> {
        val result = mutableListOf<File>()

        val publicDirs = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            File(Environment.getExternalStorageDirectory(), "WhatsApp/Media/WhatsApp Documents")
        )

        for (dir in publicDirs) {
            val files = dir?.listFiles { file ->
                file.extension.equals("txt", ignoreCase = true)
            } ?: continue
            result.addAll(files)
        }

        if (result.isEmpty()) {
            Toast.makeText(context, "No File Found.", Toast.LENGTH_SHORT).show()
        }

        return result
    }

    fun saveImportedEntries(entries: List<ListOfEntry>) = viewModelScope.launch(Dispatchers.IO) {
        if (entries.isEmpty()) return@launch

        try {
            entries.forEach { listEntry ->
                listOfEntryDao.insert(listEntry)

                listEntry.listOfEntry.forEach { entry ->
                    val member = memberDao.findByName(entry.name)
                    if (member != null) {
                        val updated = member.copy(history = member.history + entry)
                        memberDao.update(updated)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("EntryVM", "Failed to insert ListOfEntry", e)
        }
    }
}
