package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ClaimEntity
import com.example.data.model.ItemEntity
import com.example.data.model.MatchEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
}

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE isApproved = 1 ORDER BY createdAt DESC")
    fun getAllApprovedItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items ORDER BY createdAt DESC")
    fun getAllItemsAdmin(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE userId = :userId ORDER BY createdAt DESC")
    fun getItemsByUser(userId: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE itemId = :itemId LIMIT 1")
    suspend fun getItemById(itemId: Long): ItemEntity?

    @Query("SELECT * FROM items WHERE itemId = :itemId LIMIT 1")
    fun getItemByIdFlow(itemId: Long): Flow<ItemEntity?>

    @Query("SELECT * FROM items WHERE itemType = :type AND isApproved = 1 AND status != 'Returned' AND status != 'Closed' ORDER BY createdAt DESC")
    suspend fun getActiveItemsByType(type: String): List<ItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity): Long

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    @Query("UPDATE items SET status = :status WHERE itemId = :itemId")
    suspend fun updateItemStatus(itemId: Long, status: String)

    @Query("UPDATE items SET isApproved = :approved WHERE itemId = :itemId")
    suspend fun setItemApproval(itemId: Long, approved: Boolean)
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches WHERE lostItemId = :lostId OR foundItemId = :foundId ORDER BY matchScore DESC")
    fun getMatchesForItem(lostId: Long, foundId: Long): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches ORDER BY matchScore DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity): Long

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("DELETE FROM matches WHERE lostItemId = :itemId OR foundItemId = :itemId")
    suspend fun deleteMatchesForItem(itemId: Long)
}

@Dao
interface ClaimDao {
    @Query("SELECT * FROM claims WHERE itemId = :itemId ORDER BY createdAt DESC")
    fun getClaimsForItem(itemId: Long): Flow<List<ClaimEntity>>

    @Query("SELECT * FROM claims WHERE claimantUserId = :userId ORDER BY createdAt DESC")
    fun getClaimsByUser(userId: String): Flow<List<ClaimEntity>>

    @Query("SELECT * FROM claims ORDER BY createdAt DESC")
    fun getAllClaims(): Flow<List<ClaimEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaim(claim: ClaimEntity): Long

    @Update
    suspend fun updateClaim(claim: ClaimEntity)
}
