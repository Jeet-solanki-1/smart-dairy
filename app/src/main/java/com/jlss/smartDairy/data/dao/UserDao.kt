package com.jlss.smartDairy.data.dao


import androidx.room.*
import com.jlss.smartDairy.data.model.AppLockEntity
import com.jlss.smartDairy.data.model.Entry
import com.jlss.smartDairy.data.model.Rates
import com.jlss.smartDairy.data.model.ListOfEntry
import com.jlss.smartDairy.data.model.User
import com.jlss.smartDairy.data.model.Members


import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM user LIMIT 1")
    fun getUser(): Flow<User?>

    @Update
    suspend fun updateUser(user: User)
}


@Dao
interface AppLockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPin(pin: AppLockEntity)

    @Query("SELECT * FROM app_lock WHERE id = 0")
    suspend fun getPin(): AppLockEntity?

    @Query("DELETE FROM app_lock")
    suspend fun clearPin()
}


@Dao
interface MemberDao {
    @Insert suspend fun insert(m: Members)
    @Delete suspend fun delete(m: Members)
    @Query("SELECT * FROM members ORDER BY name")
    fun all(): Flow<List<Members>>

    @Update
    suspend fun update(member: Members)

    // convenience to fetch by name
    @Query("SELECT * FROM members WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): Members?

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun findById(id: Long): Members?
    // New: clear that member’s history
    @Query("UPDATE members SET history = '' WHERE id = :memberId")
    suspend fun clearHistory(memberId: Long)
    @Query("SELECT * FROM members ORDER BY name")
    suspend fun getAllOnce(): List<Members>

}



@Dao
interface FatRateDao {
    @Query("SELECT * FROM rate LIMIT 1")
    fun get(): Flow<Rates?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(rates: Rates)
}



@Dao
interface EntryDao {
    @Insert
    suspend fun insert(e: Entry)

    @Insert
    suspend fun insertAll(entries: List<Entry>)  // ← Needed for import

    @Query("SELECT * FROM entry ORDER BY timestamp DESC")
    suspend fun getAll(): List<Entry>

    @Query("SELECT * FROM entry WHERE isNight = :night ORDER BY timestamp DESC")
    fun byShift(night: Boolean): Flow<List<Entry>>
}



@Dao
interface ListEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ListOfEntry)
    @Query("SELECT * FROM list_of_entry")
    suspend fun getAllOnce(): List<ListOfEntry> // ✅ Rename to avoid conflict with Flow version
    @Query("SELECT * FROM list_of_entry ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ListOfEntry>>

    @Query("SELECT * FROM list_of_entry WHERE id = :id")
    suspend fun getById(id: Long): ListOfEntry?
    @Update
    suspend fun update(list: ListOfEntry)
    @Delete
    suspend fun delete(list: ListOfEntry) // ✅ Add this
    @Insert
    suspend fun insertAll(entries: List<ListOfEntry>)  // ← Needed for import
}
