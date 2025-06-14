// File: com.jlss.mukeshdairy.viewmodel.EntryViewModel.kt
package com.jlss.smartDairy.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jlss.smartDairy.data.AppDatabase
import com.jlss.smartDairy.data.UserPreferences
import com.jlss.smartDairy.data.model.Entry
import com.jlss.smartDairy.data.model.ListOfEntry
import com.jlss.smartDairy.data.model.Members
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalTime

import java.util.Locale

class EntryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val memberDao = db.memberDao()
    private val entryDao = db.entryDao()
    private val fatDao = db.fatRateDao()


     val ratePerFat = MutableStateFlow(0.0)
    val totalMilk = MutableStateFlow(0.0)
    val avgFat = MutableStateFlow(0.0)
    val totalAmount = MutableStateFlow(0.0)

    val userPrefs = UserPreferences(application.applicationContext)

    suspend fun refreshFatRate() {
        ratePerFat.value = fatDao.get().firstOrNull()?.ratePerFat ?: 0.0
    }

    /**
     * Editable table rows
     */
    private val _rows = MutableStateFlow<List<EntryRowState>>(emptyList())
    val rows = _rows

    fun clearRows() {
        _rows.value = emptyList()
    }

    init {

        viewModelScope.launch {
            // Step 1: Load unsaved rows from DataStore (if any)
            userPrefs.getUnsavedRows.collect { saved ->
                if (saved.isNotEmpty()) {
                    _rows.value = saved
                } else {
                    // Fallback: Pre-populate rows from member DAO
                    val names = memberDao.all()
                        .first()
                        .map { it.name }

                    _rows.value = names.mapIndexed { idx, name ->
                        EntryRowState(serialNo = idx + 1, name = name)
                    }
                }
            }
        }



        viewModelScope.launch {
            // Step 3: Update totals on row change
            rows.collect { currentRows ->
                totalMilk.value = currentRows.sumOf { it.milkQty.toDoubleOrNull() ?: 0.0 }
                val fats = currentRows.mapNotNull { it.fatRate.toDoubleOrNull() }
                avgFat.value = fats.average().takeIf { !it.isNaN() } ?: 0.0
                totalAmount.value = currentRows.sumOf { it.amount }
            }
        }

        viewModelScope.launch {
            // Step 4: Persist rows to DataStore after every change
            rows.collect {
                userPrefs.saveUnsavedRows(it)
            }
        }


    }

    // EntryViewModel.kt
    fun initRowsFromMembers(members: List<Members>) {
        _rows.value = members.mapIndexed { index, member ->
            EntryRowState(
                serialNo = index + 1,
                name = member.name,
                fatRate = "",
                milkQty = ""
            )
        }
    }

    /** Update a single row when user edits a field */
    fun updateRow(index: Int, update: EntryRowState.() -> EntryRowState) {
        val current = _rows.value.toMutableList()
        val updatedRow = update(current[index]) // invoke lambda to get new row

        val fat = updatedRow.fatRate.toDoubleOrNull()
        val qty = updatedRow.milkQty.toDoubleOrNull()
        val rate = ratePerFat.value

        val amount = if (fat != null && qty != null) {
            BigDecimal(fat.toString())
                .multiply(BigDecimal(rate.toString()))
                .multiply(BigDecimal(qty.toString()))
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()
        } else {
            0.0
        }

        current[index] = updatedRow.copy(amount = amount)
        _rows.value = current
    }


    /** Append an empty row */
    fun addEmptyRow() {
        val next = _rows.value.size + 1
        _rows.value = _rows.value + EntryRowState(serialNo = next)
    }

    // In EntryViewModel.kt
    fun setRow(index: Int, newRow: EntryRowState) {
        val updated = _rows.value.toMutableList()

        // Recalculate amount
        val fat = newRow.fatRate.toDoubleOrNull()
        val qty = newRow.milkQty.toDoubleOrNull()

        val rate = ratePerFat.value
        val amount = if (fat != null && qty != null) {
            BigDecimal(fat.toString())
                .multiply(BigDecimal(rate.toString()))
                .multiply(BigDecimal(qty.toString()))
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()
        } else {
            0.0
        }


        updated[index] = newRow.copy(amount = amount)
        _rows.value = updated
    }


