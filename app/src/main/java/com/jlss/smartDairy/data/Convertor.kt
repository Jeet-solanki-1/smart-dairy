package com.jlss.smartDairy.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jlss.smartDairy.data.model.Entry

class Converters {
    private val gson = Gson()
    private val listType = object : TypeToken<List<Entry>>() {}.type

    @TypeConverter
    fun fromEntryList(value: List<Entry>?): String {
        // Room may pass null for a nullable List<Entry>? field,
        // so we serialize null → empty list JSON.
        return gson.toJson(value ?: emptyList<Entry>())
    }

    @TypeConverter
    fun toEntryList(value: String?): List<Entry> {
        // If the database value is null or blank, return an empty list
        if (value.isNullOrBlank()) return emptyList()
        return gson.fromJson(value, listType)
    }
}
