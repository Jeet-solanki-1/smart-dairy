package com.jlss.smartDairy.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val mobile: String,
    val village: String
)

@Entity(tableName = "app_lock")
data class AppLockEntity(
    @PrimaryKey val id: Int = 0, // Always single record
    val pin: String // Store hashed PIN in production (this is plain for simplicity)
)

@Entity(tableName = "entry")
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name:String,
    val fat: Double,
    val milkQty: Double,
    val amountToPay: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val isNight: Boolean = false
)


@Entity(tableName = "list_of_entry")
data class ListOfEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val listOfEntry: List<Entry>,
    val timestamp: Long = System.currentTimeMillis(),
    val isNight: Boolean
)



@Entity(tableName = "members")
data class Members(
    @PrimaryKey (autoGenerate = true) val id: Long = 0L,
    val name: String, // Store hashed PIN in production (this is plain for simplicity)
    // add timestamp too
    val dateOfJoin: Long = System.currentTimeMillis(),
    // here we have to provide the entries of this member till joined
    // never null—Room will store "[]" when you don’t supply any history
    val history: List<Entry> = emptyList()

    )




@Entity(tableName = "fat_rate")
data class FatRate(
    @PrimaryKey val id: Int = 0,   // always 0 so there’s only one row
    val ratePerFat: Double
)