//    /** Save all rows into Room */
//    fun saveAll() = viewModelScope.launch {
//        // Fetch the current rupees-per-fat rate (assume single-row FatRate table)
//        val currentRatePerFat = fatDao
//            .get()                              // Flow<FatRate?>
//            .first()?.ratePerFat
//            ?: return@launch                    // nothing to do if rate not set
//
//        // Determine if it's night
//        val hour = LocalTime.now().hour
//        val isNight = hour < 6 || hour >= 18
//
//        _rows.value.forEach { row ->
//            val fatPercent = row.fatRate.toDoubleOrNull() ?: return@forEach
//            val qty        = row.milkQty.toDoubleOrNull()  ?: return@forEach
//            val amt        = fatPercent * currentRatePerFat * qty
//
//            entryDao.insert(
//                Entry(
//                    fat      = fatPercent,
//                    milkQty      = qty,
//                    amountToPay  = amt,
//                    isNight      = isNight
//                )
//            )
//        }
//    }


//    fun processSpeechInput(speech: String) {
//        val cleaned = normalizeWordsToDigits(speech)
//        val words = cleaned.trim().lowercase(Locale.getDefault()).split(" ")
//
//        val nameParts = mutableListOf<String>()
//        var milk: String? = null
//        var fat: String? = null
//
//        for (word in words) {
//            val value = word.toDoubleOrNull()
//            if (value == null) {
//                // Word is not a number, assume part of name
//                nameParts.add(word)
//            } else {
//                // First number = milk, second = fat
//                if (milk == null) milk = word
//                else if (fat == null) fat = word
//            }
//        }
//
//        val name = nameParts.joinToString(" ").trim()
//        if (name.isBlank()) return // Name is required
//
//        val currentRows = _rows.value.toMutableList()
//        val index = findBestMatchingIndex(name, currentRows)
//
//        if (index != -1) {
//            val row = currentRows[index]
//            val updatedRow = row.copy(
//                fatRate = fat ?: row.fatRate,
//                milkQty = milk ?: row.milkQty
//            )
//            setRow(index, updatedRow)
//        } else {
//            val newRow = EntryRowState(
//                serialNo = currentRows.size + 1,
//                name = name,
//                fatRate = fat ?: "",
//                milkQty = milk ?: ""
//            )
//            currentRows.add(newRow)
//            _rows.value = currentRows
//        }
//    }

    fun processSpeechInput(speech: String) {
        val cleaned = normalizeWordsToDigits(speech)
        val parts = cleaned.trim().lowercase(Locale.getDefault()).split(",")

        val nameAndMilk = parts.getOrNull(0)?.split(" ") ?: return
        val fat = parts.getOrNull(1)?.trim()

        val nameParts = mutableListOf<String>()
        var milk: String? = null

        for (word in nameAndMilk) {
            val value = word.toDoubleOrNull()
            if (value == null) {
                nameParts.add(word)
            } else {
                milk = word
            }
        }

        val name = nameParts.joinToString(" ").trim()
        if (name.isBlank()) return

        val currentRows = _rows.value.toMutableList()
        val index = findBestMatchingIndex(name, currentRows)

        if (index != -1) {
            val row = currentRows[index]
            val updatedRow = row.copy(
                fatRate = fat ?: row.fatRate,
                milkQty = milk ?: row.milkQty
            )
            setRow(index, updatedRow)
        } else {
            val newRow = EntryRowState(
                serialNo = currentRows.size + 1,
                name = name,
                fatRate = fat ?: "",
                milkQty = milk ?: ""
            )
            currentRows.add(newRow)
            _rows.value = currentRows
        }
    }






    fun normalizeWordsToDigits(text: String): String {
            val map = mapOf(
                "coma" to ",","cama" to ",",
                "dudh" to "milk","feet" to "fat","phat" to "fat",
                "naam" to "name",
                "zero" to "0", "shunya" to "0",
                "one" to "1", "ek" to "1",
                "two" to "2", "to" to "2", "do" to "2",
                "three" to "3", "teen" to "3",
                "four" to "4", "char" to "4","for" to "4",
                "five" to "5", "panch" to "5",
                "six" to "6", "chhe" to "6",
                "seven" to "7", "saat" to "7",
                "eight" to "8", "aath" to "8",
                "nine" to "9", "nau" to "9",
                "ten" to "10", "das" to "10",
                "eleven" to "11", "gyarah" to "11",
                "twelve" to "12", "barah" to "12",
                "thirteen" to "13", "terah" to "13",
                "fourteen" to "14", "chaudah" to "14",
                "fifteen" to "15", "pandrah" to "15",
                "sixteen" to "16", "solah" to "16",
                "seventeen" to "17", "satrah" to "17",
                "eighteen" to "18", "atharah" to "18",
                "nineteen" to "19", "unnis" to "19",
                "twenty" to "20", "bees" to "20", "biss" to "20",
                "twenty-one" to "21", "ikkees" to "21",
                "twenty-two" to "22", "baees" to "22",
                "twenty-three" to "23", "teees" to "23",
                "twenty-four" to "24", "chaubees" to "24",
                "twenty-five" to "25", "pachchees" to "25",
                "twenty-six" to "26", "chhabbees" to "26",
                "twenty-seven" to "27", "sattaees" to "27",
                "twenty-eight" to "28", "athaeess" to "28",
                "twenty-nine" to "29", "untees" to "29",
                "thirty" to "30", "tees" to "30", "tis" to "30",
                "thirty-one" to "31", "ikattis" to "31",
                "thirty-two" to "32", "battis" to "32",
                "thirty-three" to "33", "taitis" to "33",
                "thirty-four" to "34", "chauntis" to "34",
                "thirty-five" to "35", "paintis" to "35",
                "thirty-six" to "36", "chhattis" to "36",
                "thirty-seven" to "37", "saintis" to "37",
                "thirty-eight" to "38", "adhaitis" to "38",
                "thirty-nine" to "39", "untalis" to "39",
                "forty" to "40", "chalis" to "40",
                "forty-one" to "41", "iktalis" to "41",
                "forty-two" to "42", "bayalis" to "42",
                "forty-three" to "43", "tetalis" to "43",
                "forty-four" to "44", "chauntalis" to "44",
                "forty-five" to "45", "paintalis" to "45",
                "forty-six" to "46", "chiyalis" to "46",
                "forty-seven" to "47", "saintalis" to "47",
                "forty-eight" to "48", "adhaitalis" to "48",
                "forty-nine" to "49", "unchaas" to "49",
                "fifty" to "50", "pachas" to "50",
                "point" to "."
            )


        val words = text.lowercase().split(" ")
        val result = words.map { word ->
            map[word] ?: word
        }

        return result.joinToString(" ")
    }
    fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }

        for (i in 0..a.length) {
            for (j in 0..b.length) {
                if (i == 0) dp[i][j] = j
                else if (j == 0) dp[i][j] = i
                else {
                    val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                    dp[i][j] = minOf(
                        dp[i - 1][j] + 1,       // deletion
                        dp[i][j - 1] + 1,       // insertion
                        dp[i - 1][j - 1] + cost // substitution
                    )
                }
            }
        }

        return dp[a.length][b.length]
    }

    fun findBestMatchingIndex(name: String, rows: List<EntryRowState>): Int {
        var minDistance = Int.MAX_VALUE
        var bestIndex = -1

        for ((index, row) in rows.withIndex()) {
            val distance = levenshtein(name.lowercase(), row.name.lowercase())
            if (distance < minDistance) {
                minDistance = distance
                bestIndex = index
            }
        }

        // You can define a max acceptable distance (tune this threshold)
        return if (minDistance <= 3) bestIndex else -1
    }


    fun saveAll() = viewModelScope.launch {
        val currentRatePerFat = fatDao.get().first()?.ratePerFat ?: return@launch
        val hour = LocalTime.now().hour
        val isNight = hour >= 12
// Always include every row, defaulting missing fat/qty to 0.00
        val entryList = _rows.value.map { row ->
            val fat = row.fatRate.toDoubleOrNull() ?: 0.00
            val qty = row.milkQty.toDoubleOrNull() ?: 0.00
            val rate = ratePerFat.value
            val amount = BigDecimal(fat.toString())
                .multiply(BigDecimal(currentRatePerFat.toString()))
                .multiply(BigDecimal(qty.toString()))
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()

            Entry(
                name = row.name,
                fat = fat,
                milkQty = qty,
                amountToPay = amount,
                isNight = isNight
            )
        }

        if (entryList.isNotEmpty()) {
            entryList.forEach { entry ->
                try {
                    entryDao.insert(entry)
                    // **append to member history**
                    val member = memberDao.findByName(entry.name)
                    if (member != null) {
                        val update = member.copy(history = member.history.plus(entry))
                        memberDao.update(update)
                    }


                    Log.d("EntryVM", "Inserted Entry id=${entry.id}, name=${entry.name}")
                } catch (e: Exception) {
                    Log.e("EntryVM", "Failed to insert entry: $entry", e)
                }
            }
            try {
                val listOfEntry = ListOfEntry(listOfEntry = entryList, isNight = isNight)
                db.listEntryDao().insert(listOfEntry)
                Log.d(
                    "EntryVM",
                    "Inserted ListOfEntry id=${listOfEntry.id}, size=${entryList.size}"
                )
            } catch (e: Exception) {
                Log.e("EntryVM", "Failed to insert ListOfEntry", e)
            }
        }
        // Now that we’ve written to the DB, clear the rows:
        _rows.value = emptyList()
        Log.d("EntryVM", "Mapped entryList size = ${entryList.size}")
        _rows.value = emptyList()
        userPrefs.clearUnsavedRows()

    }
}
