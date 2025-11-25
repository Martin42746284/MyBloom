package com.example.mybloom.dao

import androidx.room.*
import com.example.mybloom.entities.DiscoveryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscoveryDao {
    @Query("SELECT * FROM discoveries WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserDiscoveries(userId: String): Flow<List<DiscoveryEntity>>
    @Query("SELECT * FROM discoveries WHERE id = :id")
    suspend fun getDiscoveryById(id: Int): DiscoveryEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscovery(discovery: DiscoveryEntity): Long
    @Update
    suspend fun updateDiscovery(discovery: DiscoveryEntity)
    @Delete
    suspend fun deleteDiscovery(discovery: DiscoveryEntity)
    @Query("DELETE FROM discoveries WHERE userId = :userId")
    suspend fun deleteAllUserDiscoveries(userId: String)
    @Query("SELECT COUNT(*) FROM discoveries WHERE userId = :userId")
    suspend fun getUserDiscoveryCount(userId: String): Int
    @Query("SELECT * FROM discoveries WHERE userId = :userId AND plantName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchDiscoveriesByName(userId: String, query: String): Flow<List<DiscoveryEntity>>
}
