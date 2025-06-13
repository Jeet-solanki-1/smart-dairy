package com.jlss.smartDairy.data.dao


import androidx.room.*
import com.jlss.smartDairy.data.model.AppLockEntity
import com.jlss.smartDairy.data.model.Entry
import com.jlss.smartDairy.data.model.FatRate
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

}



@Dao
interface FatRateDao {
    /**
     * Inserts or replaces the single FatRate row (id=0).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(rate: FatRate)

    /**
     * Returns the single FatRate record as a Flow, or null if never set.
     */
    @Query("SELECT * FROM fat_rate WHERE id = 0")
    fun get(): Flow<FatRate?>
}




@Dao
interface EntryDao {
    @Insert
    suspend fun insert(e: Entry)
    @Query("SELECT * FROM entry ORDER BY timestamp DESC")
    fun all(): Flow<List<Entry>>
    @Query("SELECT * FROM entry WHERE isNight = :night ORDER BY timestamp DESC")
    fun byShift(night: Boolean): Flow<List<Entry>>
}



@Dao
interface ListEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ListOfEntry)

    @Query("SELECT * FROM list_of_entry ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ListOfEntry>>

    @Query("SELECT * FROM list_of_entry WHERE id = :id")
    suspend fun getById(id: Long): ListOfEntry?

    @Delete
    suspend fun delete(list: ListOfEntry) // ✅ Add this
}
