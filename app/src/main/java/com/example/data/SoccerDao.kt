package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SoccerDao {
    @Query("SELECT * FROM players ORDER BY rating DESC")
    fun getAllPlayers(): Flow<List<PlayerCard>>

    @Query("SELECT * FROM players WHERE isMySquad = 1")
    fun getMySquad(): Flow<List<PlayerCard>>

    @Query("SELECT * FROM players WHERE isUnlocked = 1")
    fun getUnlockedPlayers(): Flow<List<PlayerCard>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlayers(players: List<PlayerCard>)

    @Update
    suspend fun updatePlayer(player: PlayerCard)

    @Query("UPDATE players SET isMySquad = :isMySquad WHERE id = :id")
    suspend fun updateSqaudStatus(id: String, isMySquad: Boolean)

    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    fun getUserStatsFlow(): Flow<UserStats?>

    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    suspend fun getUserStats(): UserStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStats)

    @Update
    suspend fun updateUserStats(stats: UserStats)
}
